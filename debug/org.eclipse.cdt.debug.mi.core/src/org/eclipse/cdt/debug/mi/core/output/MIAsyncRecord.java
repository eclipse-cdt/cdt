package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public abstract class MIAsyncRecord extends MIOOBRecord {

	final static MIResult[] nullResults = new MIResult[0];

	MIResult[] results = null;
	String asynClass = "";
	int token = -1;

	public int getToken() {
		return token;
	}

	public void setToken(int t) {
		token = t;
	}

	public String getAsyncClass() {
		return asynClass;
	}

	public void setAsyncClass(String a) {
		asynClass = a;
	}

	public MIResult[] getMIResults() {
		if (results == null) {
			return nullResults;
		}
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}
}
