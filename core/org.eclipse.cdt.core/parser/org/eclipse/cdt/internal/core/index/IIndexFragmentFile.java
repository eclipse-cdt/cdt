/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentFile extends IIndexFile {
	IIndexFragmentFile[] EMPTY_ARRAY = {};

	/**
	 * Returns the fragment that owns this file.
	 */
	IIndexFragment getIndexFragment();

	/**
	 * Sets the timestamp of the file.
	 */
	void setTimestamp(long timestamp) throws CoreException;

	/**
	 * Sets the file read time.
	 */
	void setSourceReadTime(long time) throws CoreException;

	/**
	 * Sets the hash of the file content.
	 */
	void setContentsHash(long hash) throws CoreException;

	/**
	 * Returns the hash-code computed by combining the file size and the file encoding.
	 * @return a hash-code or {@code 0} if it is unknown.
	 */
	int getSizeAndEncodingHashcode() throws CoreException;

	/**
	 * Sets the hash-code computed by combining the file size and the file encoding.
	 * @param hashcode a hash-code or {@code 0} if it is unknown.
	 */
	void setSizeAndEncodingHashcode(int hashcode) throws CoreException;

	/**
	 * Sets the flag that determines whether the file is a header with {@code #pragma once}
	 * statement or an include guard, or it is a source file and parsed only once because of that.
	 */
	void setPragmaOnceSemantics(boolean value) throws CoreException;

	/**
	 * Sets the name of the replacement header.
	 * @param replacementHeader the name of the replacement header, may be {@code null} or an empty
	 *     string
	 * @since 5.7
	 */
	void setReplacementHeader(String replacementHeader) throws CoreException;

	/**
	 * Returns whether this file contains content in its
	 * associated fragment. Files without content are inserted to track includes.
	 */
	boolean hasContent() throws CoreException;

	/**
	 * Checks if the file contains at least one unresolved include.
	 * @return {@code true} if the file contains an unresolved include
	 */
	boolean hasUnresolvedInclude() throws CoreException;

	/**
	 * Returns the id of the linkage this file belongs to.
	 */
	@Override
	int getLinkageID() throws CoreException;

	/**
	 * Changes the inclusions pointing to 'source' to point to this file, instead.
	 * The file 'source' must belong to the same fragment as this file.
	 */
	void transferIncluders(IIndexFragmentFile source) throws CoreException;

	/**
	 * Changes the inclusion from the context of 'source' to point to this file, instead.
	 * The file 'source' must belong to the same fragment as this file.
	 */
	void transferContext(IIndexFragmentFile source) throws CoreException;
}
