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
	public static final int MEMORY_SIZE_BYTE = 1;
	public static final int MEMORY_SIZE_HALF_WORD = 2;
	public static final int MEMORY_SIZE_WORD = 4;
	public static final int MEMORY_SIZE_DOUBLE_WORD = 8;
	public static final int MEMORY_SIZE_FLOAT = 8;
	public static final int MEMORY_SIZE_DOUBLE_FLOAT = 16;

	public static final int MEMORY_FORMAT_HEX = 0;
	public static final int MEMORY_FORMAT_BINARY = 1;
	public static final int MEMORY_FORMAT_OCTAL = 2;
	public static final int MEMORY_FORMAT_SIGNED_DECIMAL = 3;
	public static final int MEMORY_FORMAT_UNSIGNED_DECIMAL = 4;

	public static final int MEMORY_BYTES_PER_ROW_4 = 4;
	public static final int MEMORY_BYTES_PER_ROW_8 = 8;
	public static final int MEMORY_BYTES_PER_ROW_16 = 16;
	public static final int MEMORY_BYTES_PER_ROW_32 = 32;
	public static final int MEMORY_BYTES_PER_ROW_64 = 64;
	public static final int MEMORY_BYTES_PER_ROW_128 = 128;

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
