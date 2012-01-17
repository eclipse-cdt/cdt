/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Daniel Thomas (Broadcom corp.) - Added support for mode 2 and 3 (Bug 357073)
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
 *  - 0 disassembly
 *  - 1 mixed source and disassembly
 *  - 2 disassembly with raw opcodes
 *  - 3 mixed source and disassembly with raw opcodes
 *  Note: Modes 2 and 3 are only available starting with GDB 7.3
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
 *manipulated directly by GDB/MI, i.e. it is not possible to adjust its
 *format.
 *
 *
 */
public class MIDataDisassemble extends MICommand 
{
	private static final int MIN_MODE = 0;
	private static final int MAX_MODE = 3;
	private static final String MODE_OUT_OF_RANGE = "Mode out of range: "; //$NON-NLS-1$

	public MIDataDisassemble(String miVersion, String start, String end, boolean mode) {
		this(miVersion, start, end, mode ? 1 : 0);
	}

	/** @since 7.2 */
	public MIDataDisassemble(String miVersion, String start, String end, int mode) {
		super(miVersion, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[]{"-s", start, "-e", end}); //$NON-NLS-1$ //$NON-NLS-2$
		
        if (mode >= MIN_MODE && mode <= MAX_MODE) {
        	setParameters(new String[] { Integer.toString(mode) });
        } else {
        	throw new IllegalArgumentException(MODE_OUT_OF_RANGE + mode);
        }
	}

	public MIDataDisassemble(String miVersion, String file, int linenum, int lines, boolean mode) {
		this(miVersion, file, linenum, lines, mode ? 1 : 0);
	}
	
	/** @since 7.2 */
	public MIDataDisassemble(String miVersion, String file, int linenum, int lines, int mode) {
		super(miVersion, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[]{"-f", file, "-l", //$NON-NLS-1$ //$NON-NLS-2$
			 Integer.toString(linenum), "-n", Integer.toString(lines)}); //$NON-NLS-1$
		
		if (mode >= MIN_MODE && mode <= MAX_MODE) {
        	setParameters(new String[] { Integer.toString(mode) });
        } else {
        	throw new IllegalArgumentException(MODE_OUT_OF_RANGE + mode);
        }
	}

	public MIDataDisassembleInfo getMIDataDisassembleInfo() throws MIException {
		return (MIDataDisassembleInfo)getMIInfo();
	}

	@Override
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
	@Override
	protected String parametersToString() {
		String[] parameters = getParameters();
		if (parameters != null && parameters.length > 0) {
			return "-- " + parameters[0]; //$NON-NLS-1$
		}
		return new String();
	}

}
