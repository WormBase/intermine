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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.*;



import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.*;
import org.intermine.metadata.*;
import org.intermine.util.TypeUtil;
import org.intermine.xml.full.Item;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import net.sf.saxon.s9api.*;


import wormbase.model.parser.*;

/**
 * Mapping file format:
 * primaryIdentifier = /Variation/text()[1]
 * 
 * if.naturalVariant = /XPATH/...
 * returns true if xpath returns any nodes at all
 * 
 * type casting allowed
 * (Phenotype)parents		= /XPATH/...
 * 
 * @author
 */
public class WormbaseAcedbConverter extends BioFileConverter
{
    
	private String currentClass = null; 
	private String rejectFilePath = null;
	private String keyFilePath = null;
	private String classPID = null;

	// Overridden by setDataSet()
	private static final String DATASET_TITLE = "WormBaseAcedbConverter"; //"Add DataSet.title here";
    private static final String DATA_SOURCE_NAME = "AceDB XML"; //"Add DataSource.name here";

    private WMDebug wmd;
    private Model model;
    private ClassDescriptor classCD; // CD of current data type being processed 
	private  XPathResolver aceOracle;
	

    // Items that have already been referenced and stored
    // Key: "className:id", Value: Item ID (ex: "WBGene12345")
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
        wmd.off(); // turn on for debug output // TODO toggle switch
        
        wmd.debug("Constructor called");
        
