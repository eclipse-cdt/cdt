/**********************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTSwitchStatement extends IASTSwitchStatement {

    /**
     * <code>CONTROLLER_DECLARATION</code> represents the relationship between an
     * <code>IASTSwitchStatement</code> and it's nested
     * <code>IASTDeclaration</code>.
     */
    public static final ASTNodeProperty CONTROLLER_DECLARATION = new ASTNodeProperty(
            "IASTSwitchStatement.CONTROLLER - IASTDeclaration (controller) for IASTSwitchExpression"); //$NON-NLS-1$

    /**
     * In C++, a switch statement can be contorller by a declaration.
     * 
     * @return <code>IASTDeclaration</code>
     */
    public IASTDeclaration getControllerDeclaration();

    /**
     * In C++, a switch statement can be contorller by a declaration.
     * 
     * @param d <code>IASTDeclaration</code>
     */
    public void setControllerDeclaration( IASTDeclaration d );
    
	/**
	 * Get the <code>IScope</code> represented by this switch.
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
    
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTSwitchStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTSwitchStatement copy(CopyStyle style);
}
