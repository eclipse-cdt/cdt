/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

/**
 * 
 * Represents a row in the output table of formatted memory block.
 * 
 * @since Jul 31, 2002
 */
public interface IFormattedMemoryBlockRow
{
	/**
	 * Returns the address of this row.
	 * 
	 * @return the address of this row
	 */
	long getAddress();

	/**
	 * Returns the array of memory words.
	 * 
	 * @return the array of memory words
	 */
	String[] getData();
	
	/**
	 * Returns the ASCII dump for this row.
	 * 
	 * @return the ASCII dump for this row
	 */
	String getASCII();
}
