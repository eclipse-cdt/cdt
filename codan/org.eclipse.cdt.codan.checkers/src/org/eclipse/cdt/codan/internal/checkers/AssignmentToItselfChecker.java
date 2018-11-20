/*******************************************************************************
 * Copyright (c) 2010, 2013 Severin Gehwolf
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Severin Gehwolf  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checker that finds assignment to itself cases, such a
 * a = a. It can produce some false positives such as
 * a[f()]=a[f()] - but who writes code like that?
 */
public class AssignmentToItselfChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.AssignmentToItselfProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// Traverse the ast using the visitor pattern.
		ast.accept(new ASTVisitor() {
			{ // constructor
				shouldVisitExpressions = true;
			}

			// visit expressions
			@Override
			public int visit(IASTExpression expression) {
				if (isAssignmentToItself(expression)) {
					reportProblem(ER_ID, expression, expression.getRawSignature());
				}
				return PROCESS_CONTINUE;
			}

			private boolean isAssignmentToItself(IASTExpression expr) {
				if (expr instanceof IASTBinaryExpression) {
					IASTBinaryExpression binExpr = (IASTBinaryExpression) expr;
					if (binExpr.getOperator() == IASTBinaryExpression.op_assign) {
						IASTExpression operand1 = binExpr.getOperand1();
						IASTExpression operand2 = binExpr.getOperand2();
						if (operand1 != null && operand2 != null) {
							String op1 = operand1.getRawSignature();
							String op2 = operand2.getRawSignature();
							String exprImage = binExpr.getRawSignature();
							return op1.equals(op2)
									// When macro is used, RawSignature returns macro name, see bug 321933
									&& !op1.equals(exprImage);
						}
					}
				}
				return false;
			}
		});
	}
}