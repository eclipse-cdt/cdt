package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIStackListLocalsInfo extends MIInfo {

	public class Local {
		String name;
		String value;
	}

	public MIStackListLocalsInfo(MIOutput out) {
		super(out);
	}

	public Local[] getLocals() {
		return null;
	}
}
