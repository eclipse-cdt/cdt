/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a file that has been indexed.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @since 4.0
 */
public interface IIndexFile {
	IIndexFile[] EMPTY_FILE_ARRAY = {};

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
	 * Returns all using directives for namespaces and global scope, found in this file.
	 * @throws CoreException 
	 * @since 5.0
	 */
	ICPPUsingDirective[] getUsingDirectives() throws CoreException;

	/**
	 * Last modification of file before it was indexed.
	 * @return the last modification date of the file at the time it was parsed.
	 * @throws CoreException 
	 */
	long getTimestamp() throws CoreException;

	/**
	 * Hash of the file contents when the file was indexed.
	 * @return 64-bit hash of the file content.
	 * @throws CoreException
	 * @since 5.2
	 */
	long getContentsHash() throws CoreException;

	/**
	 * Returns the hash-code of the scanner configuration that was used to parse the file.
	 * <code>0</code> will be returned in case the hash-code is unknown.
	 * @return the hash-code of the scanner configuration or <code>0</code>.
	 * @throws CoreException 
	 */
	int getScannerConfigurationHashcode() throws CoreException;

	/**
	 * Find all names within the given range.
	 */
	IIndexName[] findNames(int offset, int length) throws CoreException;
	
	/**
	 * Returns the include that was used to parse this file, may be null.
	 */
	IIndexInclude getParsedInContext() throws CoreException;
	
	/**
	 * Returns the id of the linkage this file was parsed in.
	 * @since 5.0
	 */
	int getLinkageID() throws CoreException;
}
