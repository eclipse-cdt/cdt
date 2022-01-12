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

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for an include directive stored in the index.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface IIndexInclude {
	IIndexInclude[] EMPTY_INCLUDES_ARRAY = {};

	/**
	 * Returns the file that contains this directive.
	 * @return the file performing the include
	 * @throws CoreException
	 */
	IIndexFile getIncludedBy() throws CoreException;

	/**
	 * Returns the IIndexFileLocation of the file that contains this directive.
	 * @return the IIndexFileLocation of the file performing the include
	 * @throws CoreException
	 */
	IIndexFileLocation getIncludedByLocation() throws CoreException;

	/**
	 * Returns the IIndexFileLocation of the file that is included by this
	 * directive. In case of an unresolved include {@code null}
	 * will be returned.
	 *
	 * @return the IIndexFileLocation of the file that is included by this directive or {@code null}
	 *     if the include is unresolved or inactive
	 * @throws CoreException
	 */
	IIndexFileLocation getIncludesLocation() throws CoreException;

	/**
	 * Returns the simple name of the directive. This skips any leading
	 * directories. E.g. for {@code <sys/types.h>} {@code "types.h"} will be returned.
	 * @throws CoreException
	 */
	String getName() throws CoreException;

	/**
	 * Returns the name of the include. The name does not include the enclosing quotes
	 * or angle brackets. E.g. for {@code <sys/types.h>} {@code "sys/types.h"} will be returned.
	 * @throws CoreException
	 * @since 5.1
	 */
	String getFullName() throws CoreException;

	/**
	 * Returns the character offset of the name of the include in its source file.
	 * The name does not include the enclosing quotes or angle brackets.
	 * @throws CoreException
	 */
	int getNameOffset() throws CoreException;

	/**
	 * Returns the length of the name of the include. The name does
	 * not include the enclosing quotes or angle brackets.
	 * @throws CoreException
	 */
	int getNameLength() throws CoreException;

	/**
	 * Returns whether this is a system include (an include specified within angle
	 * brackets).
	 * @throws CoreException
	 */
	boolean isSystemInclude() throws CoreException;

	/**
	 * Test whether this include is in active code (not skipped by conditional preprocessing).
	 * @return whether this include is in active code
	 * @throws CoreException
	 */
	boolean isActive() throws CoreException;

	/**
	 * Test whether this include has been resolved (found in the file system).
	 * Inactive includes are not resolved, unless they constitute a hidden dependency.
	 * This is the case when an include is inactive because it has been included before:
	 * <pre>
	 *   #ifndef _header_h
	 *   #include "header.h"
	 *   #endif
	 * </pre>
	 *
	 * @return whether this is a resolved include
	 * @throws CoreException
	 */
	boolean isResolved() throws CoreException;

	/**
	 * Tests whether this include has been resolved using a heuristics rather than relying on
	 * the include search path.
	 * @since 5.1
	 */
	boolean isResolvedByHeuristics() throws CoreException;

	/**
	 * Returns {@code true} if the included file is exported by the including header.
	 * @see "https://github.com/include-what-you-use/include-what-you-use/blob/master/docs/IWYUPragmas.md"
	 * @since 5.5
	 */
	boolean isIncludedFileExported() throws CoreException;
}
