/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * 
 * Supports the retrieval of formatted blocks of memory.
 * 
 * @since Jul 31, 2002
 */
public interface IFormattedMemoryRetrieval
{
	int[] getSupportedFormats() throws DebugException;
	
	IFormattedMemoryBlock getFormattedMemoryBlock( long startAddress,
												   int format,
												   int wordSize,
												   int numberOfRows,
												   int numberOfColumns ) throws DebugException;

	IFormattedMemoryBlock getFormattedMemoryBlock( long startAddress,
												   int format,
												   int wordSize,
												   int numberOfRows,
												   int numberOfColumns, 
												   char paddingChar ) throws DebugException;
}
