/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI ResultRecord.
 */
public class MIResultRecord {

	public final static String DONE ="done";
	public final static String RUNNING ="running";
	public final static String CONNECTED ="connected";
	public final static String ERROR ="error";
	public final static String EXIT ="exit";

	static final MIResult[] nullResults = new MIResult[0];
	MIResult[] results = nullResults;
	String resultClass = "";
	int token = -1;

	public int geToken() {
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
