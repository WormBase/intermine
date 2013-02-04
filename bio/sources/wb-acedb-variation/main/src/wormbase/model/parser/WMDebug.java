/**
 * 
 */
package wormbase.model.parser;

/**
 * @author jwong
 *
 */
public class WMDebug {

	boolean debug;
	/**
	 * 
	 */
	public WMDebug() {
		debug = true;
	}

	public void on(){
		debug = true;
	}
	
	public void off(){
		debug = false;
	}
	
	/**
	 * Prints "JDJDJD:: "+msg to System.out
	 * @param msg message to print
	 */
	public void debug(String msg){
		if( debug )
			System.out.println("JDJDJD:: "+msg);
	}
}
