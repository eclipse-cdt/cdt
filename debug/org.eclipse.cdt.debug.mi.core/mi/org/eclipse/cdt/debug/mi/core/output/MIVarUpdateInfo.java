/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI var-update.
 * -var-update *
 * ^done,changelist={name="var3",in_scope="true",type_changed="false",name="var2",in_scope="true",type_changed="false"}
 */
public class MIVarUpdateInfo extends MIInfo {

	MIVarChange[] changeList;

	public MIVarUpdateInfo(MIOutput record) {
		super(record);
		parse();
	}

	public MIVarChange[] getMIVarChanges() {
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
					if (var.equals("changelist")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MITuple) {
							parseChangeList((MITuple)value, aList);
						}
					}
				}
			}
		}
		changeList = (MIVarChange[])aList.toArray(new MIVarChange[aList.size()]);
	}

	void parseChangeList(MITuple tuple, List aList) {
		MIResult[] results = tuple.getMIResults();
		MIVarChange change = null;
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value instanceof MIConst) {
				str = ((MIConst)value).getString();
			}
			if (var.equals("name")) { //$NON-NLS-1$
				change = new MIVarChange(str);
				aList.add(change);
			} else if (var.equals("in_scope")) { //$NON-NLS-1$
				if (change != null) {
					change.setInScope("true".equals(str)); //$NON-NLS-1$
				}
			} else if (var.equals("type_changed")) { //$NON-NLS-1$
				if (change != null) {
					change.setChanged("true".equals(str)); //$NON-NLS-1$
				}
			}
		}
	}
}
