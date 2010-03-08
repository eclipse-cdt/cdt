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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;

/**
 * Checker that detects statements without effect such as
 * 
 * a+b;
 * 
 * or
 * 
 * +b;
 * 
 * 
 */
public class StatementHasNoEffectChecker extends AbstractIndexAstChecker {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.sample.StatementHasNoEffectProblem";

	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new CheckStmpVisitor());
	}

	class CheckStmpVisitor extends ASTVisitor {
		CheckStmpVisitor() {
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTExpressionStatement) {
				if (hasNoEffect(((IASTExpressionStatement) stmt)
						.getExpression())) {
					reportProblem(ER_ID, stmt, "Statement has no effect");
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		/**
		 * We consider has not effect binary statements without assignment and
		 * unary statement which is not dec and inc. If operator is overloaded
		 * we not going to bother.
		 * 
		 * @param e
		 * @return
		 */
		private boolean hasNoEffect(IASTExpression e) {
			if (e instanceof IASTBinaryExpression) {
				IASTBinaryExpression binExpr = (IASTBinaryExpression) e;
				if (binExpr.getOperator() == IASTBinaryExpression.op_assign)
					return false;
				if (binExpr instanceof CPPASTBinaryExpression) {
					// unfortunately ICPPASTBinaryExpression does not have
					// getOverload public method
					CPPASTBinaryExpression cppBin = (CPPASTBinaryExpression) binExpr;
					ICPPFunction overload = cppBin.getOverload();
					if (overload != null)
						return false;
					IType expressionType = binExpr.getOperand1()
							.getExpressionType();
					if (!(expressionType instanceof IBasicType)) {
						return false; // must be overloaded but parser could not
						// find it
					}
				}
				return true;
			}
			if (e instanceof IASTUnaryExpression) {
				IASTUnaryExpression unaryExpr = (IASTUnaryExpression) e;
				int operator = unaryExpr.getOperator();
				switch (operator) {
				case IASTUnaryExpression.op_postFixDecr:
				case IASTUnaryExpression.op_prefixDecr:
				case IASTUnaryExpression.op_postFixIncr:
				case IASTUnaryExpression.op_prefixIncr:
					return false;
				}
				return true;
			}
			if (e instanceof IASTIdExpression) {
				// simply a;
				return true;
			}
			return false;
		}
	}
}
