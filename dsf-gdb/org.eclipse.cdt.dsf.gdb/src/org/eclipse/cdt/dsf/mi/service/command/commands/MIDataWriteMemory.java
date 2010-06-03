/*******************************************************************************
 * Copyright (c) 2000, 2009, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson Communication - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.MIFormat;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * -data-write-memory [ -o COLUMN_OFFSET ] 
 *   	ADDRESS WORD-FORMAT WORD-SIZE VALUE
 *  
 * where:
 * 
 * 'COLUMN_OFFSET'
 *   	The cell offset from the beginning of the memory grid row
 *    
 * 'ADDRESS'
 *   	Row address of the cell to be written
 *   
 * 'WORD-FORMAT'
 *      The format to be used to print the memory words
 * 
 * 'WORD-SIZE'
 *      The size of each memory word in bytes
 *
 * 'VALUE'
 * 		The value to be written into the cell
 *
 * Writes VALUE into ADDRESS + (COLUMN_OFFSET * WORD_SIZE).
 *
 */
public class MIDataWriteMemory extends MICommand<MIDataWriteMemoryInfo> {

	public MIDataWriteMemory(
	        IDMContext ctx, 
			long offset,
			String address,
			int wordFormat,
			int wordSize,
			String value)
	{
		super(ctx, "-data-write-memory"); //$NON-NLS-1$

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

		setParameters(
			new String[] {
				address,
				format,
				Integer.toString(wordSize),
				value});
	}

    @Override
    public MIDataWriteMemoryInfo getResult(MIOutput out)  {
        return new MIDataWriteMemoryInfo(out);
    }
}
