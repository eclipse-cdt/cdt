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
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

/**
 * This inteface accommodates C++ allows for broader while loop syntax.
 * 
 * @author jcamelon
 */
public interface ICPPASTWhileStatement extends IASTWhileStatement {

	/**
	 * In C++ conditions can be declarations w/side effects.
	 */
	public static final ASTNodeProperty CONDITIONDECLARATION = new ASTNodeProperty(
			"ICPPASTWhileStatement.CONDITIONDECLARATION - C++ condition/declaration"); //$NON-NLS-1$

	/**
	 * Get the condition declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getConditionDeclaration();

	/**
	 * Set the condition declaration.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void setConditionDeclaration(IASTDeclaration declaration);

}
