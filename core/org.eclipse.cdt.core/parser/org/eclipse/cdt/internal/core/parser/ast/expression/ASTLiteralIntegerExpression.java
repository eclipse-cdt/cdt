/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.expression;

import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class ASTLiteralIntegerExpression extends ASTExpression
		implements
			IASTExpression {

	private final long literal;
	private final boolean isHex;

	/**
	 * @param kind
	 * @param literal
	 */
	public ASTLiteralIntegerExpression(Kind kind, long literal, boolean isHex) {
		super( kind );
		this.literal = literal;
		this.isHex = isHex;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		if( isHex )
		{
			StringBuffer x = new StringBuffer( "0x"); //$NON-NLS-1$
			x.append( Long.toHexString(literal));
			return x.toString();
		}
			
		return Long.toString( literal );
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
	 */
	public long evaluateExpression() throws ASTExpressionEvaluationException {
		if( getExpressionKind() == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL )
			return literal;
		return super.evaluateExpression();
	}
}
