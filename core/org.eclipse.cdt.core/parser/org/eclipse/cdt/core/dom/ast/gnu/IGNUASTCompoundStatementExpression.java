/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * There are GNU language extensions that apply to both GCC and G++. Compound
 * statement's as expressions.
 * 
 * @author jcamelon
 */
public interface IGNUASTCompoundStatementExpression extends IASTExpression {

	/**
	 * <code>STATEMENT</code> describes the relationship between
	 * <code>IGNUASTCompoundStatementExpression</code> and
	 * <code>IASTCompoundStatement</code>.
	 */
	public static final ASTNodeProperty STATEMENT = new ASTNodeProperty(
			"IGNUASTCompoundStatementExpression.STATEMENT - IASTCompoundStatement for IGNUASTCompoundStatementExpression"); //$NON-NLS-1$

	/**
	 * Get the compound statement.
	 * 
	 * @return <code>IASTCompoundStatement</code>
	 */
	public IASTCompoundStatement getCompoundStatement();

	/**
	 * Set the compound statement.
	 * 
	 * @param statement
	 *            <code>IASTCompoundStatement</code>
	 */
	public void setCompoundStatement(IASTCompoundStatement statement);

}
