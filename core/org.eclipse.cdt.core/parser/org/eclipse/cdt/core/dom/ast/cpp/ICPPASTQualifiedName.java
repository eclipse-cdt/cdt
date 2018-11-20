/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface is a qualified name in C++.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTQualifiedName extends ICPPASTName, IASTNameOwner {
	/**
	 * Each ICPPASTNameSpecifier segment has property being <code>SEGMENT_NAME</code>.
	 */
	public static final ASTNodeProperty SEGMENT_NAME = new ASTNodeProperty(
			"ICPPASTQualifiedName.SEGMENT_NAME - An ICPPASTNameSpecifier segment"); //$NON-NLS-1$

	/**
	 * Adds a name segment.
	 *
	 * @param name {@code IASTName}
	 */
	public void addName(IASTName name);

	/**
	 * Adds a segment to the end of the qualifier.
	 *
	 * @since 5.6
	 */
	public void addNameSpecifier(ICPPASTNameSpecifier nameSpecifier);

	/**
	 * Sets the last name.
	 *
	 * @since 5.6
	 */
	public void setLastName(ICPPASTName name);

	/**
	 * Returns all segments of the name but the last.
	 *
	 * @since 5.6
	 */
	public ICPPASTNameSpecifier[] getQualifier();

	/**
	 * Returns all segments of the name.
	 *
	 * This method is less efficient than calling getQualifier() and
	 * getLastName() separately, because it constructs a new array.
	 * It is provided mainly to ease transition of client code from
	 * getNames() to getQualifier() and getLastName().
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.6
	 */
	public ICPPASTNameSpecifier[] getAllSegments();

	/**
	 * The last name is often semantically significant.
	 */
	@Override
	public IASTName getLastName();

	/**
	 * Is this name fully qualified?
	 *
	 * @return boolean
	 */
	public boolean isFullyQualified();

	/**
	 * Sets this name to be fully qualified or not (true/false).
	 *
	 * @param value boolean
	 */
	public void setFullyQualified(boolean value);

	/**
	 * Returns {@code true} if last segment is an ICPPASTConversionName or an ICPPASTOperatorName.
	 */
	public boolean isConversionOrOperator();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTQualifiedName copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTQualifiedName copy(CopyStyle style);
}
