/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

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
	public MIDataDisassemble(String[] params) {
		super("-data-disassemble", params);
	}

	public MIDataDisassemble(String[] opts, String[] params) {
		super("-data-disassemble", opts, params);
	}
}
