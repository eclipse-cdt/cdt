package org.eclipse.cdt.debug.mi.core.output;

/**
 */
public class MIList extends MIValue {

	final static MIResult[] nullResults = new MIResult[0];
	final static MIValue[] nullValues = new MIValue[0];

	MIResult[] results;
	MIValue[] values;

	public MIResult[] getMIResults() {
		if (results == null) {
			return nullResults;
		}
		return results;
	}

	public void setMIResults(MIResult[] res) {
		results = res;
	}

	public MIValue[] getMIValues() {
		if (values == null) {
			return nullValues;
		}
		return values;
	}

	public void setMIValues(MIValue[] vals) {
		values = vals;
	}
}
