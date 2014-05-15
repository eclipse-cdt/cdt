/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
