/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.sourcelookup;

/**
 * Defines constatnts and methods to set the source presentation mode.
 * 
 * @since: Oct 8, 2002
 */
public interface ISourceMode
{
	static final public int MODE_SOURCE = 0;
	static final public int MODE_DISASSEMBLY = 1;
	static final public int MODE_MIXED = 2;

	/**
	 * Returns the current source presentation mode.
	 * 
	 * @return the current source presentation mode
	 */
	int getMode();

	/**
	 * Sets the source presentation mode.
	 * 
	 * @param the source presentation mode to set
	 */
	void setMode( int mode );	
}
