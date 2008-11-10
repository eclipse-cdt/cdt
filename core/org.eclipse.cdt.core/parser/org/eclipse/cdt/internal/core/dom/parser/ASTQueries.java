/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Base class for {@link CVisitor} and {@link CPPVisitor}
 */
public class ASTQueries {
	/**
	 * Tests whether the given expression can contain ast-names, suitable to be used before ambiguity 
	 * resolution.
	 */
	public static boolean canContainName(IASTExpression expr) {
		if (expr == null || expr instanceof IASTLiteralExpression) 
			return false;

		if (expr instanceof IASTAmbiguousExpression) 
			return true;
		if (expr instanceof IASTIdExpression) 
			return true;
		if (expr instanceof IASTCastExpression)
			return true;
		
		if (expr instanceof IASTUnaryExpression) {
			IASTUnaryExpression uexpr= (IASTUnaryExpression) expr;
			return canContainName(uexpr.getOperand());
		}
		if (expr instanceof IASTBinaryExpression) {
			IASTBinaryExpression bexpr= (IASTBinaryExpression) expr;
			return canContainName(bexpr.getOperand1()) || canContainName(bexpr.getOperand2());
		}
		if (expr instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr= (IASTConditionalExpression) expr;
			return canContainName(cexpr.getLogicalConditionExpression()) || 
					canContainName(cexpr.getPositiveResultExpression()) || canContainName(cexpr.getNegativeResultExpression());
		}
		if (expr instanceof IASTExpressionList) {
			IASTExpressionList lexpr= (IASTExpressionList) expr;
			IASTExpression[] subexprs= lexpr.getExpressions();
			for (IASTExpression subexpr : subexprs) {
				if (canContainName(subexpr))
					return true;
			}
		}
		return true;
	}
}
