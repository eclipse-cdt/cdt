/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Enter type comment.
 * 
 * @since: Jan 15, 2003
 */
public interface ICSharedLibrary extends IDebugElement
{
	/**
	 * Returns the name of shared library file.
	 * 
	 * @return the name of shared library file
	 */
	String getFileName();
	
	/**
	 * Returns the start address of this library.
	 * 
	 * @return the start address of this library
	 */
	long getStartAddress();

	/**
	 * Returns the end address of this library.
	 * 
	 * @return the end address of this library
	 */
	long getEndAddress();

	/**
	 * Returns whether the symbols of this library are read.
	 *
	 * @return whether the symbols of this library are read
	 */
	boolean areSymbolsLoaded();
	
	/**
	 * Loads the library symbols.
	 * 
	 * @throws DebugException if this method fails.  Reasons include:
	 */
	void loadSymbols() throws DebugException;
	
	void dispose();
}
