package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public abstract class MIStreamRecord extends MIOOBRecord {

	String cstring = "";

	public String getCString() {
		return cstring;
	} 

	public void setCString(String str) {
		cstring = str;
	} 
}
