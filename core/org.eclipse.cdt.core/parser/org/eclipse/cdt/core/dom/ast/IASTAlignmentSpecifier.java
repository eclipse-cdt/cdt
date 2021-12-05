/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents an alignment specifier.
 *
 * Grammatically, this is a decl-specifier in C and an attribute-specifier in C++.
 *
 * Possible forms are:
 *   C++:
 *     alignas(&lt;type-id&gt;)
 *     alignas(&lt;expression&gt;)
 *   C:
 *     _Alignas(&lt;type-id&gt;)
 *     _Alignas(&lt;expression&gt;)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.10
 */
public interface IASTAlignmentSpecifier extends IASTNode {
	public static final IASTAlignmentSpecifier[] EMPTY_ALIGNMENT_SPECIFIER_ARRAY = {};

	public static final ASTNodeProperty ALIGNMENT_EXPRESSION = new ASTNodeProperty(
			"IASTAlignmentSpecifier.ALIGNMENT_EXPRESSION - Expression in alignment specifier"); //$NON-NLS-1$

	public static final ASTNodeProperty ALIGNMENT_TYPEID = new ASTNodeProperty(
			"IASTAlignmentSpecifier.ALIGNMENT_TYPEID - Type-id in alignment specifier"); //$NON-NLS-1$

	/**
	 * If the specifier is of the form 'alignas(&lt;expression&gt;)' or '_Alignas(&lt;expression&gt;)',
	 * returns the enclosed expression. Otherwise, returns null.
	 */
	IASTExpression getExpression();

	/**
	 * If the specifier is of the form 'alignas(&lt;type-id&gt;)' or '_Alignas(&lt;type-id&gt;)',
	 * returns the enclosed type-id. Otherwise, returns null.
	 */
	IASTTypeId getTypeId();

	@Override
	public IASTAlignmentSpecifier copy();

	@Override
	public IASTAlignmentSpecifier copy(CopyStyle style);
}
