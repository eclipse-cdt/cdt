/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI var-list-children
 * -var-list-children var2
 *  ^done,numchild="6",children={child={name="var2.0",exp="0",numchild="0",type="char"},child={name="var2.1",exp="1",numchild="0",type="char"},child={name="var2.2",exp="2",numchild="0",type="char"},child={name="var2.3",exp="3",numchild="0",type="char"},child={name="var2.4",exp="4",numchild="0",type="char"},child={name="var2.5",exp="5",numchild="0",type="char"}}
 *
 */
public class MIVarListChildrenInfo extends MIInfo {

	MIVar[] children;
	int numchild;
	private boolean hasMore = false;

	public MIVarListChildrenInfo(MIOutput record) {
		super(record);
		List<MIVar> aList = new ArrayList<>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();

					if (var.equals("numchild")) { //$NON-NLS-1$
						if (value instanceof MIConst) {
							String str = ((MIConst) value).getString();
							try {
								numchild = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("children")) { //$NON-NLS-1$
						parseChildren(value, aList);
					} else if (var.equals("has_more")) { //$NON-NLS-1$
						if (value instanceof MIConst) {
							String str = ((MIConst) value).getString();
							if (str.trim().equals("1")) { //$NON-NLS-1$
								hasMore = true;
							}
						}
					}
				}
			}
		}
		children = aList.toArray(new MIVar[aList.size()]);
	}

	public MIVar[] getMIVars() {
		return children;
	}

	/**
	 * @return Whether the are more children to fetch.
	 *
	 * @since 4.0
	 */
	public boolean hasMore() {
		return hasMore;
	}

	/*
	 * Some gdb MacOSX do not return a MITuple so we have
	 * to check for different format.
	 * See PR 81019
	 */
	private void parseChildren(MIValue val, List<MIVar> aList) {
		MIResult[] results = null;
		if (val instanceof MITuple) {
			results = ((MITuple) val).getMIResults();
		} else if (val instanceof MIList) {
			results = ((MIList) val).getMIResults();
		}
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				if (var.equals("child")) { //$NON-NLS-1$
					MIValue value = results[i].getMIValue();
					if (value instanceof MITuple) {
						aList.add(new MIVar((MITuple) value));
					}
				}
			}
		}
	}
}
