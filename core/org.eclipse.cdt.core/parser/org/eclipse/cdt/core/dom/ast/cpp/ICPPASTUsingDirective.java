/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface represents a C++ using directive.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTUsingDirective extends IASTDeclaration, IASTNameOwner {
	/**
	 * Constant.
	 */
	public static final ICPPASTUsingDirective[] EMPTY_USINGDIRECTIVE_ARRAY = new ICPPASTUsingDirective[0];

	/**
	 * <code>QUALIFIED_NAME</code> is the name that is brought into local
	 * scope.
	 */
	public static final ASTNodeProperty QUALIFIED_NAME = new ASTNodeProperty(
			"ICPPASTUsingDirective.QUALIFIED_NAME - Name brought into local scope"); //$NON-NLS-1$

	/**
	 * Get the qualified name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getQualifiedName();

	/**
	 * Set the qualified name.
	 * 
	 * @param qualifiedName
	 *            <code>IASTName</code>
	 */
	public void setQualifiedName(IASTName qualifiedName);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTUsingDirective copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTUsingDirective copy(CopyStyle style);

}
