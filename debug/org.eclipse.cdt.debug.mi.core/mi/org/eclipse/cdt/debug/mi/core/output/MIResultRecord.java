/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI ResultRecord.
 */
public class MIResultRecord {

	public final static String DONE ="done"; //$NON-NLS-1$
	public final static String RUNNING ="running"; //$NON-NLS-1$
	public final static String CONNECTED ="connected"; //$NON-NLS-1$
	public final static String ERROR ="error"; //$NON-NLS-1$
	public final static String EXIT ="exit"; //$NON-NLS-1$

	static final MIResult[] nullResults = new MIResult[0];
	MIResult[] results = nullResults;
	String resultClass = ""; //$NON-NLS-1$
	int token = -1;

	public int getToken() {
		return token;
	}

	public void setToken(int t) {
		token = t;
	}

	/**
	 */
	public String getResultClass() {
		return resultClass;
	}

	public void setResultClass(String type) {
		resultClass = type;
	}

	public MIResult[] getMIResults() {
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(token).append('^').append(resultClass);
		for (int i = 0; i < results.length; i++) {
			buffer.append(',').append(results[i].toString());
		}
		return buffer.toString();
	}
}
