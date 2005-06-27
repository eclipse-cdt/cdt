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

import org.eclipse.cdt.debug.mi.core.MIFormat;

/**
 * 
 *    -data-write-memory [-o COLUMN_OFFSET] ADDR FORMAT WORD-SIZE VALUE.");
 *
 * where:
 * 
 * DATA-MEMORY-WRITE:
 *
 *   COLUMN_OFFSET: optional argument. Must be preceeded by '-o'. The
 *   offset from the beginning of the memory grid row where the cell to
 *   be written is.
 *   ADDR: start address of the row in the memory grid where the memory
 *   cell is, if OFFSET_COLUMN is specified. Otherwise, the address of
 *   the location to write to.
 *   FORMAT: a char indicating format for the ``word''. See 
 *   the ``x'' command.
 *   WORD_SIZE: size of each ``word''; 1,2,4, or 8 bytes
 *   VALUE: value to be written into the memory address.
 *
 *   Writes VALUE into ADDR + (COLUMN_OFFSET * WORD_SIZE).
 *
 *  Prints nothing.
 *
 */
public class MIDataWriteMemory extends MICommand {

	public MIDataWriteMemory(String miVersion, long offset, String address, int wordFormat, int wordSize,
		String value) {

		super (miVersion, "-data-write-memory"); //$NON-NLS-1$

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

		setParameters(new String[] {address, format, Integer.toString(wordSize), value});
	}

}
