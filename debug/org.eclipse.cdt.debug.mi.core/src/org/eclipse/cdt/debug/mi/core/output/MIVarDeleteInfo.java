package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIVarDeleteInfo extends MIInfo {

	public MIVarDeleteInfo(MIOutput record) {
		super(record);
	}

	public int getNumberDeleted () {
		return 0;
	}
}
