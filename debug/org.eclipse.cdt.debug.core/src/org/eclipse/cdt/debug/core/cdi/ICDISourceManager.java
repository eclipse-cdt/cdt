/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi;

import java.io.File;

/**
 * 
 * Maintains the list of directories to search for source files.
 * 
 * @since Jul 9, 2002
 */
public interface ICDISourceManager extends ICDISessionObject
{
	/**
	 * Returns an array of directories. Returns the empty array 
	 * if the source path is empty.
	 * 
	 * @return an array of directories
	 * @throws CDIException on failure. Reasons include:
	 */
	File[] getDirectories() throws CDIException;
	
	/**
	 * Sets the source path according to the given array of directories.
	 * 
	 * @param directories - the array of directories
	 * @throws CDIException on failure. Reasons include:
	 */
	void set( File[] directories ) throws CDIException;
	
	/**
	 * Reset the source path to empty.
	 * 
	 * @throws CDIException on failure. Reasons include:
	 */
	void reset() throws CDIException;
	
	/**
	 * Returns a ICDILocation
	 */
	ICDILocation createLocation(String file, String function, int line);
}
