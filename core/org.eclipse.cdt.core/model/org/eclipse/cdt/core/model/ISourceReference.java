/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Common protocol for C elements that have associated source code.
 * <p>
 * Note: For {@code IBinary}, {@code IArchive} and other members
 * derived from a binary type, the implementation returns source iff the element
 * has attached source code and debugging information.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISourceReference {
	/**
	 * Returns the source code associated with this element.
	 * <p>
	 * For binary files, this returns the source of the entire translation unit
	 * associated with the binary file (if there is one).
	 *
	 * @return the source code, or {@code null} if this element has no
	 *     associated source code
	 * @exception CModelException if this element does not exist or if an
	 *     exception occurs while accessing its corresponding resource
	 */
	String getSource() throws CModelException;

	/**
	 * Returns the source range associated with this element.
	 * <p>
	 * For binary files, this returns the range of the entire translation unit
	 * associated with the binary file (if there is one).
	 *
	 * @return the source range, or {@code null} if if this element has no
	 *     associated source code
	 * @exception CModelException if this element does not exist or if an
	 *     exception occurs while accessing its corresponding resource
	 */
	ISourceRange getSourceRange() throws CModelException;

	/**
	 * Returns the translation unit in which this member is declared, or {@code null}
	 * if this member is not declared in a translation unit (for example, a binary type).
	 */
	ITranslationUnit getTranslationUnit();

	/**
	 * Returns whether this element is in active code. Code is inactive when it is hidden
	 * by conditional compilation.
	 * @since 5.1
	 */
	public boolean isActive();

	/**
	 * Allows to differentiate otherwise equal elements of the same file.
	 * @since 5.1
	 */
	int getIndex();
}
