package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIDataListRegistersNamesInfo extends MIInfo {

	public MIDataListRegistersNamesInfo(MIResultRecord rr) {
		super(rr);
	}

	String[] getRegistersNames () {
		return null;
	}
}
