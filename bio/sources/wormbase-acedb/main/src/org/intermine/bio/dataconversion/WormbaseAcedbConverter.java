package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.*;
import org.intermine.metadata.*;
import org.intermine.util.TypeUtil;
import org.intermine.xml.full.Item;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import wormbase.model.parser.*;

/**
 * 
 * @author
 */
public class WormbaseAcedbConverter extends BioFileConverter
{
    
	private final String currentClass = "Gene"; 
	private final String errorFilePath = "/home/jdmswong/website-intermine/acedb-dev/intermine/wormmine/wbconverter_error.txt";
	private final String rejectFilePath = "/home/jdmswong/intermine/datadir/wormbase-acedb/wormbase-acedb-rejects.xml";

	private static final String DATASET_TITLE = "wormbaseAcedb"; //"Add DataSet.title here";
    private static final String DATA_SOURCE_NAME = "wormbaseAcedbFileconverter"; //"Add DataSource.name here";

    private WMDebug wmd;
    private DataMapper dataMapping = null;
    private Model model;
    private ClassDescriptor classCD; // CD of current data type being processed 
    
    // Items that have already been referenced and stored
    // Key: "className:id", Value: Item ID (ex: "4_1")
	private HashMap<String, Item> storedRefItems; 
	
	private HashMap<String, String> keyMapping; // the primary key for each class
	
    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public WormbaseAcedbConverter(ItemWriter writer, Model _model) {
        super(writer, _model, DATA_SOURCE_NAME, DATASET_TITLE);
        
        wmd = new WMDebug();
        wmd.off();
        
        wmd.debug("Constructor called");
        
//        // keyMapping hash block for debugging
//        keyMapping.put("Organism", "name"); 
//        keyMapping.put("Transcript", "primaryIdentifier"); 
//        // end keyMapping hash block
        
        storedRefItems = new HashMap<String, Item>();
        model = _model;
        
        classCD = model.getClassDescriptorByName(currentClass); 
        
//        // Begin debug code
//        ClassDescriptor geneCD = model.getClassDescriptorByName("Gene");
//        
//        
//        String label[] = {
//        		"Gene attrs",
//        		"Gene refs",
//        		"Gene colls",
//        		"Gene fields"
//        		};
//        String value[] = {
//        		geneCD.getAllAttributeDescriptors().toString(),
//        		geneCD.getAllReferenceDescriptors().toString(),
//        		geneCD.getAllCollectionDescriptors().toString(),
//        		geneCD.getAllFieldDescriptors().toString()
//        		};
//        for(int i=0; i<label.length; i++){
//        	System.out.printf("[%s]\t[%s]\n",label[i],value[i]);
//        }
//        // end debug code
        
    }

