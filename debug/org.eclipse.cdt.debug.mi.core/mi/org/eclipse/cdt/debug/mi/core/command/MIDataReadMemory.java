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
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

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
public class MIDataReadMemory extends MICommand {

	public MIDataReadMemory(
		long offset,
		String address,
		int wordFormat,
		int wordSize,
		int rows,
		int cols,
		Character asChar) {
		super("-data-read-memory"); //$NON-NLS-1$
		if (offset != 0) {
			setOptions(new String[] { "-o", Long.toString(offset)}); //$NON-NLS-1$
		}

		String format = "x"; //$NON-NLS-1$
		switch (wordFormat) {
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
				format = "d"; //$NON-NLS-1$
				break;

			case MIFormat.BINARY :
				format = "t"; //$NON-NLS-1$
				break;

			case MIFormat.OCTAL :
				format = "o"; //$NON-NLS-1$
				break;

			case MIFormat.HEXADECIMAL :
			default :
				format = "x"; //$NON-NLS-1$
				break;
		}

		if (asChar == null) {
			setParameters(
				new String[] {
					address,
					format,
					Integer.toString(wordSize),
					Integer.toString(rows),
					Integer.toString(cols)});
		} else {
			setParameters(
				new String[] {
					address,
					format,
					Integer.toString(wordSize),
					Integer.toString(rows),
					Integer.toString(cols),
					asChar.toString()});
		}
	}

	public MIDataReadMemoryInfo getMIDataReadMemoryInfo() throws MIException {
		return (MIDataReadMemoryInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataReadMemoryInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
