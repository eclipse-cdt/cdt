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
package org.eclipse.cdt.internal.core.parser.ast.quick.extension;

import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.extension.IASTExpressionExtension;


public final class ASTExpressionExtension implements IASTExpressionExtension {
	private IASTExpression expression;

	/**
	 * @param ASTExpression
	 */
	public ASTExpressionExtension() {
	}

	public int evaluateExpression() throws ASTExpressionEvaluationException {
		if( this.expression.getExpressionKind() == Kind.ID_EXPRESSION )
			return 0;
		throw new ASTExpressionEvaluationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.extension.IASTExpressionExtension#setExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public void setExpression(IASTExpression expression) {
		this.expression = expression;
	}
}