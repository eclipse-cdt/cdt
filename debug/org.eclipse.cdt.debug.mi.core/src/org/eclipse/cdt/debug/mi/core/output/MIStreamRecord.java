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

	public String getString () {
		return MIConst.getString(getCString());
	}

	public String toString() {
		if (this instanceof MIConsoleStreamOutput) {
			return "~\"" + cstring + "\"\n";
		} else if (this instanceof MITargetStreamOutput) {
			return "@\"" + cstring + "\"\n";
		} else if (this instanceof MILogStreamOutput) {
			return "&\"" + cstring + "\"\n";
		}
		return  "\"" + cstring + "\"\n";
	}
}
