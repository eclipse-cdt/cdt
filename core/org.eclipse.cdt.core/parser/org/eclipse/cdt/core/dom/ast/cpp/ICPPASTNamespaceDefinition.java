/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * This interface represents a namespace definition in C++.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNamespaceDefinition extends IASTDeclaration, IASTNameOwner, IASTDeclarationListOwner {
	/**
	 * <code>OWNED_DECLARATION</code> is the role served by all the nested
	 * declarations.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTNamespaceDefinition.OWNED_DECLARATION - Role served by nested declarations"); //$NON-NLS-1$

	/**
	 * <code>NAMESPACE_NAME</code> is the role served by the name in this
	 * interface.
	 */
	public static final ASTNodeProperty NAMESPACE_NAME = new ASTNodeProperty(
			"ICPPASTNamespaceDefinition.NAMESPACE_NAME - Role served by name"); //$NON-NLS-1$

	/**
	 * Get the name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Specify whether the namespace definition is inline.
	 * @since 5.3
	 */
	public void setIsInline(boolean isInline);

	/**
	 * Returns whether this namespace definition is inline.
	 * @since 5.3
	 */
	public boolean isInline();
	
	/**
	 * A namespace contains an ordered sequence of declarations.
	 * 
	 * @return an array of declarations contained in the namespace
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Adds a declaration to the namespace.
	 * 
	 * @param declaration <code>IASTDeclaration</code>
	 */
	@Override
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * Returns the scope object represented by this construct.
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTNamespaceDefinition copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTNamespaceDefinition copy(CopyStyle style);
}
