package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIInfo {

	MIResultRecord resultRecord;

	public MIInfo(MIResultRecord record) {
		resultRecord = record;
	}

	MIResultRecord getResultRecord () {
		return resultRecord;
	}
}
