/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.sample;

import org.eclipse.cdt.codan.core.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * This checker finds a problems that cause by lack of understanding operator
 * precedence in C. In any case it is better to surround expressions in
 * parenthesis to improve readability. Example: ! x>0 && x<10 (this would be
 * (!x)>0 && x<10 in C) We only look for &&, || and ! operators (and binary | &
 * ^ ~)
 * 
 * @author Alena
 * 
 */
public class SuggestedParenthesisChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.sample.SuggestedParenthesisProblem";

	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new ExpressionVisitor());
	}

	class ExpressionVisitor extends ASTVisitor {
		private SuspiciousExpressionVisitor svis;

		ExpressionVisitor() {
			shouldVisitExpressions = true;
			svis = new SuspiciousExpressionVisitor();
		}

		public int visit(IASTExpression expression) {
			int precedence = getPrecedence(expression);
			if (precedence == 2) { // unary not
				if (isUsedAsOperand(expression)) {
					reportProblem(ER_ID, expression,
							"Suggested parenthesis around expression");
					return PROCESS_SKIP;
				}
			}
			if (precedence >= 0) {
				synchronized (svis) { // since we use only one instance of this
										// visitor sync just in case
					svis.init(expression);
					expression.accept(svis);
					if (svis.suspicious == true) {
						reportProblem(ER_ID, svis.other,
								"Suggested parenthesis around expression");
						return PROCESS_SKIP;
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isUsedAsOperand(IASTExpression expression) {
			ASTNodeProperty prop = expression.getPropertyInParent();
			if (prop == IASTBinaryExpression.OPERAND_ONE
					|| prop == IASTBinaryExpression.OPERAND_TWO
					|| prop == IASTUnaryExpression.OPERAND)
				return true;
			return false;
		}
	}

	private int getPrecedence(IASTExpression e) {
		if (e instanceof IASTBinaryExpression) {
			IASTBinaryExpression binExpr = (IASTBinaryExpression) e;
			int operator = binExpr.getOperator();
			if (operator == IASTBinaryExpression.op_binaryAnd)
				return 8;
			if (operator == IASTBinaryExpression.op_binaryXor)
				return 9;
			if (operator == IASTBinaryExpression.op_binaryOr)
				return 10;
			if (operator == IASTBinaryExpression.op_logicalAnd)
				return 11;
			if (operator == IASTBinaryExpression.op_logicalOr)
				return 12;
		}
		if (e instanceof IASTUnaryExpression) {
			IASTUnaryExpression binExpr = (IASTUnaryExpression) e;
			int operator = binExpr.getOperator();
			if (operator == IASTUnaryExpression.op_not)
				return 2;
			if (operator == IASTUnaryExpression.op_tilde)
				return 2;
		}
		return -1;
	}

	class SuspiciousExpressionVisitor extends ASTVisitor {
		IASTExpression parent;
		IASTExpression other;
		boolean suspicious = false;

		void init(IASTExpression e) {
			parent = e;
			suspicious = false;
		}

		SuspiciousExpressionVisitor() {
			shouldVisitExpressions = true;
		}

		public int visit(IASTExpression expression) {
			if (expression == parent)
				return PROCESS_CONTINUE;
			if (expression instanceof IASTUnaryExpression) {
				IASTUnaryExpression uExpr = (IASTUnaryExpression) expression;
				int operator = uExpr.getOperator();
				if (operator == IASTUnaryExpression.op_bracketedPrimary) {
					return PROCESS_SKIP;
				}
			}
			if (getPrecedence(expression) < 0) // not considered operator
				return PROCESS_CONTINUE;
			if (getPrecedence(expression) == getPrecedence(parent)) {
				return PROCESS_SKIP;
			}
			suspicious = true;
			other = expression;
			return PROCESS_ABORT;
		}
	}
}
