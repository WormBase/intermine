package org.intermine.bio.dataconversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingFileKey {

	private Pattern paransB4XPath;
	private String castType = null;
	private String dataPath = null;
	private String rawKey	= null;
	private boolean forcedBool = false;
	
	/**
	 * This class is used as a key in the hash representing mapping files.
	 * @param mappingFileKey
	 */
	public MappingFileKey(String mappingFileKey) {
		rawKey = mappingFileKey;
		
		// Is this field type casted?
		// (castType) xpath
		paransB4XPath  = Pattern.compile("\\((.*)\\)\\s*");
        Matcher typeCastMatcher = paransB4XPath.matcher(mappingFileKey);
     	if(typeCastMatcher.find()){
    		String matchedText = typeCastMatcher.group(1);
    		if( matchedText != null && matchedText.length()!=0 ){
    			castType = matchedText;
    		}
	     	dataPath = mappingFileKey.substring(typeCastMatcher.end());
    	}else{
    		dataPath = mappingFileKey;
    	}
     	
     	// Is this field forced boolean?
     	// if.xpath
     	Pattern strB4Dot = Pattern.compile("(.*?)\\.(.*)");
     	Matcher fNMatcher = strB4Dot.matcher(dataPath);
     	if( fNMatcher.find() ){
	        String prefix = fNMatcher.group(1);
	        if(prefix.equalsIgnoreCase("if")){
	        	dataPath = fNMatcher.group(2);
	        	forcedBool = true;
	        }else{
//	        	wmd.debug("prefix '"+prefix+"' ignored on '"+dataPath+"'");
	        	dataPath = fNMatcher.group(2);
	        	
	        }
     		
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
	
	public String getDataPath(){
		return dataPath;
	}
	
	public String getRawKey(){
		return rawKey;
	}
	
	public boolean isForcedBool(){
		return forcedBool;
	}
	
	public boolean equals(MappingFileKey m){
		if(this.getRawKey() == m.getRawKey()){
			return true;
		}else{
			return false;
		}
	}

}
