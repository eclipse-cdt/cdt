package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;

/**
 */
public class MIBreakpointHitInfo extends MIInfo {

	public MIBreakpointHitInfo(MIResultRecord record) {
		super(record);
	}

	int getBreakNumber() {
		return 0;
	}

	String getFunction() {
		return null;
	}

	int getAddress() {
		return 0;
	}

	String getFileName() {
		return null;
	}

	int getLineNumber() {
		return 0;
	}

	String[] getArguments () {
		return null;
	}
}
