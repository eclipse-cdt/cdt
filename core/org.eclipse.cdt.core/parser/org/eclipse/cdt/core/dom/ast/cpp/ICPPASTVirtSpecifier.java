/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
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

/**
 * A virt-specifier at the end of a function declaration.
 * There are two virt-specifiers, 'final' and 'override'.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.7
 */
public interface ICPPASTVirtSpecifier extends IASTNode {

	public enum SpecifierKind {
		/**
		 * 'final' specifier
		 */
		Final,
		/**
		 * 'override' specifier
		 */
		Override
	}

	/**
	 * Return the kind of this virt-specifier.
	 * The kind is either 'final' or 'override'.
	 */
	SpecifierKind getKind();

	@Override
	public ICPPASTVirtSpecifier copy();

	@Override
	public ICPPASTVirtSpecifier copy(CopyStyle style);

}
