package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIDataListChangedRegistersInfo extends MIInfo {

	public MIDataListChangedRegistersInfo(MIResultRecord rr) {
		super(rr);
	}

	int [] getRegisters () {
		return null;
	}
}
