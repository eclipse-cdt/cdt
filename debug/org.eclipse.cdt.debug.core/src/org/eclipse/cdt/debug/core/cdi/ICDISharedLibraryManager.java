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
	 * load symbols for the specified shared libraries.
	 * 
	 * @return the array of loaded shared libraries
	 * @throws CDIException on failure. Reasons include:
	 */
	void loadSymbols(ICDISharedLibrary[] libs) throws CDIException;

	/**
	 * load symbols of all the shared libs.
	 * 
	 * @return the array of loaded shared libraries
	 * @throws CDIException on failure. Reasons include:
	 */
	void loadSymbols() throws CDIException;

}
