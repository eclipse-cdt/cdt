/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;

/**
 * 
 * Manages the collection of shared libraries in the debug session.
 * Auto update is on by default.
 * 
 * @since: Jan 15, 2003
 */
public interface ICDISharedLibraryManager extends ICDIManager {

	/**
	 * Returns the array of loaded shared libraries.
	 * 
	 * @return the array of loaded shared libraries
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDISharedLibrary[] getSharedLibraries() throws CDIException;

	/**
	 * Loads symbols for the specified shared libraries.
	 * 
	 * @return the array of loaded shared libraries
	 * @throws CDIException on failure. Reasons include:
	 */
	void loadSymbols(ICDISharedLibrary[] libs) throws CDIException;

	/**
	 * Loads symbols of all shared libraries.
	 * 
	 * @return the array of loaded shared libraries
	 * @throws CDIException on failure. Reasons include:
	 */
	void loadSymbols() throws CDIException;

	/**
	 * Returns the search paths for shared libraries.
	 * 
	 * @return the array of the search paths
	 * @throws CDIException
	 */
	String[] getSharedLibraryPaths() throws CDIException;

	/**
	 * Sets the shared libs paths to libpaths.
	 * 
	 * @param array of search paths
	 * @throws CDIException
	 */
	void setSharedLibraryPaths(String[] libpaths) throws CDIException;

	/**
	 * Sets the "automatically load symbols from shared libraries" mode.
	 *  
	 * @param set
	 * @throws CDIException
	 */
	void setAutoLoadSymbols(boolean set) throws CDIException;

	/**
	 * Returns the current autoloading mode.
	 * 
	 * @return the current autoloading mode
	 * @throws CDIException
	 */
	boolean isAutoLoadSymbols() throws CDIException;

	/**
	 * Returns whether this manager supports autoloading.
	 * 
	 * @return whether this manager supports autoloading
	 */
	boolean supportsAutoLoadSymbols();

	/**
	 * Sets the "stop on shared library events" mode.
	 * 
	 * @param set
	 * @throws CDIException
	 */
	void setStopOnSolibEvents(boolean set) throws CDIException;

	/**
	 * Returns the current mode of shared library events handling.
	 * 
	 * @return the current mode of shared library events handling
	 * @throws CDIException
	 */
	boolean isStopOnSolibEvents() throws CDIException;

	/**
	 * Returns whether this manager supports shared library events handling.
	 * 
	 * @return whether this manager supports shared library events handling
	 */
	boolean supportsStopOnSolibEvents();
}
