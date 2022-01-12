/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge
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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * AST node for elements of the qualifier in a qualified name.
 *
 * A name-specifier can either be a name, or a decltype-specifier.
 *
 * Note that a decltype-specifier can only appear as the first
 * element of a qualifier, but this constraint is not encoded
 * in the AST.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.6
 */
public interface ICPPASTNameSpecifier extends IASTNode {
	public static final ICPPASTNameSpecifier[] EMPTY_NAME_SPECIFIER_ARRAY = {};

	@Override
	public ICPPASTNameSpecifier copy();

	@Override
	public ICPPASTNameSpecifier copy(CopyStyle style);

	public char[] toCharArray();

	/**
	 * If the name-specifier is a name, returns the binding named.
	 * If the name-specifier is a decltype-specifier, return the type
	 * if it's a binding, otherwise return null.
	 */
	public IBinding resolveBinding();

	/**
	 * Similar to resolveBinding(), but only performs the first phase
	 * of binding resolution for two-phase bindings.
	 */
	public IBinding resolvePreBinding();
}
