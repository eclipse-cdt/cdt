/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * 
 * A contiguos segment of memory in an execution context represented 
 * as a table of values.
 * 
 * @since Jul 31, 2002
 */
public interface IFormattedMemoryBlock extends IDebugElement
{
	/**
	 * Returns the start address of this memory block.
	 * 
	 * @return the start address of this memory block
	 */
	long getStartAddress();

	/**
	 * Returns the format of the memory words of this block.
	 * 
	 * @return The format of the memory words of this block
	 */
	int getFormat();
	
	/**
	 * Returns the size of each memory word in bytes.
	 * 
	 * @return the size of each memory word in bytes
	 */
	int getWordSize();
	
	/**
	 * Returns the number of rows in the output table.
	 * 
	 * @return the number of rows in the output table
	 */
	int getNumberOfRows();
	
	/**
	 * Returns the number of columns in the output table.
	 * 
	 * @return the number of columns in the output table
	 */
	int getNumberOfColumns();

	/**
	 * Returns whether each row should include an ASCII dump.
	 * 
	 * @return whether each row should include an ASCII dump
	 */
	boolean displayASCII();
	
	/**
	 * Returns the array of rows.
	 * 
	 * @return the array of rows
	 */
	IFormattedMemoryBlockRow[] getRows();

	long nextRowAddress();
	
	long previousRowAddress();
	
	long nextPageAddress();
	
	long previousPageAddress();

	void reformat( long startAddress,
				   int format,
				   int wordSize,
				   int numberOfRows,
				   int numberOfColumns ) throws DebugException;

	void reformat( long startAddress,
				   int format,
				   int wordSize,
				   int numberOfRows,
				   int numberOfColumns,
				   char paddingChar ) throws DebugException;
}
