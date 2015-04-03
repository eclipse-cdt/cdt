/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An AST node that may have implicit destructor names.
 * @see IASTImplicitDestructorName
 *
 * @since 5.11
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTImplicitDestructorNameOwner extends IASTNode {
	public static final ASTNodeProperty IMPLICIT_DESTRUCTOR_NAME = 
			new ASTNodeProperty("IASTImplicitDestructorNameOwner.IMPLICIT_DESTRUCTOR_NAME"); //$NON-NLS-1$

	public IASTImplicitDestructorName[] getImplicitDestructorNames();
}
