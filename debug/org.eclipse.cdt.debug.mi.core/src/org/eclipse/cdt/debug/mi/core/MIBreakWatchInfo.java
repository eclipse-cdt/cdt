package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIBreakWatchInfo extends MIInfo {

	public MIBreakWatchInfo(MIResultRecord rr) {
		super(rr);
	}

	public int getNumber () {
		return 0;
	}

	public String getExpression() {
		return null;
	}
}
