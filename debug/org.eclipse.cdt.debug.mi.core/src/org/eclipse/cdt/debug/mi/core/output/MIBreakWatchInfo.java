package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIBreakWatchInfo extends MIInfo {

	public MIBreakWatchInfo(MIOutput rr) {
		super(rr);
	}

	public int getNumber () {
		return 0;
	}

	public String getExpression() {
		return null;
	}
}
