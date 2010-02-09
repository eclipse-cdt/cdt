/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson             - Created version for Mac OS
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output.macos;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIList;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResultRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarChange;

/**
 * GDB/MI var-update for Mac OS.
 * -var-update *
 * ^done,changelist=[varobj={name="var1",in_scope="true",type_changed="false"}],time={.....}
 * 
 * @since 3.0
 */
public class MacOSMIVarUpdateInfo extends MIInfo {

	MIVarChange[] changeList;

	public MacOSMIVarUpdateInfo(MIOutput record) {
		super(record);
        List<MIVarChange> aList = new ArrayList<MIVarChange>();
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
                        } else if (value instanceof MIList) {
                            parseChangeList((MIList)value, aList);
                        }
                    }
                }
            }
        }
        changeList = aList.toArray(new MIVarChange[aList.size()]);
	}

	public MIVarChange[] getMIVarChanges() {
		return changeList;
	}

	/**
	 * For MI2 the format is now a MIList.
	 * @param tuple
	 * @param aList
	 */
	void parseChangeList(MIList miList, List<MIVarChange> aList) {
		// The MIList in Apple gdb contains MIResults instead of MIValues. It looks like:
		// ^done,changelist=[varobj={name="var1",in_scope="true",type_changed="false"}],time={.....}
		// Bug 250037
		MIResult[] results = miList.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			if (var.equals("varobj")) { //$NON-NLS-1$
				MIValue value = results[i].getMIValue();
				if (value instanceof MITuple) {
					parseChangeList((MITuple)value, aList);
				} else if (value instanceof MIList) {
					parseChangeList((MIList)value, aList);
				}
			}
		}
	} 
	
	void parseChangeList(MITuple tuple, List<MIVarChange> aList) {
		MIResult[] results = tuple.getMIResults();
		MIVarChange change = null;
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			if (value instanceof MITuple) {
				parseChangeList((MITuple)value, aList);
			}
			else
			{
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}
				if (var.equals("name")) { //$NON-NLS-1$
					change = new MIVarChange(str);
					aList.add(change);
				} else if (var.equals("value")) { //$NON-NLS-1$
					if (change != null) {
						change.setValue(str);
					}
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
}
