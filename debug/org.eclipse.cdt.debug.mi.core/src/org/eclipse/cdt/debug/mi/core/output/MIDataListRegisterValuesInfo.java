package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIDataListRegisterValuesInfo extends MIInfo {

	public class Register {
		int number;
		int value;
	}

	public MIDataListRegisterValuesInfo(MIOutput rr) {
		super(rr);
	}

	Register [] getRegistersValues () {
		return null;
	}
}
