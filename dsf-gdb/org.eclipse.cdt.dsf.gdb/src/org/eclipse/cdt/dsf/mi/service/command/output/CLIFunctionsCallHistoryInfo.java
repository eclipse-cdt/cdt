/*******************************************************************************
 * Copyright (c) 2021 Trande UG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Trande UG - Added Functions Call History support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;

/**
 * 'info record' returns the selected reverse trace method.
 *
 * sample output:
 *
 * (gdb) record function-call-history /ilc
 * ~ "id"	"tab" "2x level spaces" "function name" "tab" "inst" "start,stop" "tab" "at" "source"
 *
 * @since 6.5
 */

public class CLIFunctionsCallHistoryInfo extends MIInfo {

	private ReverseDebugMethod fReverseMethod;

	public CLIFunctionsCallHistoryInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] records = out.getMIOOBRecords();
			StringBuilder builder = new StringBuilder();
			for (MIOOBRecord rec : records) {
				if (rec instanceof MIConsoleStreamOutput) {
					MIStreamRecord o = (MIStreamRecord) rec;
					builder.append(o.getString());
				}
			}
			parseFunctionsCallHistoryRecord(builder.toString());
		}
	}

	protected void parseFunctionsCallHistoryRecord(String output) {
		if (output.contains("Processor")) { //$NON-NLS-1$
			fReverseMethod = ReverseDebugMethod.PROCESSOR_TRACE;
		} else if (output.contains("Branch")) { //$NON-NLS-1$
			fReverseMethod = ReverseDebugMethod.BRANCH_TRACE;
		} else if (output.contains("full")) { //$NON-NLS-1$
			fReverseMethod = ReverseDebugMethod.SOFTWARE;
		} else {
			fReverseMethod = ReverseDebugMethod.OFF;
		}
	}

	public ReverseDebugMethod getReverseMethod() {
		return fReverseMethod;
	}
}