package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIEnvironmentPWDInfo extends MIInfo {

	public MIEnvironmentPWDInfo(MIOutput o) {
		super(o);
	}

	public String getWorkingDirectory() {
		return ".";
	}
}
