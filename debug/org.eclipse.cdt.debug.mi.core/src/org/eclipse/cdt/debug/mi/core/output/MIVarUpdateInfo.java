/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI var-update.
 * -var-update *
 * ^done,changelist={name="var3",in_scope="true",type_changed="false",name="var2",in_scope="true",type_changed="false"}
 */
public class MIVarUpdateInfo extends MIInfo {

	public class Change {
		String name;
		boolean inScope;
		boolean changed;
	}

	MIChange[] changeList;

	public MIVarUpdateInfo(MIOutput record) {
		super(record);
		parse();
	}

	public MIChange[] getChangeList () {
		return changeList;
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
					if (var.equals("changelist")) {
						MIValue value = results[i].getMIValue();
						if (value instanceof MITuple) {
							parseChangeList((MITuple)value, aList);
						}
					}
				}
			}
		}
		changeList = (MIChange[])aList.toArray(new MIChange[aList.size()]);
	}

	void parseChangeList(MITuple tuple, List aList) {
		MIResult[] results = tuple.getMIResults();
		MIChange change = null;
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value instanceof MIConst) {
				str = ((MIConst)value).getString();
			}
			if (var.equals("name")) {
				change = new MIChange(str);
				aList.add(change);
			} else if (var.equals("in_scope")) {
				if (change != null) {
					change.setInScope("true".equals(str));
				}
			} else if (var.equals("type_changed")) {
				if (change != null) {
					change.setChanged("true".equals(str));
				}
			}
		}
	}
}
