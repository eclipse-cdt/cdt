package org.eclipse.cdt.debug.mi.core.output;



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

	public MIBreakListInfo(MIOutput rr) {
		super(rr);
	}

	BreakPoint[] getBreakPoints() {
		return null;
	}
}
