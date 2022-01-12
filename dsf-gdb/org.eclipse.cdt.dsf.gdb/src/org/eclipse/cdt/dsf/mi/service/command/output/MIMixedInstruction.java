/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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
 *     Ericsson - Adapted for DSF
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;

public class MIMixedInstruction implements IMixedInstruction {

	// The parsed information
	private String fileName = ""; //$NON-NLS-1$
	private String fullName = ""; //$NON-NLS-1$
	private int lineNumber = 0;
	private MIInstruction[] assemblyCode;

	public MIMixedInstruction(MITuple tuple) {
		parse(tuple);
	}

	@Override
	public String getFileName() {
		String result = getFullName();
		return result.isEmpty() ? fileName : result;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public IInstruction[] getInstructions() {
		return assemblyCode;
	}

	/**
	 *  Parse the mixed instruction result. It has the following 3 fields:
	 *
	 *      line="31",
	 *      file="/dir1/dir2/basics.c",
	 *      line_asm_insn=[
	 *          {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
	 *          {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
	 *          ...,
	 *          {address="0x00010820",func-name="main",offset="100",inst="restore "}
	 *          ]
	 */
	private void parse(MITuple tuple) {
		List<MIInstruction> instructions = new ArrayList<>();
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$

			if (value != null && value instanceof MIConst) {
				str = ((MIConst) value).getCString();
			}

			if (var.equals("line")) { //$NON-NLS-1$
				try {
					lineNumber = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
				continue;
			}

			if (var.equals("file")) { //$NON-NLS-1$
				fileName = str;
				continue;
			}

			if (var.equals("fullname")) { //$NON-NLS-1$
				fullName = str;
				continue;
			}

			if (var.equals("line_asm_insn")) { //$NON-NLS-1$
				if (value instanceof MIList) {
					MIList list = (MIList) value;
					MIValue[] values = list.getMIValues();
					for (int j = 0; j < values.length; j++) {
						if (values[j] instanceof MITuple) {
							instructions.add(new MIInstruction((MITuple) values[j]));
						}
					}
				}
			}
		}
		assemblyCode = instructions.toArray(new MIInstruction[instructions.size()]);

	}

	/**
	 * Get full source path as reported by gdb-mi "fullname" attribute.
	 * @since 4.4
	 */
	public String getFullName() {
		return fullName;
	}
}
