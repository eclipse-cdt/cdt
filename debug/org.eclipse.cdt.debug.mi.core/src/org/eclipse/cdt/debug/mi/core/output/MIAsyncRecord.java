package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIAsyncRecord extends MIOOBRecord {

	public final static int EXEC_ASYNC = 0;
	public final static int STATUS_ASYNC = 1;
	public final static int NOTIFY_ASYNC = 2;

	final static MIResult[] nullResults = new MIResult[0];

	MIResult[] results = null;
	String asynClass = "";
	int token = -1;
	int type = 0;

	public int getToken() {
		return token;
	}

	public void setToken(int t) {
		token = t;
	}

	public int getType() {
		return type;
	}

	public void setType(int t) {
		type = t;
	}


	public String getAsyncClass() {
		return asynClass;
	}

	public void setAsyncClass(String a) {
		asynClass = a;
	}

	public MIResult[] getResults() {
		if (results == null) {
			return nullResults;
		}
		return results;
	}

	public void setResults(MIResult[] res) {
		results = res;
	}
}
