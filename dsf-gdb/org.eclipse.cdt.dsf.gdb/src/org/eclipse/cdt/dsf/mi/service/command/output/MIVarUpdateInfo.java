/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Anton Gorenkov - DSF-GDB should properly handle variable type change (based on RTTI) (Bug 376901)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI var-update.
 * -var-update *
 * ^done,changelist={name="var3",value="3",in_scope="true",type_changed="false",name="var2",value="4",in_scope="true",type_changed="false"}
 */
public class MIVarUpdateInfo extends MIInfo {

	MIVarChange[] changeList;

	public MIVarUpdateInfo(MIOutput record) {
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
	 * @since 3.0
	 */
	protected void parseChangeList(MIList miList, List<MIVarChange> aList) {
		MIValue[] values = miList.getMIValues();
		for (int i = 0; i < values.length; ++i) {
			if (values[i] instanceof MITuple) {
				parseChangeList((MITuple)values[i], aList);
			} else if (values[i] instanceof MIList) {
				parseChangeList((MIList)values[i], aList);
			}
		}
	} 
	
	/**
	 * @since 3.0
	 */
	protected void parseChangeList(MITuple tuple, List<MIVarChange> aList) {
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
				} else if (var.equals("new_type")) { //$NON-NLS-1$
					if (change != null) {
						change.setNewType(str);
					}
				} else if (var.equals("new_num_children")) { //$NON-NLS-1$
					if (change != null) {
						try {
							change.setNewNumChildren(Integer.parseInt(str.trim()));
						} catch (NumberFormatException e) {
							change.setNewNumChildren(0);
						}
					}
				} else if (var.equals("dynamic")) { //$NON-NLS-1$
					if (change != null) {
						change.setDynamic(str.trim().equals("1")); //$NON-NLS-1$
					}
				} else if (var.equals("has_more")) { //$NON-NLS-1$
					if (change != null) {
						change.setHasMore(str.trim().equals("1")); //$NON-NLS-1$
					}
				} else if (var.equals("new_children")) { //$NON-NLS-1$
					if (change != null) {
						List<MIVar> newChildren = new ArrayList<MIVar>();
						parseNewChildren(value, newChildren);
						change.setNewChildren(newChildren.toArray(new MIVar[newChildren.size()]));
					}
				} else if (var.equals("displayhint")) { //$NON-NLS-1$
					if (change != null) {
		            	change.setDisplayHint(new MIDisplayHint(str));
					}
				}
			}
		}
	}
	
	private void parseNewChildren(MIValue value, List<MIVar> aList) {
		if (value instanceof MIList) {
			MIValue[] children = ((MIList)value).getMIValues();
			
			for (MIValue child : children) {
				if (child instanceof MITuple) {
					aList.add(new MIVar((MITuple)child));
				}
			}
		}
	}
}
