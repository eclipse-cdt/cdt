package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIDataListRegisterValuesInfo extends MIInfo {

	public class Register {
		int number;
		int value;
	}

	public MIDataListRegisterValuesInfo(MIResultRecord rr) {
		super(rr);
	}

	Register [] getRegistersValues () {
		return null;
	}
}
