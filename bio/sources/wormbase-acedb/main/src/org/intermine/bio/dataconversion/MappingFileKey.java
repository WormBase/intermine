package org.intermine.bio.dataconversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingFileKey {

	private Pattern paransB4XPath;
	private String castType = null;
	private String field = null;
	private String rawKey	= null;
	private boolean forcedBool = false;
	private boolean hasSubField = false; // true if field stores value(s) in linked object
	private String subField = null;
	
	/**
	 * This class is used as a key in the hash representing mapping files.
	 * @param mappingFileKey
	 */
	public MappingFileKey(String mappingFileKey) {
		rawKey = mappingFileKey;
		String unparsedKey = mappingFileKey;
		
		// Is this field type casted?
		// (castType) field
		paransB4XPath  = Pattern.compile("\\((.*)\\)\\s*");
        Matcher typeCastMatcher = paransB4XPath.matcher(mappingFileKey);
     	if(typeCastMatcher.find()){
    		String matchedText = typeCastMatcher.group(1);
    		if( matchedText != null && matchedText.length()!=0 ){
    			castType = matchedText;
    		}
	     	field = mappingFileKey.substring(typeCastMatcher.end());
    	}else{
    		field = mappingFileKey;
    	}
     	
     	// Is this field forced boolean?
     	// if.xpath
     	String boolTypeRegex = "^\\s*if\\.";
     	if(Pattern.compile(boolTypeRegex).matcher(field).find()){
     		field = field.replaceFirst(boolTypeRegex,"");
     		forcedBool = true;
     	}

     	// Is this field forced boolean?
     	// if.xpath
     	Pattern strB4Dot = Pattern.compile("(.*?)\\.(.*)");
     	Matcher fNMatcher = strB4Dot.matcher(field);
     	if( fNMatcher.find() ){
	        field = fNMatcher.group(1);
        	hasSubField = true;
        	subField = fNMatcher.group(2);
     	}
}
	
    /**
     * Gets cast type from property key if exists aka: (Gene)Datapath
     * @param propKey
     * @return cast type, null if nonexistent
     */
	public String getCastType(){
		return castType;
	}
	
	public String getField(){
		return field;
	}
	
	public String getRawKey(){
		return rawKey;
	}
	
	public boolean isForcedBool(){
		return forcedBool;
	}
	
	public boolean hasSubField(){
		return hasSubField;
	}
	
	public String getSubField(){
		return subField;
	}
	
	public boolean equals(MappingFileKey m){
		if(this.getRawKey() == m.getRawKey()){
			return true;
		}else{
			return false;
		}
	}

}
