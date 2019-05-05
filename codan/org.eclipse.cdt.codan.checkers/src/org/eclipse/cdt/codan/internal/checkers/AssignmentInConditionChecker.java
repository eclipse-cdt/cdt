/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

public class AssignmentInConditionChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.AssignmentInConditionProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new CheckCodeVisitor());
	}

	class CheckCodeVisitor extends ASTVisitor {
		CheckCodeVisitor() {
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (isAssignmentExpression(expression) && isUsedAsCondition(expression)) {
				reportProblem(ER_ID, expression, expression.getRawSignature());
			}
			return PROCESS_CONTINUE;
		}

		private boolean isAssignmentExpression(IASTExpression e) {
			if (e instanceof IASTBinaryExpression) {
				IASTBinaryExpression binExpr = (IASTBinaryExpression) e;
				return binExpr.getOperator() == IASTBinaryExpression.op_assign;
			} else if (e instanceof IASTExpressionList) {
				for (IASTExpression expr : ((IASTExpressionList) e).getExpressions()) {
					if (isAssignmentExpression(expr))
						return true;
				}
			}
			return false;
		}

		private boolean isUsedAsCondition(IASTExpression expression) {
			ASTNodeProperty prop = expression.getPropertyInParent();
			if (prop == IASTForStatement.CONDITION || prop == IASTIfStatement.CONDITION
					|| prop == IASTWhileStatement.CONDITIONEXPRESSION || prop == IASTDoStatement.CONDITION)
				return true;
			if (prop == IASTUnaryExpression.OPERAND) {
				IASTUnaryExpression expr = (IASTUnaryExpression) expression.getParent();
				if (expr.getOperator() == IASTUnaryExpression.op_bracketedPrimary
						&& expr.getPropertyInParent() == IASTConditionalExpression.LOGICAL_CONDITION) {
					return true;
				}
			}
			return false;
		}
	}
}
