/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.expression;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTConditionalExpression extends ASTBinaryExpression
		implements
			IASTExpression {

	private final IASTExpression thirdExpression;

	/**
	 * @param kind
	 * @param lhs
	 * @param rhs
	 * @param thirdExpression
	 */
	public ASTConditionalExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression) {
		super( kind, lhs, rhs );
		this.thirdExpression = thirdExpression;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getThirdExpression()
	 */
	public IASTExpression getThirdExpression() {
		return thirdExpression;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
