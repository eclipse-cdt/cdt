/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;

/**
 * This is a function try block declarator.
 * 
 * @author jcamelon
 */
public interface ICPPASTFunctionTryBlockDeclarator extends
		ICPPASTFunctionDeclarator {

	/**
	 * A <code>CATCH_HANDLER</code> is the role of an ICPPASTCatchHandler in
	 * this interface.
	 */
	public static final ASTNodeProperty CATCH_HANDLER = new ASTNodeProperty(
			"ICPPASTFunctionTryBlockDeclarator.CATCH_HANDLER - role of an ICPPASTCatchHandler"); //$NON-NLS-1$

	/**
	 * Add a catch handler.
	 * 
	 * @param statement
	 *            <code>ICPPASTCatchHandler</code>
	 */
	public void addCatchHandler(ICPPASTCatchHandler statement);

	/**
	 * Get catch handlers.
	 * 
	 * @return <code>ICPPASTCatchHandler</code>
	 */
	public ICPPASTCatchHandler[] getCatchHandlers();
}
