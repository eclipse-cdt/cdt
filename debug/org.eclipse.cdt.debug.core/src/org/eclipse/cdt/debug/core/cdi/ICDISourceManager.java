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
	 * Set the source search paths for the debug session.
	 * @param String array of search paths
	 */
	void addSourcePaths(String[] srcPaths) throws CDIException;

	/**
	 * Return the array of source paths
	 * @return String array of search paths.
	 */
	String[] getSourcePaths() throws CDIException;

	/**
	 * Set the shared library search paths for the debu session.
	 * @param String
	 */
	void addLibraryPaths(String[] libPaths) throws CDIException;

	/**
	 * Return the array of shared libraries search paths
	 * @return String array of search paths.
	 */
	String[] getLibraryPaths() throws CDIException;

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

}
