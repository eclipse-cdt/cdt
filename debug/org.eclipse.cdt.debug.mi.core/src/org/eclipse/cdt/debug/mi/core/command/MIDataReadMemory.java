/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -data-read-memory [ -o BYTE-OFFSET ]
 *      ADDRESS WORD-FORMAT WORD-SIZE
 *      NR-ROWS NR-COLS [ ASCHAR ]
 *  
 * where:
 * 
 * `ADDRESS'
 *      An expression specifying the address of the first memory word to be
 *      read.  Complex expressions containing embedded white space should
 *      be quoted using the C convention.
 * 
 * `WORD-FORMAT'
 *      The format to be used to print the memory words.  The notation is
 *      the same as for GDB's `print' command (*note Output formats:
 *      Output Formats.).
 * 
 * `WORD-SIZE'
 *      The size of each memory word in bytes.
 * 
 * `NR-ROWS'
 *      The number of rows in the output table.
 * 
 * `NR-COLS'
 *      The number of columns in the output table.
 * 
 * `ASCHAR'
 *      If present, indicates that each row should include an ASCII dump.
 *      The value of ASCHAR is used as a padding character when a byte is
 *      not a member of the printable ASCII character set (printable ASCII
 *      characters are those whose code is between 32 and 126,
 *      inclusively).
 * 
 * `BYTE-OFFSET'
 * 
 *
 */
public class MIDataReadMemory extends MICommand 
{

	public MIDataReadMemory(String[] params) {
		super("-data-read-memory", params);
	}

	public MIDataReadMemory(String[] opts, String[] params) {
		super("-data-read-memory", opts, params);
	}
}
