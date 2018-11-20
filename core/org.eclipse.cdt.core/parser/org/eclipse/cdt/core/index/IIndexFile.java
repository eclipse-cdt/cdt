/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IFileNomination;
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
public interface IIndexFile extends IFileNomination {
	IIndexFile[] EMPTY_FILE_ARRAY = {};

	/**
	 * Returns an IIndexFileLocation representing the location of this file
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
	 * Time when the file was read during indexing. Corresponds to the start of reading.
	 * @return time of indexing in milliseconds since epoch
	 * @throws CoreException
	 * @since 5.4
	 */
	long getSourceReadTime() throws CoreException;

	/**
	 * Hash of the file contents when the file was indexed.
	 * @return 64-bit hash of the file content.
	 * @throws CoreException
	 * @since 5.2
	 */
	long getContentsHash() throws CoreException;

	/**
	 * @deprecated Returns 0.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	int getScannerConfigurationHashcode() throws CoreException;

	/**
	 * @since 5.3
	 * @deprecated Returns 0.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	int getEncodingHashcode() throws CoreException;

	/**
	 * Find all names within the given range.
	 */
	IIndexName[] findNames(int offset, int length) throws CoreException;

	/**
	 * Returns the include that was used to parse this file, may be <code>null</code>.
	 */
	IIndexInclude getParsedInContext() throws CoreException;

	/**
	 * Returns the id of the linkage this file was parsed in.
	 * @since 5.0
	 */
	int getLinkageID() throws CoreException;

	/**
	 * Returns the name of the replacement header obtained from <code>@headername{header}</code> or
	 * from {@code IWYU pragma: private, include "header"}. Returns an empty string if the file
	 * contained {@code IWYU pragma: private} without a replacement header. Returns {@code null} if
	 * the file does not contain <code>@headername{header}</code> or {@code IWYU pragma: private}.
	 * @since 5.7
	 */
	String getReplacementHeader() throws CoreException;

	/**
	 * Returns detailed information about the file. For debugging only.
	 * @since 5.4
	 */
	String toDebugString();
}
