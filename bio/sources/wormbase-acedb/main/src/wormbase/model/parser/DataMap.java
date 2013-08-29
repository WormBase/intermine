/**
 * 
 */
package wormbase.model.parser;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.metadata.Model;


/**
 * @author jwong
 * Handles data mapping between AcdDB XML and InterMine with a properties file
 */
public class DataMap extends Properties {

	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	
	private Vector<String> attributes;
	private Vector<String> references;
	private Vector<String> collections;
    private ClassDescriptor classCD; // CD of current data type being processed 
	
	public DataMap(String mappingFile, ClassDescriptor classCD) throws FileNotFoundException, IOException {
		super();
		this.load(new FileReader(mappingFile));
		
		this.classCD = classCD;
		categorizeKeys();
	}

	/**
	 * @param defaults
	 */
	public DataMap(Properties defaults) {
		super(defaults);
		
	}
	
	public void load(Reader reader) throws IOException{
		super.load(reader);
	}

	private void categorizeKeys(){
		Enumeration<Object> keys = this.keys();
		while( keys.hasMoreElements() ){
        	String fieldName = (String) keys.nextElement();
        	FieldDescriptor fd = classCD.getFieldDescriptorByName(fieldName);
        	if(fd.isAttribute()){
        		attributes.add(fieldName);
        	}else if(fd.isCollection()){
        		collections.add(fieldName);
        	}else if(fd.isReference()){
        		references.add(fieldName);
        	}
		}
		
	}
	
	public String[] getAttributes(){
		return attributes.toArray(new String[]{});
	}
	
	public String[] getReferences(){
		return references.toArray(new String[]{});
	}
	
	public String[] getCollections(){
		return collections.toArray(new String[]{});
	}
}
