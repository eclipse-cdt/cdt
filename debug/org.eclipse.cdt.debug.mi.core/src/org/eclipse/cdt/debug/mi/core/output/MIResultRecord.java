package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIResultRecord {

	public final static String DONE ="done";
	public final static String RUNNING ="running";
	public final static String CONNECTED ="connected";
	public final static String ERROR ="error";
	public final static String EXIT ="exit";

	static final MIResult[] nullResults = new MIResult[0];
	MIResult[] results = null;
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
