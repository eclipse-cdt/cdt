/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * The 'if' statement including the optional else clause.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTIfStatement extends IASTIfStatement {
	/**
	 * Returns the condition declaration. The condition declaration and the condition expression are
	 * mutually exclusive.
	 *
	 * @return the condition declaration, or <code>null</code> if the 'if' statement doesn't
	 *     have a condition declaration.
	 */
    public IASTDeclaration getConditionDeclaration();

    /**
     * Sets the condition declaration.
     */
    public void setConditionDeclaration(IASTDeclaration d);
    
    /**
	 * Returns the implicit <code>IScope</code> represented by this if statement
	 * 
	 * @return <code>IScope</code>
	 */
	public IScope getScope();
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTIfStatement copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTIfStatement copy(CopyStyle style);
}
