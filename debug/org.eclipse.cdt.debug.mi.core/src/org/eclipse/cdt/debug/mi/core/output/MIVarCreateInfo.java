package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIVarCreateInfo extends MIInfo {

	public MIVarCreateInfo(MIOutput record) {
		super(record);
	}

	public String getName () {
		return "";
	}

	public int getChildNumber() {
		return 0;
	}

	public String getType() {
		return "";
	}
}
