package org.eclipse.cdt.debug.mi.core.output;



/**
 */
public class MIVarListChildrenInfo extends MIInfo {

	public class Children {
		String name;
		int numchild;
		String type;
	}

	public MIVarListChildrenInfo(MIOutput record) {
		super(record);
	}

	public Children[] getChildren() {
		return null;
	}
}
