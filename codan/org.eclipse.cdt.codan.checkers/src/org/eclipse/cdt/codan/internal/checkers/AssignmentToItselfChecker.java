/*******************************************************************************
 * Copyright (c) 2010 Severin Gehwolf 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Severin Gehwolf  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checker that find assignment to itself cases, such a
 * a = a. It can produce some false positives such as
 * a[f()]=a[f()] - but who write codes like that?
 */
public class AssignmentToItselfChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.AssignmentToItselfProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
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

			private boolean isAssignmentToItself(IASTExpression e) {
				if (e instanceof IASTBinaryExpression) {
					IASTBinaryExpression binExpr = (IASTBinaryExpression) e;
					if (binExpr.getOperator() == IASTBinaryExpression.op_assign) {
						String op1 = binExpr.getOperand1().getRawSignature();
						String op2 = binExpr.getOperand2().getRawSignature();
						String expr = binExpr.getRawSignature();
						return op1.equals(op2)
						// when macro is used RawSignature returns macro name, see Bug 321933
								&& !op1.equals(expr);
					}
				}
				return false;
			}
		});
	}
}