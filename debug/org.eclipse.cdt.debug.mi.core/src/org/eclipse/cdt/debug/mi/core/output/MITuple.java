package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MITuple extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	MIResult[] results = null;

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
