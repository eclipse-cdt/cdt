/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI tuple value.
 */
public class MITuple extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	MIResult[] results = nullResults;

	public MIResult[] getMIResults() {
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		for (int i = 0; i < results.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(results[i].toString());
		}
		buffer.append('}');
		return buffer.toString();
	}
}
