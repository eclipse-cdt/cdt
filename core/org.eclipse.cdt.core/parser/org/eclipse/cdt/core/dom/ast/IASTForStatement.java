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
 * The for statement. The initialization clause can be an expression or
 * a declaration but not both.
 * 
 * @author Doug Schaefer
 */
public interface IASTForStatement extends IASTStatement {

    public static final ASTNodeProperty INITEXPRESSION = new ASTNodeProperty("initExpression");  //$NON-NLS-1$
    public static final ASTNodeProperty INITDECLARATION = new ASTNodeProperty("initDeclaration");  //$NON-NLS-1$
    public static final ASTNodeProperty CONDITION = new ASTNodeProperty("condition");  //$NON-NLS-1$
    public static final ASTNodeProperty ITERATION = new ASTNodeProperty("iteration");  //$NON-NLS-1$
    public static final ASTNodeProperty BODY = new ASTNodeProperty("body"); //$NON-NLS-1$

	/**
	 * The initial expression for the loop. Returns null if there is
	 * none. You can not have both an initial expression and an initial
	 * declaration.
	 * 
	 * @return
	 */
	public IASTExpression getInitExpression();
	
	public void setInit(IASTExpression expression);

	/**
	 * The initial declaration for the loop. Returns null if there is
	 * none. You can not have both an initial declaration and an initial
	 * declaration.
	 * 
	 * @return
	 */
	public IASTDeclaration getInitDeclaration();
	
	public void setInit(IASTDeclaration declaration);

	/**
	 * The condition for the loop.
	 * 
	 * @return
	 */
	public IASTExpression getCondition();
	
	public void setCondition(IASTExpression condition);

	/**
	 * The expression that is evaluated after the completion of an iteration
	 * of the loop.
	 * 
	 * @return
	 */
	public IASTExpression getIterationExpression();

	public void setIterationExpression(IASTExpression iterator);
	
	public IASTStatement getBody();
	public void setBody( IASTStatement statement );
	
	public IScope getScope();	
}
