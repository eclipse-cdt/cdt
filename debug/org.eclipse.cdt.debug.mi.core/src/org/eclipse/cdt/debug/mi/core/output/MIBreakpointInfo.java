package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIBreakpointInfo extends MIInfo {

	public MIBreakpointInfo(MIOutput record) {
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
}
