/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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

	public MIDataWriteMemory(long offset, String address, int wordFormat, int wordSize,
		String value) {

		super ("-data-write-memory");

		if (offset != 0) {
			setOptions(new String[] { "-o", Long.toString(offset)});
		}

		String format = "x";
		switch (wordFormat) {
			case MIFormat.UNSIGNED :
				format = "u";
				break;

			case MIFormat.FLOAT :
				format = "f";
				break;

			case MIFormat.ADDRESS :
				format = "a";
				break;

			case MIFormat.INSTRUCTION :
				format = "i";
				break;

			case MIFormat.CHAR :
				format = "c";
				break;

			case MIFormat.STRING :
				format = "s";
				break;

			case MIFormat.DECIMAL :
				format = "d";
				break;

			case MIFormat.BINARY :
				format = "t";
				break;

			case MIFormat.OCTAL :
				format = "o";
				break;

			case MIFormat.HEXADECIMAL :
			default :
				format = "x";
				break;
		}

		setParameters(new String[] {address, format, Integer.toString(wordSize), value});
	}

}
