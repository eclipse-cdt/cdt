/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
 *     Ericsson  - Modified for DSF Reference Implementation
 *     Daniel Thomas (Broadcom corp.) - Added support for mode 2 and 3 (Bug 357073)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -data-disassemble
 *       [ -s START-ADDR -e END-ADDR ]
 *     | [ -f FILENAME -l LINENUM [ -n LINES ] ]
 *       -- MODE
 *
 * Where:
 *
 * '-s START-ADDR'
 *     is the beginning address (or '$pc')
 *
 * '-e END-ADDR'
 *     is the end address
 *
 * '-f FILENAME'
 *     is the name of the file to disassemble
 *
 * '-l LINENUM'
 *     is the line number to disassemble around
 *
 * -n LINES'
 *     is the the number of disassembly lines to be produced.  If it is
 *     -1, the whole function will be disassembled, in case no END-ADDR is
 *     specified.  If END-ADDR is specified as a non-zero value, and
 *     LINES is lower than the number of disassembly lines between
 *     START-ADDR and END-ADDR, only LINES lines are displayed; if LINES
 *     is higher than the number of lines between START-ADDR and
 *     END-ADDR, only the lines up to END-ADDR are displayed.
 *
 * '-- MODE'
 *     - 0 disassembly
 *     - 1 mixed source and disassembly
 *     - 2 disassembly with raw opcodes
 *     - 3 mixed source and disassembly with raw opcodes
 *     Note: Modes 2 and 3 are only available starting with GDB 7.3
 */

public class MIDataDisassemble extends MICommand<MIDataDisassembleInfo> {

	private static final int MIN_MODE = 0;

	/** @since 4.4 */
	public static final int DATA_DISASSEMBLE_MODE_DISASSEMBLY = 0;
	/** @since 4.4 */
	public static final int DATA_DISASSEMBLE_MODE_MIXED = 1;
	/** @since 4.4 */
	public static final int DATA_DISASSEMBLE_MODE_DISASSEMBLY_OPCODES = 2;
	/** @since 4.4 */
	public static final int DATA_DISASSEMBLE_MODE_MIXED_OPCODES = 3;

	private static final int MAX_MODE = 3;

	private static final String MODE_OUT_OF_RANGE = "Mode out of range: "; //$NON-NLS-1$

	public MIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, boolean mode) {
		this(ctx, start, end, mode ? DATA_DISASSEMBLE_MODE_MIXED : DATA_DISASSEMBLE_MODE_DISASSEMBLY);
	}

	/** @since 4.1 */
	public MIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, int mode) {
		super(ctx, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[] { "-s", start, "-e", end }); //$NON-NLS-1$ //$NON-NLS-2$

		if (mode >= MIN_MODE && mode <= MAX_MODE) {
			setParameters(new String[] { Integer.toString(mode) });
		} else {
			throw new IllegalArgumentException(MODE_OUT_OF_RANGE + mode);
		}
	}

	public MIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, boolean mode) {
		this(ctx, file, linenum, lines, mode ? DATA_DISASSEMBLE_MODE_MIXED : DATA_DISASSEMBLE_MODE_DISASSEMBLY);
	}

	/** @since 4.1 */
	public MIDataDisassemble(IDisassemblyDMContext ctx, String file, int linenum, int lines, int mode) {
		super(ctx, "-data-disassemble"); //$NON-NLS-1$
		setOptions(new String[] { "-f", file, "-l", //$NON-NLS-1$ //$NON-NLS-2$
				Integer.toString(linenum), "-n", Integer.toString(lines) }); //$NON-NLS-1$

		if (mode >= MIN_MODE && mode <= MAX_MODE) {
			setParameters(new String[] { Integer.toString(mode) });
		} else {
			throw new IllegalArgumentException(MODE_OUT_OF_RANGE + mode);
		}
	}

	/*
	 * -data-disassemble uses "--" as a separator with only the MODE
	 * So override the MICommand
	 */
	@Override
	protected String parametersToString() {
		String[] parameters = getParameters();
		if (parameters != null && parameters.length > 0) {
			return "-- " + parameters[0]; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public MIDataDisassembleInfo getResult(MIOutput output) {
		return new MIDataDisassembleInfo(output);
	}
}
