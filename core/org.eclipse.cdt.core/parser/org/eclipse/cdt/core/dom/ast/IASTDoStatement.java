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
package org.eclipse.cdt.core.dom.ast;

/**
 * Ye ol' do statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTDoStatement extends IASTStatement {

    public static final ASTNodeProperty BODY = new ASTNodeProperty("body");  //$NON-NLS-1$
    public static final ASTNodeProperty CONDITION = new ASTNodeProperty("condition");  //$NON-NLS-1$

	/**
	 * The body of the loop.
	 * 
	 * @return
	 */
	public IASTStatement getBody();
	
	public void setBody(IASTStatement body);
	
	/**
	 * The condition on the loop.
	 * 
	 * @return the expression for the condition
	 */
	public IASTExpression getCondition();
	
	public void setCondition(IASTExpression condition);
	
}
