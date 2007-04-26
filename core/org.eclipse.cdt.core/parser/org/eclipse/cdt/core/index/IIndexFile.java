/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.core.runtime.CoreException;

/**
 * Represents a file that has been indexed.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexFile {
	/**
	 * Returns an IIndexFileLocation representing the location of this file
	 * @return an IIndexFileLocation representing the location of this file
	 * @throws CoreException
	 */
	IIndexFileLocation getLocation() throws CoreException;

	/**
	 * Returns all includes found in this file.
	 * @return an array of all includes found in this file
	 * @throws CoreException
	 */
	IIndexInclude[] getIncludes() throws CoreException;
	
	/**
	 * Returns all macros defined in this file.
	 * @return an array of macros found in this file
	 * @throws CoreException
	 */
	IIndexMacro[] getMacros() throws CoreException;
	
	/**
	 * Last modification of file before it was indexed.
	 * @return the last modification date of the file at the time it was parsed.
	 * @throws CoreException 
	 */
	long getTimestamp() throws CoreException;

	/**
	 * Returns the hashcode of the scanner configuration that was used to parse the file.
	 * <code>0</code> will be returned in case the hashcode is unknown.
	 * @return the hashcode of the scanner configuration or <code>0</code>.
	 * @throws CoreException 
	 */
	int getScannerConfigurationHashcode() throws CoreException;

	/**
	 * Find all names within the given range.
	 */
	IIndexName[] findNames(int offset, int length) throws CoreException;
}
