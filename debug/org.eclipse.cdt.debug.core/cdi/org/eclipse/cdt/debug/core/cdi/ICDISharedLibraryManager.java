/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
}
