package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIBreakListInfo extends MIInfo {

	public class BreakPoint {
		int number;
		String type;
		String disposition;
		boolean enabled;
		int address;
		String what;
		int times;
	}

	public MIBreakListInfo(MIResultRecord rr) {
		super(rr);
	}

	int getCount() {
		return 0;
	}

	BreakPoint[] getBreakPoints() {
		return null;
	}
}
