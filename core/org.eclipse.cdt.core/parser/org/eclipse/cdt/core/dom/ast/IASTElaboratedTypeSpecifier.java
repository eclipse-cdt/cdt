/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents an elaborated type specifier in the C & C++ language grammar.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTElaboratedTypeSpecifier extends IASTDeclSpecifier, IASTNameOwner {
	/**
	 * Enumeration.
	 */
	public static final int k_enum = 0;

	/**
	 * Structure.
	 */
	public static final int k_struct = 1;

	/**
	 * Union.
	 */
	public static final int k_union = 2;

	/**
	 * Constant for extensibility in sub-interfaces.
	 */
	public static final int k_last = k_union;

	/**
	 * Returns the kind.
	 *
	 * @return int (kind).
	 */
	public int getKind();

	/**
	 * Sets the kind.
	 *
	 * @param value int (kind)
	 */
	public void setKind(int value);

	/**
	 * <code>TYPE_NAME</code> describes the relationship between
	 * <code>IASTElaboratedTypeSpecifier</code> and <code>IASTName</code>.
	 */
	public static final ASTNodeProperty TYPE_NAME = new ASTNodeProperty(
			"IASTElaboratedTypeSpecifier.TYPE_NAME - IASTName for IASTElaboratedTypeSpecifier"); //$NON-NLS-1$

	/**
	 * Returns the name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTElaboratedTypeSpecifier copy();
}
