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
 * A class-virt-specifier after a class name.
 * There is currently one specifier, 'final'.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 5.7
 */
public interface ICPPASTClassVirtSpecifier extends IASTNode {

	public enum SpecifierKind {
		/**
		 * 'final' specifier
		 */
		Final
	}

	/**
	 * Return the kind of this class-virt-specifier.
	 * Currently the only kind is 'final'.
	 */
	SpecifierKind getKind();

	@Override
	public ICPPASTClassVirtSpecifier copy();

	@Override
	public ICPPASTClassVirtSpecifier copy(CopyStyle style);
}