        storedRefItems = new HashMap<String, Item>();
        model = _model;
        
        
    }

    /**
     * 
     * @param reader The java.io.BufferedReader that intermine passes in
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
    	wmd.debug("started WormbaseAcedbConverter.process()"); 
    	
    	// Checking for properties
    	if( aceOracle == null )
    		throw new Exception("mapping.file property not defined for this"+
    				" source in the project.xml");
    	
    	if( keyMapping == null )
    		throw new Exception("key.file property not defined for this"+
    				" source in the project.xml");
    	
    	if( currentClass == null )
    		throw new Exception("source.class property not defined for this"+
    				" source in the project.xml");
    	classPID = getClassPIDField(classCD.getSimpleName());
    	
    	if( rejectFilePath == null )
    	{
			wmd.debug("rejects.file property not set, rejected XML"+
					" elements will be discarded");
    	}else{
    		aceOracle.setRejectFile(rejectFilePath);
    		wmd.log("XML rejects file set to:"+rejectFilePath);
    	}

		FileParser fp = new FileParser(reader);
    	
        
	        
    	// foreach XML string
    	String xmlChunk;
    	int count=0; // 
    	while( (xmlChunk = fp.getDataString()) != null ){
		
    		count++;
    		wmd.debug("###========== NEW OBJECT ==========###");
    		
//    		if(count < 1070){
//    			System.out.println(String.valueOf(count)+":"+xmlChunk.length());
//    			continue;
//    		}
    		
    		try{
    			aceOracle.setXML(xmlChunk);
    		}catch(IOException e){
    			continue;
    		}
    		
		
	        Item item = createItem(currentClass);
	        wmd.debug("New IMID: "+item.getIdentifier());
	        
	        MappingFileKey[] fields = aceOracle.getMappingFileFields(); 
	        
	        String ID = null;
	        String castType = null;
	        MappingFileKey propKey;
	        boolean firstPass = true;
	        
	        // ===================== my place
	        
	        // foreach mapping file key, extra first loop adds the PID
	        // Same PID added twice, overwrites self
	        for(int i=-1; i < fields.length; i++){
	        	if(i == -1){ // Set PID first no matter what
	        		propKey = aceOracle.getMappingFileKey(getClassPIDField(classCD.getSimpleName()));
	        		if(propKey == null)
	        			throw new Exception(String.format(
	        				"ERROR: %s set as the primary key for %s according to keyfile (%s), but cannot be found", 
	        				getClassPIDField(classCD.getSimpleName()),
	        				classCD.getSimpleName(), 
	        				keyFilePath));
	        		wmd.debug("first pass: setting key to "+propKey.getDataPath()); 
	        	}else{
	        		propKey = fields[i];
	        	}
	        	
	        	
			    String fieldName = propKey.getDataPath();
	        	wmd.debug("fieldname="+fieldName);
	        	
	        	wmd.debug("Retrieving:["+propKey.getRawKey()+"]");
	        	String[] queryResults = aceOracle.getFieldValue(fieldName);
	        	if(queryResults.length == 0){
					wmd.debug("No results returned, moving on...");
					continue;
				}

		        FieldDescriptor fd = classCD.getFieldDescriptorByName(fieldName);
		        if( fd == null ){
		        	throw new Exception(classCD.getName()+"."+fieldName+" not found in model");
		        }
		        
		        if(fd.isAttribute()){
	        	
		        	String xPathValue = StringUtils.join(queryResults, ',');
		        	wmd.debug("xpathvalue:"+xPathValue);
			        if(fieldName.equals(getClassPIDField(classCD.getSimpleName()))){
			        	if(firstPass){
			        		ID = xPathValue;
					        // if this record's pID exists in the hash, replace the incumbent
					        if(itemHasBeenProcessed(currentClass, ID)){
					        	String existingRecordsIMID = getRefItem(currentClass, ID).getIdentifier();
					        	wmd.log("found cached stand-in record, replacing "+
					        			item.getIdentifier()+" with "+existingRecordsIMID);
					        	item.setIdentifier(existingRecordsIMID);
					        }
				        	setRefItem(currentClass, ID, item);
			        	}else{
			        		wmd.debug("=======================");
			        		continue;
			        	}
			        	
			        }
			        
		        	// DataPath describes attribute
			        if (!StringUtils.isEmpty(xPathValue)) {
						wmd.debug("Setting attribute ["+fieldName+"] to ["+xPathValue+"]");
						item.setAttribute(fieldName, xPathValue);
					}else{
						wmd.debug("ignoring attribute ["+fieldName+"], no value");
					}
		        	
		        }else{
		        	
		        	ReferenceDescriptor rd = (ReferenceDescriptor) fd; 
		        	
		        	String refClassName;
        			if(propKey.getCastType() != null){
        				refClassName = propKey.getCastType();
        			}else{
			        	refClassName = TypeUtil.unqualifiedName(rd.getReferencedClassName());
        			}
		        	
		        	
		        	if( rd.relationType() == FieldDescriptor.ONE_ONE_RELATION ||
		        		rd.relationType() == FieldDescriptor.N_ONE_RELATION   )
		        	{
	//			        wmd.debug("This is a reference");
		        		if(queryResults.length > 1) 
		        			throw new Exception("Reference "+fieldName+" returns more than one result for record "+ID);
		        		
		        		String xPathValue = queryResults[0];
			        	Item referencedItem;
		        		if(!xPathValue.isEmpty()){
	        				referencedItem = getRefItem(refClassName, xPathValue);
			        	}else{
			        		wmd.debug("ID not defined, moving on...");
			        		wmd.debug("=======================");
			        		continue;
			        	}
			        	
			        	wmd.debug("Setting current "+currentClass+"."+fd.getName()+" to: ("+refClassName+")["+xPathValue+"]" );
			        	item.setReference(rd.getName(), referencedItem.getIdentifier());
			        	
			        	if( 		rd.relationType() == FieldDescriptor.ONE_ONE_RELATION ){
	//				        		wmd.debug("1:1");
			        		setRevRefIfExists(item, referencedItem, rd);
			        	}else if(	rd.relationType() == FieldDescriptor.N_ONE_RELATION){
	//				        		wmd.debug("N:1");
			        		addToRevColIfExists(item, referencedItem, rd);
			        	}
		        		
		        	}else if( rd.isCollection() ){
	//			        		wmd.debug("This is a collection"); 
		        		CollectionDescriptor cd = (CollectionDescriptor) rd;
		        		
		        		//if(cd.relationType() == FieldDescriptor.ONE_N_RELATION ){wmd.debug("1:N");}else if(cd.relationType() == FieldDescriptor.M_N_RELATION){wmd.debug("M:N");}
		        		
		        		Item referencedItem = createItem(refClassName); // Initialized by necessity
		        		
				        for(int j = 0; j < queryResults.length; j++) {
				            
			        		if(!queryResults[j].isEmpty()){
				        		referencedItem = getRefItem(refClassName, queryResults[j]);
				        	}else{
				        		continue;
				        	}
	
			        		
			        		item.addToCollection(cd.getName(), referencedItem);
			        		
				            wmd.debug(cd.getName()+":["+queryResults[j]+"]");
		        		
			        		if( 		cd.relationType() == FieldDescriptor.ONE_N_RELATION ){
			        			setRevRefIfExists(item, referencedItem, cd);
			        		}else if(	cd.relationType() == FieldDescriptor.M_N_RELATION   ){
	//				        			wmd.debug("M:N");
			        			addToRevColIfExists(item, referencedItem, rd);
			        		}
				        }
		        	}else{
		        		throw new Exception(propKey.getDataPath()+" contains a '.', "+
		        				"but is not a reference or collection");
		        	}
		        }
        		firstPass = false;
		        wmd.debug("=======================");
	        }
	        
	        if( ID == null ){
	        	throw new Exception(getClassPIDField(classCD.getSimpleName())+
	        			" set as class ID but not defined. Record ending at line:"+fp.getCurrentLine());
	        }
//	        wmd.debug("Storing "+currentClass+" with ID:"+ID);
//	        store(item);
	        
        	setRefItem(currentClass, ID, item);
	    	
	        // TODO remove in final build
//	        if(count == 100){
//		        wmd.debug("STOP AFTER 100 RECORDS FOR TESTING");
//		        break;
//	        }
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
    
		if(rejectFilePath != null)
			aceOracle.closeRejectsFile();
    	
    }
    
    /**
     * Gets ID of referenced object if exists.  It it doesn't exist, creates it
     * and returns ID of newly created object.
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
	
	/**
	 * Stores item in working buffer, overwriting any existing pairs.  Buffer
	 * is flushed once all items are dealt with.
	 * @param className
	 * @param pID
	 * @param item
	 * @throws Exception
	 */
	public void setRefItem(String className, String pID, Item item) throws Exception{
    	if( className == null ){
    		throw new Exception("getRefID className parameter is null");
    	}
    	if( pID == null ){
    		throw new Exception("getRefID pID parameter is null");
    	}
    	storedRefItems.put(className+":"+pID, item);
	}
	
	public boolean itemHasBeenProcessed(String className, String pID) throws Exception {
//    	ReferenceDescriptor rd = classCD.getReferenceDescriptorByName(fieldName, true);
    	if( className == null ){
    		throw new Exception("getRefID className parameter is null");
    	}
    	if( pID == null ){
    		throw new Exception("getRefID pID parameter is null");
    	}
    	
		Item referencedItem; 
		if (storedRefItems.containsKey(className + ":" + pID)) {
			return true;
		} else {
			return false;
		}
	}
	
	// TODO configure two part keys
	public String getClassPIDField(String className) throws Exception{
		if (keyMapping.containsKey(className)) {
			return keyMapping.get(className);
		} 
		
		throw new Exception(
				"keyMapping hash has no \"class key value\" for "
				+ className + ". Add a " + className + ".key property in "
				+ keyFilePath);
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
    	aceOracle = new XPathResolver(wmd, mappingFile);
    	
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
    	this.keyFilePath = keyFilePath; 
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
	 * This method is automatically called if "key.file" property set 
	 * for source in project XML.
	 * 
	*/
    public void setDataType(String dataType){
    	
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
//			wmd.debug("Unidirectional, no reverse reference");
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
//			wmd.debug("Unidirectional, no reverse reference");
		}else{
//			wmd.debug(String.format(
//					"Adding current item to (%s)%s.%s", 
//					rd.getName(), rd.getReferencedClassName(), 
//					rcd.getName()));
			referencedItem.addToCollection(rcd.getName(), currentItem);
		}

    }
    
    public void setSourceClass(String sourceClass){
    	currentClass = sourceClass;
        
        classCD = model.getClassDescriptorByName(currentClass); 
        
        System.out.println("current class: "+currentClass);
        System.out.println("class descriptor: "+classCD.toString());
    }
    
    public void setRejectsFile(String rejectsFile){
    	rejectFilePath = rejectsFile;
    }
    
    public void setDebug(String debug){
    	if(debug.equalsIgnoreCase("true")){
    		wmd.on();
    		wmd.log("debug: on");
    	}else{
    		wmd.log("debug: off");
    	}
    }
    
    /**
     * Sets the dataset for this instance 
     * @param datasource
     */
    public void setDataSet(String dataSet){
    	System.out.println("DataSet set to:"+dataSet); // DELETE
    	String dataSourceRefID = getDataSource(DATA_SOURCE_NAME);
        String dataSetRefID = getDataSet(dataSet, dataSourceRefID);
    	
    	BioStoreHook hook = (BioStoreHook) this.storeHook;
    	hook.setDataSet(dataSetRefID);
    }
    
 }
