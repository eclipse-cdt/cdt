/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI var-list-children
 * -var-list-children var2
 *  ^done,numchild="6",children={child={name="var2.0",exp="0",numchild="0",type="char"},child={name="var2.1",exp="1",numchild="0",type="char"},child={name="var2.2",exp="2",numchild="0",type="char"},child={name="var2.3",exp="3",numchild="0",type="char"},child={name="var2.4",exp="4",numchild="0",type="char"},child={name="var2.5",exp="5",numchild="0",type="char"}}
 *
 */
public class MIVarListChildrenInfo extends MIInfo {

	MIChild[] children;
	int numchild;

	public MIVarListChildrenInfo(MIOutput record) {
		super(record);
		parse();
	}

	public MIChild[] getChildren() {
		return children;
	}

	void parse() {
		List aList = new ArrayList();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();

					if (var.equals("numchild")) {
						if (value instanceof MIConst) {
							String str = ((MIConst)value).getString();
							try {
								numchild = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("children")) {
						if (value instanceof MITuple) {
							parseChildren((MITuple)value, aList);
						}
					}
				}
			}
		}
		children = (MIChild[])aList.toArray(new MIChild[aList.size()]);
	}

	void parseChildren(MITuple tuple, List aList) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("child")) {
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					aList.add(new MIChild((MITuple)value));
				}
			}
		}
	}
}
