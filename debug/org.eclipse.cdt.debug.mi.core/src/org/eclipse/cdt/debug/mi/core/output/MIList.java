/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI list semantic.
 */
public class MIList extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	final static MIValue[] nullValues = new MIValue[0];

	MIResult[] results = nullResults;;
	MIValue[] values = nullValues;

	public MIResult[] getMIResults() {
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public MIValue[] getMIValues() {
		return values;
	}

	public void setMIValues(MIValue[] vals) {
		values = vals;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		for (int i = 0; i < results.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(results[i].toString());
		}
		for (int i = 0; i < values.length; i++) {
			if (i != 0) {
				buffer.append(',');
			}
			buffer.append(values[i].toString());
		}
		buffer.append(']');
		return buffer.toString();
	}
}
