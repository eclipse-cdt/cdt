package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIVarUpdateInfo extends MIInfo {

	public class Change {
		String name;
		boolean inScope;
		boolean changed;
	}

	public MIVarUpdateInfo(MIOutput record) {
		super(record);
	}

	public Change[] getChangeList () {
		return null;
	}
}
