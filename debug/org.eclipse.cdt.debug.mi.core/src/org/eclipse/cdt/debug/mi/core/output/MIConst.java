package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIConst extends MIValue {
	String cstring = "";

	public String getCString() {
		return cstring;
	}

	public void setCString(String str) {
		cstring = str;
	}
}
