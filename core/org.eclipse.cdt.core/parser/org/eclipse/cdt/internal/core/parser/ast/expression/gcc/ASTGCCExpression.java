/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.expression.gcc;

import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.internal.core.parser.ast.expression.ASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTGCCExpression extends ASTExpression {

	/**
	 * @param kind
	 * @param lhs
	 * @param rhs
	 * @param third
	 * @param typeId
	 * @param idExpression
	 * @param literal
	 * @param newDescriptor
	 */
	public ASTGCCExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression third, IASTTypeId typeId, String idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) {
		super(kind, lhs, rhs, third, typeId, idExpression, literal, newDescriptor);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
	 */
	public int evaluateExpression() throws ASTExpressionEvaluationException {
		if( getExpressionKind() == Kind.ID_EXPRESSION )
			return 0;
		return super.evaluateExpression();
	}
}
