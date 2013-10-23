package wormbase.model.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.*;

import org.apache.commons.lang.StringUtils;
import org.intermine.bio.dataconversion.*;
import org.xml.sax.InputSource;

import net.sf.saxon.s9api.*;


public class XPathResolver {

    private WMDebug wmd;
    private LinkedHashMap<MappingFileKey, XPathExecutable> key2Exec;
    private XPathCompiler xpcomp;
    private String classPID;
    private FileWriter rejectsFW = null;
    private Processor saxonProcessor;
    private MappingFileKey PIDKey = null;
    private XdmNode currentXMLItem;
    private LinkedHashMap<String, MappingFileKey> field2Key;
    
	/**
	 * Parses the mapping file and resolves it's mappings on arbitrary chunks of XML.
	 * @throws Exception 
	 */
	public XPathResolver(WMDebug wmd, String mappingFilePath) throws Exception{
		this.wmd = wmd;
		saxonProcessor = new Processor(false);
		xpcomp = saxonProcessor.newXPathCompiler();
		field2Key = new LinkedHashMap<String, MappingFileKey>();
		key2Exec = new LinkedHashMap<MappingFileKey, XPathExecutable>();
		
		createDataMapping(mappingFilePath);
	}
	
	/**
	 * Returns net.sf.saxon.s9api.XdmNode constructed from XML string
	 * @param xmlChunk
	 * @return
	 * @throws SaxonApiException
	 */
	private XdmNode XdmNodeFromString(String xmlChunk) throws SaxonApiException{
		DocumentBuilder docB = saxonProcessor.newDocumentBuilder();
		return docB.build(
					new SAXSource(
							new InputSource(
									new java.io.ByteArrayInputStream(
											xmlChunk.getBytes()))));
	}
	
	/**
	 * Populates internal MappingFileKey to XPathExecutable mapping from
	 * properties file.
	 * @param mappingFile
	 * @throws Exception
	 */
	private void createDataMapping(String mappingFile ) throws Exception{
        LinkedProperties dataMapping = new LinkedProperties();
        FileReader mappingFR = new FileReader(mappingFile);
        
        /*
         * PROBLEM:
         * Properties type is map, doesn't save order.
         * Create parallel array to preserve order, use to will in linkedHashMaps defined above
         * 
         */
        
    	try {
			dataMapping.load(mappingFR);
		} catch (FileNotFoundException e) {
			wmd.debug("ERROR: "+mappingFile+" not found");
			throw e;
		}
    	
		wmd.debug("Parsing mapping file...");
        // Get enumerator of InterMine datapaths to map (ex: primaryIdentifier)
        Enumeration<Object> dataPathEnum = dataMapping.keys();
        
    	wmd.debug("=== Mapping file entries ===");
        String rawPropKey;
        
        // fill mapping file hash
        while( dataPathEnum.hasMoreElements() ){
        	rawPropKey = (String) dataPathEnum.nextElement(); // ex: "symbol"
        	if(rawPropKey.length() == 0){
        		continue;
        	}
        	
        	MappingFileKey propKey = new MappingFileKey(rawPropKey);
        	
        	wmd.debug("=== "+propKey.getRawKey()+" ===");
        	wmd.debug("cast type: "+propKey.getCastType());
        	wmd.debug("datapath: "+propKey.getDataPath());

        	String xpathQuery = dataMapping.getProperty(rawPropKey); // ex: "/Transcript/text()[1]"
        	
        	// The XPath object compiles the XPath expression
        	XPathExecutable xpexec = xpcomp.compile( xpathQuery );
	        
	        field2Key.put(propKey.getDataPath(),propKey);
	        key2Exec.put(propKey, xpexec);
        }
    	wmd.debug("=== ==================== ===");
	}
	
	public void closeRejectsFile() throws IOException{
		rejectsFW.close();
	}
	
	/**
	 * Evaluates the value of the given field in the current XML context.  
	 * @return String array of results, extra whitespace stripped
	 * @throws SaxonApiException 
	 */
	public String[] getFieldValue(String field) throws SaxonApiException{
		// Prepare compiled xpath for execution
		XPathSelector xpsel = key2Exec.get(field2Key.get(field)).load();
		
		// set context XML
		xpsel.setContextItem(currentXMLItem);
		
		Vector<String> results = new Vector<String>();
		// iterator() evaluates xpath on XML
		Iterator<XdmItem> resultsIterator = xpsel.iterator();
		while(resultsIterator.hasNext()){
			XdmItem nextItem = resultsIterator.next();
			results.add(StringUtils.strip(nextItem.getStringValue()));
		}
		
		if(field2Key.get(field).isForcedBool()){
			if(results.isEmpty()){
				return new String[]{"false"};
			}else{
				return new String[]{"true"};
			}
		}
		
		return results.toArray(new String[]{});
	}
	
	public MappingFileKey getMappingFileKey(String field){
		return field2Key.get(field);
	}
	
	public MappingFileKey[] getMappingFileFields(){
		if(key2Exec != null){
			return key2Exec.keySet().toArray(new MappingFileKey[]{});
		}else{
			return null;
		}
	}

	/**
	 * Set the XML string future XPath queries will run on.
	 * @param xmlChunk
	 * @throws IOException
	 */
	public void setXML(String xmlChunk) throws IOException{
		try{
			// Load XML into an XdmNode 
			// XdmNode is an XdmItem
			currentXMLItem = XdmNodeFromString(xmlChunk);
		}catch(SaxonApiException e){
			try{
				wmd.debug("CALLING XML SANITATION FUNCTION");
				String repairedData = PackageUtils.sanitizeXMLTags(xmlChunk);
				currentXMLItem = XdmNodeFromString(repairedData);
			}catch( Exception e1 ){
    			try{
    				
    				if(rejectsFW != null){
	    				wmd.log("### SANITATION FAILED: ADDING RECORD TO REJECTS FILE ###");
	    				
	    				// Add to rejects file
		    			rejectsFW.write(xmlChunk);
		    			rejectsFW.write("\n\n");
    				}
    			}catch( IOException e2 ){
    				System.out.println("Something wrong with the FileWriter");
    				throw e2;
    			}
    			
			}
		}
	}
	

	public void setRejectFile(String rejectFilePath) throws IOException{
		if(rejectFilePath != null)
			rejectsFW = new FileWriter(rejectFilePath); // creates file if exists
		
	}
}
