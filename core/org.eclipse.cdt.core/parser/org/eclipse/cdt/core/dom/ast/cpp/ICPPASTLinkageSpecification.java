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
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;

/**
 * This interface represents a linkage specification. e.g. extern "C" { ... }
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTLinkageSpecification extends IASTDeclaration, IASTDeclarationListOwner {

	/**
	 * Get the "literal" that represents the linkage.
	 * 
	 * @return String
	 */
	public String getLiteral();

	/**
	 * Set the "literal" that represents the linkage.
	 * 
	 * @param value
	 *            String
	 */
	public void setLiteral(String value);

	/**
	 * <code>OWNED_DECLARATION</code> is the owned declaration role for
	 * linkages.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTLinkageSpecification.OWNED_DECLARATION - Owned Declaration role for linkages"); //$NON-NLS-1$

	/**
	 * Get all of the declarations.
	 * 
	 * @return <code>IASTDeclaration[] </code>
	 */
	public IASTDeclaration[] getDeclarations();

	/**
	 * Add another declaration to the linkage.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	@Override
	public void addDeclaration(IASTDeclaration declaration);
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTLinkageSpecification copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTLinkageSpecification copy(CopyStyle style);
}
