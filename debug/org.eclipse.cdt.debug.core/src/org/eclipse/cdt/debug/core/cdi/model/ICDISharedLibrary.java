/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a shared library which has been loaded into 
 * the debug target.
 * 
 * @since Jul 8, 2002
 */
public interface ICDISharedLibrary extends ICDIObject {
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
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void loadSymbols() throws CDIException;
}
