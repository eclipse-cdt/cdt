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
 * The if statement including the optional else clause.
 * 
 * @author Doug Schaefer
 */
public interface IASTIfStatement extends IASTStatement {

    public static final ASTNodeProperty CONDITION = new ASTNodeProperty("condition");  //$NON-NLS-1$
    public static final ASTNodeProperty THEN = new ASTNodeProperty("then");  //$NON-NLS-1$
    public static final ASTNodeProperty ELSE = new ASTNodeProperty("else");  //$NON-NLS-1$

	/**
	 * The condition in the if statement.
	 * 
	 * @return the condition expression
	 */
	public IASTExpression getCondition();
	
	public void setCondition(IASTExpression condition);
	
	/**
	 * The statement that is executed if the condition is true.
	 * 
	 * @return the then clause
	 */
	public IASTStatement getThenClause();
	
	public void setThenClause(IASTStatement thenClause);
	
	/**
	 * The statement that is executed if the condition is false. This
	 * clause is optional and returns null if there is none.
	 * 
	 * @return the else clause or null
	 */
	public IASTStatement getElseClause();

	public void setElseClause(IASTStatement elseClause);
	
}
