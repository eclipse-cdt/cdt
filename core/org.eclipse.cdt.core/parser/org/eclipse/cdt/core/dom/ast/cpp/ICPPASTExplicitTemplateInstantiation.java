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

/**
 * This interface represents an explict template instantiation.
 * 
 * @author jcamelon
 */
public interface ICPPASTExplicitTemplateInstantiation extends IASTDeclaration {

	/**
	 * <code>OWNED_DECLARATION</code> represents the role of the inner
	 * declaration that this template refers to.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION - Role of inner declaration template refers to"); //$NON-NLS-1$

	/**
	 * Get the owned declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the owned declaration.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void setDeclaration(IASTDeclaration declaration);

}
