/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI stream record response.
 */
public abstract class MIStreamRecord extends MIOOBRecord {

	String cstring = ""; //$NON-NLS-1$

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
			return "~\"" + cstring + "\"\n"; //$NON-NLS-1$
		} else if (this instanceof MITargetStreamOutput) {
			return "@\"" + cstring + "\"\n"; //$NON-NLS-1$
		} else if (this instanceof MILogStreamOutput) {
			return "&\"" + cstring + "\"\n"; //$NON-NLS-1$
		}
		return  "\"" + cstring + "\"\n"; //$NON-NLS-1$
	}
}
