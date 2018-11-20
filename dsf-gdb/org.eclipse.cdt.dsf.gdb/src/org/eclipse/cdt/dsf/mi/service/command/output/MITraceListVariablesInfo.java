/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * -trace-list-variables
 *
 * ^done,trace-variables={nr_rows="1",nr_cols="3",
 *                        hdr=[{width="15",alignment="-1",col_name="name",colhdr="Name"},
 *                             {width="11",alignment="-1",col_name="initial",colhdr="Initial"},
 *                             {width="11",alignment="-1",col_name="current",colhdr="Current"}],
 *                        body=[variable={name="$trace_timestamp",initial="0"}
 *                              variable={name="$foo",initial="10",current="15"}]}
 *
 * @since 3.0
 */
public class MITraceListVariablesInfo extends MIInfo {

	public class MITraceVariableInfo {
		private String fName;
		private String fInitialValue;
		private String fCurrentValue;

		public String getName() {
			return fName;
		}

		public String getCurrentValue() {
			return fCurrentValue;
		}

		public String getInitialValue() {
			return fInitialValue;
		}
	}

	private MITraceVariableInfo[] fVariables;

	public MITraceListVariablesInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MITraceVariableInfo[] getTraceVariables() {
		return fVariables;
	}

	private void parse() {
		List<MITraceVariableInfo> aList = new ArrayList<>(1);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("trace-variables")) { //$NON-NLS-1$
						parseTable(results[i].getMIValue(), aList);
					}
				}
			}
		}
		fVariables = aList.toArray(new MITraceVariableInfo[aList.size()]);
	}

	private void parseTable(MIValue val, List<MITraceVariableInfo> aList) {
		if (val instanceof MITuple) {
			MIResult[] table = ((MITuple) val).getMIResults();
			for (int i = 0; i < table.length; i++) {
				String variable = table[i].getVariable();
				if (variable.equals("body")) { //$NON-NLS-1$
					parseBody(table[i].getMIValue(), aList);
				}
			}
		}
	}

	private void parseBody(MIValue body, List<MITraceVariableInfo> aList) {
		if (body instanceof MIList) {
			MIResult[] vars = ((MIList) body).getMIResults();
			for (int i = 0; i < vars.length; i++) {
				String variable = vars[i].getVariable();
				if (variable.equals("variable")) { //$NON-NLS-1$
					parseVariable(vars[i].getMIValue(), aList);
				}
			}
		}
	}

	private void parseVariable(MIValue variable, List<MITraceVariableInfo> aList) {
		if (variable instanceof MITuple) {
			MIResult[] results = ((MITuple) variable).getMIResults();
			MITraceVariableInfo info = new MITraceVariableInfo();
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();
				String str = ""; //$NON-NLS-1$
				if (value != null && value instanceof MIConst) {
					str = ((MIConst) value).getCString();
				}

				if (var.equals("name")) { //$NON-NLS-1$
					info.fName = str;
				} else if (var.equals("initial")) { //$NON-NLS-1$
					info.fInitialValue = str;
				} else if (var.equals("current")) { //$NON-NLS-1$
					info.fCurrentValue = str;
				}
			}
			aList.add(info);
		}
	}
}
