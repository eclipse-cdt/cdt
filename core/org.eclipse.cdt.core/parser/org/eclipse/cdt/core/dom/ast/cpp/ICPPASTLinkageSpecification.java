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
 * This interface represents a linkage specification. e.g. extern "C" { ... }
 * 
 * @author jcamelon
 */
public interface ICPPASTLinkageSpecification extends IASTDeclaration {

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
			"Owned Declaration"); //$NON-NLS-1$

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
	public void addDeclaration(IASTDeclaration declaration);
}
