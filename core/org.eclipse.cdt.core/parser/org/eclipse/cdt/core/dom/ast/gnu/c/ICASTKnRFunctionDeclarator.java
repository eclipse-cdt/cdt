/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This is the declarator for a K&R C Function.
 * 
 * @author dsteffle
 */
public interface ICASTKnRFunctionDeclarator extends IASTFunctionDeclarator, IASTNameOwner {

	/**
	 * <code>PARAMETER_NAME</code> refers to the names qualified in a K&R C
	 * function definition.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"ICASTKnRFunctionDeclarator.PARAMETER_NAME - K&R Parameter Name"); //$NON-NLS-1$

	/**
	 * Overwrite the parameter names. TODO - this should change to add
	 * 
	 * @param names
	 *            <code>IASTName []</code>
	 */
	public void setParameterNames(IASTName[] names);

	/**
	 * Get parameter names.
	 * 
	 * @return <code>IASTName []</code>
	 */
	public IASTName[] getParameterNames();

	/**
	 * <code>FUNCTION_PARAMETER</code> represents the relationship between an
	 * K&R function declarator and the full parameter declarations.
	 */
	public static final ASTNodeProperty FUNCTION_PARAMETER = new ASTNodeProperty(
			"ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER - Full K&R Parameter Declaration"); //$NON-NLS-1$

	/**
	 * Overrwrite the parameter lists.
	 * 
	 * @param decls
	 *            TODO - replace w/zadd
	 */
	public void setParameterDeclarations(IASTDeclaration[] decls);

	/**
	 * Get parameters declarations.
	 * 
	 * @return <code>IASTDeclaration []</code>
	 */
	public IASTDeclaration[] getParameterDeclarations();

	/**
	 * Map declarator to IASTName.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 * @return
	 */
	public IASTDeclarator getDeclaratorForParameterName(IASTName name);
}
