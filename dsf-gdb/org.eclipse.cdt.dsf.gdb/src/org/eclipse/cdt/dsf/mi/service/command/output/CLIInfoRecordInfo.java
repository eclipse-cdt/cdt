/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;

/**
 * 'info record' returns the selected reverse trace method.
 *
 * sample output:
 *
 * (gdb) info record
 * ~ Active record target: record-btrace
 * ~ Recording format: Branch Trace Store.
 * ~ Buffer size: 64kB.
 * ~ Recorded 0 instructions in 0 functions (0 gaps) for thread 1 (process 24645).
 *
 * @since 5.0
 */

public class CLIInfoRecordInfo extends MIInfo {

	private ReverseDebugMethod fReverseMethod;

	public CLIInfoRecordInfo(MIOutput record) {
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
			parseReverseMethod(builder.toString());
		}
	}

	protected void parseReverseMethod(String output) {
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
