package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIBreakpointHitInfo extends MIInfo {

	public MIBreakpointHitInfo(MIOutput record) {
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
