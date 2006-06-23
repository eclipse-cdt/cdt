/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.output;

import java.util.ArrayList;
import java.util.List;


/**
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x08048468",func="main",file="hello.c",line="4",times="0"}
 * -break-insert -a p
 * ^done,hw-awpt={number="2",exp="p"}
 * -break-watch -r p
 * ^done,hw-rwpt={number="4",exp="p"}
 * -break-watch p
 * ^done,wpt={number="6",exp="p"}
 */
public class MIBreakInsertInfo extends MIInfo {

	MIBreakpoint[] breakpoints;

	void parse() {
		List aList = new ArrayList(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue val = results[i].getMIValue();
					MIBreakpoint bpt = null;
					if (var.equals("wpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = new MIBreakpoint((MITuple)val);
							bpt.setEnabled(true);
							bpt.setWriteWatchpoint(true);
						}
					} else if (var.equals("bkpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = new MIBreakpoint((MITuple)val);
							bpt.setEnabled(true);
						}
					} else if (var.equals("hw-awpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = new MIBreakpoint((MITuple)val);
							bpt.setAccessWatchpoint(true);
							bpt.setEnabled(true);
						}
					} else if (var.equals("hw-rwpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = new MIBreakpoint((MITuple)val);
							bpt.setReadWatchpoint(true);
							bpt.setEnabled(true);
						}
					}
					if (bpt != null) {
						aList.add(bpt);
					}
				}
			}
		}
		breakpoints = (MIBreakpoint[])aList.toArray(new MIBreakpoint[aList.size()]);
	}

	public MIBreakInsertInfo(MIOutput record) {
		super(record);
	}

	public MIBreakpoint[] getMIBreakpoints() {
		if (breakpoints == null) {
			parse();
		}
		return breakpoints;
	}
}
