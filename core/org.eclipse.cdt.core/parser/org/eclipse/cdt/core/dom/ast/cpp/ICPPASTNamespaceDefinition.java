/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * This interface repesents a namespace definition in C++.
 * 
 * @author jcamelon
 */
public interface ICPPASTNamespaceDefinition extends IASTDeclaration, IASTNameOwner {

	/**
	 * <code>OWNED_DECLARATION</code> is the role served by all the nested
	 * declarations.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"Owned"); //$NON-NLS-1$

	/**
	 * <code>NAMESPACE_NAME</code> is the role served by the name in this
	 * interface.
	 */
	public static final ASTNodeProperty NAMESPACE_NAME = new ASTNodeProperty(
			"Name"); //$NON-NLS-1$

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
	 * A translation unit contains an ordered sequence of declarations.
	 * 
	 * @return <code>IASTDeclaration []</code>
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Add a declaration to the namespace.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void addDeclaration(IASTDeclaration declaration);

	/**
	 * Get the scope object represented by this construct.
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
}