    /**
     * 
     * @param reader The java.io.BufferedReader that intermine passes in
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	wmd.debug("started WormbaseAcedbConverter.process()"); 
    	
    	// Checking for properties
    	if( dataMapping == null ){
    		throw new Exception("mapping.file property not defined for this"+
    				" source in the project.xml");
    	}
    	if( keyMapping == null){
    		throw new Exception("key.file property not defined for this"+
    				" source in the project.xml");
    	}
    	

		FileWriter fw = new FileWriter(rejectFilePath);
		FileParser fp = new FileParser(reader);
    	
    	// foreach XML string
    	String dataString;
    	int count=0; // 
    	while( (dataString = fp.getDataString()) != null ){
		
    		count++;
    		
//    		if(count < 1070){
//    			System.out.println(String.valueOf(count)+":"+dataString.length());
//    			continue;
//    		}
    		
    		Document doc;
    		try{
				// Load XML into org.w3c.dom.Document 
				doc = PackageUtils.loadXMLFrom(dataString);
    		}catch(SAXParseException e){
    			try{
    				/*
    				 * Possible error sources:
    				 *  "<2_poinnt>"
    				 *  unescaped '&', '<', or '>' inside of tags
    				 */
    				wmd.debug("CALLING XML SANITATION FUNCTION");
    				String repairedData = PackageUtils.sanitizeXMLTags(dataString);
    				doc = PackageUtils.loadXMLFrom(repairedData);
    			}catch( SAXParseException e1 ){
	    			try{
	    				wmd.debug("### SANITATION FAILED: ADDING RECORD TO REJECTS FILE ###");
	    				
	    				// Add to rejects file
		    			fw.write("==============================================\n");
		    			System.out.println("Original error: "+e.getMessage());
		    			fw.write("\n");
		    			System.out.println("Post sanitation: "+e1.getMessage());
		    			fw.write("\n\n");
		    			fw.write(dataString);
	    			}catch( Exception e2 ){
	    				System.out.println("Something wrong with the FileWriter");
	    				throw e2;
	    			}
	    			continue;
    			}
    		}
			
		    // Get XPathFactory
	        XPathFactory xpf = XPathFactory.newInstance();
	        XPath xpath = xpf.newXPath();
	    	
	        // Get enumerator of InterMine datapaths to map (ex: primaryIdentifier)
	        Enumeration<Object> dataPathEnum = dataMapping.keys();
	        
	        
	        Item item = createItem(currentClass);
	        
	        
	        String dataPath;
	        String ID = null;
	        while( dataPathEnum.hasMoreElements() ){ // foreach property mapping
	        	dataPath = (String) dataPathEnum.nextElement(); // ex: symbol
	        	wmd.debug("Processing property:["+dataPath+"]");
	        	String xpathQuery = dataMapping.getProperty(dataPath);
	        	
	        	// The XPath object compiles the XPath expression
		        XPathExpression expr = xpath.compile( xpathQuery );
		        
		        
		        // '.' indicates join, aka reference or collection
		        if( dataPath.contains(".") ){
		        	
		        	
		        	Matcher fNMatcher = Pattern.compile("(.*?)\\.").matcher(dataPath);
		        	if( fNMatcher.find() ){
			        	String fieldName = fNMatcher.group(1);
			        	
				        FieldDescriptor fd = classCD.getFieldDescriptorByName(fieldName);
				        if( fd == null ){
				        	throw new Exception("Type not found in model");
				        }
				        
			        	ReferenceDescriptor rd = (ReferenceDescriptor) fd; 
			        	String refClassName = 
			        			TypeUtil.unqualifiedName(rd.getReferencedClassName());
			        	
			        	
			        	if( rd.relationType() == FieldDescriptor.ONE_ONE_RELATION ||
			        		rd.relationType() == FieldDescriptor.N_ONE_RELATION   )
			        	{
			        		wmd.debug("This is a reference");
			        		
				        	String xPathValue = StringUtils.strip( expr.evaluate(doc) );
				        	Item referencedItem = getRefItem(refClassName, xPathValue);
				        	
				        	wmd.debug("Setting current "+currentClass+"."+fd.getName()+" to: ("+refClassName+")["+xPathValue+"]" );
				        	item.setReference(rd.getName(), referencedItem.getIdentifier());
				        	
				        	if( 		rd.relationType() == FieldDescriptor.ONE_ONE_RELATION ){
				        		wmd.debug("1:1");
				        		// UNTESTED
				        		setRevRefIfExists(item, referencedItem, rd);
				        	}else if(	rd.relationType() == FieldDescriptor.N_ONE_RELATION){
				        		wmd.debug("N:1");
				        		addToRevColIfExists(item, referencedItem, rd);
				        	}
			        		
			        	}else if( 	rd.relationType() == FieldDescriptor.ONE_N_RELATION ||
			        				rd.relationType() == FieldDescriptor.M_N_RELATION   )
			        	{
			        		wmd.debug("This is a collection"); 
			        		CollectionDescriptor cd = (CollectionDescriptor) rd;
			        		
			        		if( 		cd.relationType() == FieldDescriptor.ONE_N_RELATION ){
			        			wmd.debug("1:N");
			        		}else if(	cd.relationType() == FieldDescriptor.M_N_RELATION   ){
			        			wmd.debug("M:N");
			        		}
			        		
			        		Item referencedItem = createItem(refClassName); // Initialized by necessity
			        		
				        	// Get set of IDs referenced
					        NodeList resultNodes = (NodeList) expr.evaluate(doc,  XPathConstants.NODESET);
					        String collectionIDs[] = new String[resultNodes.getLength()]; 
					        for(int i = 0; i < resultNodes.getLength(); i++) {
					            collectionIDs[i] = StringUtils.strip(resultNodes.item(i).getTextContent()); 
				        		referencedItem = getRefItem(refClassName, collectionIDs[i]);
				        		item.addToCollection(cd.getName(), referencedItem);
				        		
					            wmd.debug(cd.getName()+":["+collectionIDs[i]+"]");
			        		
				        		if( 		cd.relationType() == FieldDescriptor.ONE_N_RELATION ){
				        			setRevRefIfExists(item, referencedItem, cd);
				        		}else if(	cd.relationType() == FieldDescriptor.M_N_RELATION   ){
				        			wmd.debug("M:N");
				        			// UNTESTED
				        			addToRevColIfExists(item, referencedItem, rd);
				        		}
			        		
					        }
			        		
			        		
			        		
			        	}else{
			        		throw new Exception(dataPath+" contains a '.', "+
			        				"but is not a reference or collection");
			        	}
			        	


		        	}else{
		        		throw new Exception("Matching error: ["+dataPath+"] expected to contain '.'");
		        	}
		        }else{
		        	wmd.debug("This is an attribute");
		        	
		        	String xPathValue = StringUtils.strip( expr.evaluate(doc) );
		        	
			        if(dataPath.equals("primaryIdentifier")){
			        	ID = xPathValue;
			        }
			        
		        	// DataPath describes attribute
					wmd.debug("Setting attribute ["+dataPath+"] to ["+xPathValue+"]");
			        if (!StringUtils.isEmpty(xPathValue)) {
						item.setAttribute(dataPath, xPathValue);
					}
		        	
		        }
		        
		        
		        wmd.debug("=======================");
	        }
	        
	        if( ID == null ){
	        	throw new Exception("InterMine ID not defined");
	        }
	        wmd.debug("Storing "+currentClass+" with ID:"+ID);
	        store(item);
	    	
	        if(count == 100){
		        wmd.debug("STOP AFTER 100 GENES FOR TESTING");
		        break;
	        }
    	}
    	
    	wmd.debug("==== Flushing cached reference items ====");
    	// Store all items in storedRefItems
    	Iterator<Entry<String, Item>> keySetIter = 
    			storedRefItems.entrySet().iterator();
    	while(keySetIter.hasNext()){
    		Entry<String, Item> keySet = keySetIter.next();
    		wmd.debug("Storing item:["+keySet.getKey()+"]");
    		store(keySet.getValue());
    	}
    
    	fw.close();
    	
    }
    
    /**
     * Gets ID of referenced object.
     * @param fieldName The reference or collection this object is referred to in 
     * @param pID Primary ID value of referenced object
     * @return InterMine item identifier for this object
     * @throws Exception 
     */
	public Item getRefItem(String className, String pID) throws Exception {
//    	ReferenceDescriptor rd = classCD.getReferenceDescriptorByName(fieldName, true);
    	if( className == null ){
    		throw new Exception("getRefID className parameter is null");
    	}
    	if( pID == null ){
    		throw new Exception("getRefID pID parameter is null");
    	}
    	
		Item referencedItem; 
		if (storedRefItems.containsKey(className + ":" + pID)) {
			referencedItem = storedRefItems.get(className + ":"
					+ pID);
		} else {
			wmd.debug("new " + className + " object:" + pID);
			referencedItem = createItem(className);

			referencedItem.setAttribute(getClassPIDField(className), pID);
			
			storedRefItems.put(className+":"+pID, referencedItem);
		}

		return referencedItem;
	}
	
	// TODO configure two part keys
	public String getClassPIDField(String className) throws Exception{
		if (keyMapping.containsKey(className)) {
			return keyMapping.get(className);
		} 
		
		throw new Exception(
				"keyMapping hash has no \"class key value\" for "
				+ className);
	}

	/**
	 * This method is automatically called if "mapping.file" property set 
	 * for source in project XML.
	 * 
	 * Reads InterMine to AceXML data mapping configuration file
	 * @param mappingFile 
	 * @throws Exception
	 */
    public void setMappingFile(String mappingFile) throws Exception{
        dataMapping = new DataMapper();
    	try {
			dataMapping.load(new FileReader(mappingFile));
		} catch (FileNotFoundException e) {
			wmd.debug("ERROR: "+mappingFile+" not found");
			throw e;
		}
    	System.out.println("Processed mapping file: "+mappingFile);
    }
    
    /**
	 * This method is automatically called if "key.file" property set 
	 * for source in project XML.
	 * 
	 * Reads key/value file loader will use.
	 * File must be in the format
	 * 
	 * className.key = value
	 * 
	 * For example:
	 * BioEntity.key = primaryIdentifier
	 * This line will set the primaryIdentifier field as primary key
	 * for all children of BioEntity.  Precedence granted to more
	 * specific keys.
	 * 
     * @param keyFilePath
     * @throws Exception
     */
    public void setKeyFile(String keyFilePath) throws Exception{
        keyMapping = new HashMap<String, String>();
    	Properties keyFileProps = new Properties();
    	try{
	    	keyFileProps.load(new FileReader(keyFilePath));
    	}catch(Exception e){
    		System.out.println("Problem loading keyfile:["+keyFilePath+"]");
    		e.printStackTrace();
    		throw e;
    	}
    	Enumeration keyEnum = keyFileProps.keys();
    	while( keyEnum.hasMoreElements() ){
    		String key = (String) keyEnum.nextElement();
    		int index = key.indexOf(".key");
			if(index > 0){
    			keyMapping.put( key.substring(0, index), 
    							keyFileProps.getProperty(key));
    			
    		}
    	}
    	System.out.println("Processed key file: ["+keyFilePath+"]");
    }
    
    /**
     * Sets the reverse reference of referenced classes of 1:1 and N:1 
     * relationships.
     * @param currentItem The item whose fields are being processed.
     * @param referencedItem The item the currentItem's reference points to
     * @param rd Descriptor for currentItem's current reference being processed 
     * @param refPID The primary ID intended to be set for referencedItem
     */
    public void setRevRefIfExists(Item currentItem, Item referencedItem, 
    		ReferenceDescriptor rd){
    	ReferenceDescriptor rrd = rd.getReverseReferenceDescriptor();
		if(rrd == null){
			wmd.debug("Unidirectional, no reverse reference");
		}else{
//			wmd.debug(String.format(
//					"Setting (%s)%s.%s= current item", 
//					rd.getName(), rd.getReferencedClassName(), 
//					rrd.getName()));
			referencedItem.setReference(rrd.getName(), currentItem);
		}
    }
    
    public void addToRevColIfExists(Item currentItem, Item referencedItem, 
    		ReferenceDescriptor rd){
    	CollectionDescriptor rcd = (CollectionDescriptor) rd.getReverseReferenceDescriptor();
		if(rcd == null){
			wmd.debug("Unidirectional, no reverse reference");
		}else{
			wmd.debug(String.format(
					"Adding current item to (%s)%s.%s", 
					rd.getName(), rd.getReferencedClassName(), 
					rcd.getName()));
			referencedItem.addToCollection(rcd.getName(), currentItem);
		}

    }
    
}
