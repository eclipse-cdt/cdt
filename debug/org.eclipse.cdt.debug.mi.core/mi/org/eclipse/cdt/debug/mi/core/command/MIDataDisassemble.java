/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIDataDisassembleInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -data-disassemble
 *         [ -s START-ADDR -e END-ADDR ]
 *       | [ -f FILENAME -l LINENUM [ -n LINES ] ]
 *       -- MODE
 *
 *Where:
 *
 *`START-ADDR'
 *     is the beginning address (or `$pc')
 *
 *`END-ADDR'
 *     is the end address
 *
 *`FILENAME'
 *     is the name of the file to disassemble
 *
 *`LINENUM'
 *     is the line number to disassemble around
 *
 *`LINES'
 *     is the the number of disassembly lines to be produced.  If it is
 *     -1, the whole function will be disassembled, in case no END-ADDR is
 *     specified.  If END-ADDR is specified as a non-zero value, and
 *     LINES is lower than the number of disassembly lines between
 *     START-ADDR and END-ADDR, only LINES lines are displayed; if LINES
 *     is higher than the number of lines between START-ADDR and
 *     END-ADDR, only the lines up to END-ADDR are displayed.
 *
 *`MODE'
 *     is either 0 (meaning only disassembly) or 1 (meaning mixed source
 *     and disassembly).
 *
 *Result
 *......
 *
 *   The output for each instruction is composed of four fields:
 *
 *   * Address
 *
 *   * Func-name
 *
 *   * Offset
 *
 *   * Instruction
 *
 *   Note that whatever included in the instruction field, is not
 *manipulated directely by GDB/MI, i.e. it is not possible to adjust its
 *format.
 *
 *
 */
public class MIDataDisassemble extends MICommand 
{
	public MIDataDisassemble(String miVersion, String start, String end, boolean mode) {
		super(miVersion, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[]{"-s", start, "-e", end}); //$NON-NLS-1$ //$NON-NLS-2$
		String mixed = "0"; //$NON-NLS-1$
		if (mode) {
			mixed = "1"; //$NON-NLS-1$
		}
		setParameters(new String[]{mixed});
	}

	public MIDataDisassemble(String miVersion, String file, int linenum, int lines, boolean mode) {
		super(miVersion, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[]{"-f", file, "-l", //$NON-NLS-1$ //$NON-NLS-2$
			 Integer.toString(linenum), "-n", Integer.toString(lines)}); //$NON-NLS-1$
		String mixed = "0"; //$NON-NLS-1$
		if (mode) {
			mixed = "1"; //$NON-NLS-1$
		}
		setParameters(new String[]{mixed});	
	}

	public MIDataDisassembleInfo getMIDataDisassembleInfo() throws MIException {
		return (MIDataDisassembleInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataDisassembleInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	/*
	 * GDB the -data-disassemble uses "--" as a separator wit only the MODE
	 * So override the MICommand
	 */
	protected String parametersToString() {
		if (parameters != null && parameters.length > 0) {
			return "-- " + parameters[0]; //$NON-NLS-1$
		}
		return new String();
	}

}
