/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * A -break-list result-record is the form:
 * <pre>
 * ^done,BreakpointTable={nr_rows="1",nr_cols="6",hdr=[..],body=[brkpt={},brkpt={}]}
 *-break-list
^done,BreakpointTable={nr_rows="6",nr_cols="6",hdr=[{width="3",alignment="-1",col_name="number",colhdr="Num"},{width="14",alignment="-1",col_name="type",colhdr="State"},{width="4",alignment="-1",col_name="disp",colhdr="Disp"},{width="3",alignment="-1",col_name="enabled",colhdr="Enb"},{width="10",alignment="-1",col_name="addr",colhdr="Address"},{width="40",alignment="2",col_name="what",colhdr="What"}],body=[bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"},bkpt={number="2",type="breakpoint",disp="del",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"},bkpt={number="3",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",cond="1",times="0"},bkpt={number="4",type="hw breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"},bkpt={number="5",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="0",thread="0",times="0"},bkpt={number="6",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="1",thread="1",times="0"}]}
 * </pre>
 */
public class MIBreakListInfo extends MIInfo {

	MIBreakpoint[] breakpoints;

	public MIBreakListInfo(MIOutput rr) {
		super(rr);
	}

	public MIBreakpoint[] getMIBreakpoints() {
		if (breakpoints == null) {
			parse();
		}
		return breakpoints;
	}

	void parse() {
		List<MIBreakpoint> aList = new ArrayList<>(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("BreakpointTable")) { //$NON-NLS-1$
						parseTable(results[i].getMIValue(), aList);
					}
				}
			}
		}
		breakpoints = aList.toArray(new MIBreakpoint[aList.size()]);
	}

	void parseTable(MIValue val, List<MIBreakpoint> aList) {
		if (val instanceof MITuple) {
			MIResult[] table = ((MITuple) val).getMIResults();
			for (int j = 0; j < table.length; j++) {
				String variable = table[j].getVariable();
				if (variable.equals("body")) { //$NON-NLS-1$
					parseBody(table[j].getMIValue(), aList);
				}
			}
		}
	}

	void parseBody(MIValue body, List<MIBreakpoint> aList) {
		if (body instanceof MIList) {
			MIResult[] bkpts = ((MIList) body).getMIResults();
			for (int i = 0; i < bkpts.length; i++) {
				String b = bkpts[i].getVariable();
				if (b.equals("bkpt")) { //$NON-NLS-1$
					MIValue value = bkpts[i].getMIValue();
					if (value instanceof MITuple) {
						aList.add(createMIBreakpoint((MITuple) value));
					}
				}
			}
		}
	}

	/**
	 * Create a target specific MIBreakpoint
	 *
	 * @param value
	 *            tuple suitable for passing to MIBreakpoint constructor
	 * @return new breakpoint
	 * @since 5.3
	 */
	protected MIBreakpoint createMIBreakpoint(MITuple tuple) {
		return new MIBreakpoint(tuple);
	}
}
