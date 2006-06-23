/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Common protocol for C elements that have associated source code.
 * <p>
 * Note: For <code>IBinary</code>, <code>IArchive</code> and other members
 * derived from a binary type, the implementation returns source iff the
 * element has attached source code and debuging information.
 *
 */

public interface ISourceReference {

	/**
	 * Returns the source code associated with this element.
	 * <p>
	 * For binary files, this returns the source of the entire translation unit 
	 * associated with the binary file (if there is one).
	 * </p>
	 *
	 * @return the source code, or <code>null</code> if this element has no 
	 *   associated source code
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource
	 */
	String getSource() throws CModelException;

	/**
	 * Returns the source range associated with this element.
	 * <p>
	 * For binary files, this returns the range of the entire translation unit 
	 * associated with the binary file (if there is one).
	 * </p>
	 *
	 * @return the source range, or <code>null</code> if if this element has no 
	 *   associated source code
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource
	 */
	ISourceRange getSourceRange() throws CModelException;

	/**
	 * Returns the translation unit in which this member is declared, or <code>null</code>
	 * if this member is not declared in a translation unit (for example, a binary type).
	 * @return
	 * @throws CModelException
	 */
	ITranslationUnit getTranslationUnit();
}
