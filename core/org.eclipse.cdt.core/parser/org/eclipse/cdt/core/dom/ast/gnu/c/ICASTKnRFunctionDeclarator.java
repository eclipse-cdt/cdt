/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * This is the declarator for a K&R C Function.
 *
 * @author dsteffle
 */
public interface ICASTKnRFunctionDeclarator extends IASTFunctionDeclarator {
	
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty( "Parameter Name"); //$NON-NLS-1$
	public void setParameterNames(IASTName[] names);
	public IASTName[] getParameterNames();
	
	public static final ASTNodeProperty FUNCTION_PARAMETER = new ASTNodeProperty( "Parameter"); //$NON-NLS-1$
	public void setParameterDeclarations(IASTDeclaration[] decls);
	public IASTDeclaration[] getParameterDeclarations();
	public IASTDeclarator getDeclaratorForParameterName(IASTName name);
}
