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
 * Ye ol' while statement.
 * 
 * @author Doug Schaefer
 */
public interface IASTWhileStatement extends IASTStatement {

	public static final ASTNodeProperty CONDITIONEXPRESSION = new ASTNodeProperty("condition");  //$NON-NLS-1$
	public static final ASTNodeProperty BODY = new ASTNodeProperty("body");  //$NON-NLS-1$

	/**
	 * The condition on the while loop
	 * 
	 * @return expression for the condition
	 */
	public IASTExpression getCondition();
	public void setCondition(IASTExpression condition);
	
	/**
	 * The body of the loop.
	 * 
	 * @return the body
	 */
	public IASTStatement getBody();

	public void setBody(IASTStatement body);
	
}
