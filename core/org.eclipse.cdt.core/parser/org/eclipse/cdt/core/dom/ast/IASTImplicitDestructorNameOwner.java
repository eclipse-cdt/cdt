/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An AST node that may have implicit destructor names.
 * @see IASTImplicitDestructorName
 *
 * @since 5.10
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImplicitDestructorNameOwner extends IASTNode {
	public static final ASTNodeProperty IMPLICIT_DESTRUCTOR_NAME = new ASTNodeProperty(
			"IASTImplicitDestructorNameOwner.IMPLICIT_DESTRUCTOR_NAME"); //$NON-NLS-1$

	public IASTImplicitDestructorName[] getImplicitDestructorNames();
}
