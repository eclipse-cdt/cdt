/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB - Modified for new DSF Reference Implementation
 *     John Dallaway - Accept word size bigger than 1 (Bug 341762)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.MIFormat;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -data-read-memory [ -o BYTE-OFFSET ]
 *      ADDRESS WORD-FORMAT WORD-SIZE
 *      NR-ROWS NR-COLS [ ASCHAR ]
 *  
 * where:
 * 
 * 'ADDRESS'
 *      An expression specifying the address of the first memory word to be
 *      read.  Complex expressions containing embedded white space should
 *      be quoted using the C convention.
 * 
 * 'WORD-FORMAT'
 *      The format to be used to print the memory words.  The notation is
 *      the same as for GDB's `print' command (*note Output formats:
 *      Output Formats.).
 * 
 * 'WORD-SIZE'
 *      The size of each memory word in bytes.
 * 
 * 'NR-ROWS'
 *      The number of rows in the output table.
 * 
 * 'NR-COLS'
 *      The number of columns in the output table.
 * 
 * 'ASCHAR'
 *      If present, indicates that each row should include an ASCII dump.
 *      The value of ASCHAR is used as a padding character when a byte is
 *      not a member of the printable ASCII character set (printable ASCII
 *      characters are those whose code is between 32 and 126,
 *      inclusively).
 * 
 * 'BYTE-OFFSET'
 * 		An offset to add to ADDRESS before fetching the memory.
 *
 */
public class MIDataReadMemory extends MICommand<MIDataReadMemoryInfo> {
	
	int fword_size;

	public MIDataReadMemory(
	        IDMContext ctx, 
			long offset,
			String address,
			int word_format,
			int word_size,
			int rows,
			int cols,
			Character asChar)
	{
		super(ctx, "-data-read-memory"); //$NON-NLS-1$

		// Save this for the result parser
		fword_size = word_size;

		if (offset != 0) {
			setOptions(new String[] { "-o", Long.toString(offset)}); //$NON-NLS-1$
		}

		String format = "x"; //$NON-NLS-1$
		switch (word_format) {
			case MIFormat.UNSIGNED :
				format = "u"; //$NON-NLS-1$
				break;

			case MIFormat.FLOAT :
				format = "f"; //$NON-NLS-1$
				break;

			case MIFormat.ADDRESS :
				format = "a"; //$NON-NLS-1$
				break;

			case MIFormat.INSTRUCTION :
				format = "i"; //$NON-NLS-1$
				break;

			case MIFormat.CHAR :
				format = "c"; //$NON-NLS-1$
				break;

			case MIFormat.STRING :
				format = "s"; //$NON-NLS-1$
				break;

			case MIFormat.DECIMAL :
			case MIFormat.NATURAL :
				format = "d"; //$NON-NLS-1$
				break;

			case MIFormat.BINARY :
				format = "t"; //$NON-NLS-1$
				break;

			case MIFormat.OCTAL :
				format = "o"; //$NON-NLS-1$
				break;

			case MIFormat.HEXADECIMAL :
			case MIFormat.RAW :
			default :
				format = "x"; //$NON-NLS-1$
				break;
		}

		if (asChar == null) {
			setParameters(
				new String[] {
					address,
					format,
					Integer.toString(word_size), 
					Integer.toString(rows),
					Integer.toString(cols)});
		} else {
			setParameters(
				new String[] {
					address,
					format,
					Integer.toString(word_size),
					Integer.toString(rows),
					Integer.toString(cols),
					asChar.toString()});
		}
	}

    @Override
    public MIDataReadMemoryInfo getResult(MIOutput out)  {
        return new MIDataReadMemoryInfo(out, fword_size);
    }
}
