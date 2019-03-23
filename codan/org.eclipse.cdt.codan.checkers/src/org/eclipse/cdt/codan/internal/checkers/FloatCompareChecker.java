/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class FloatCompareChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.FloatCompareProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			private boolean equals(IASTExpression expr1, IASTExpression expr2) {
				if (expr1 instanceof IASTIdExpression && expr2 instanceof IASTIdExpression) {
					IBinding leftLeftBinding = ((IASTIdExpression) expr1).getName().resolveBinding();
					IBinding rightLeftBinding = ((IASTIdExpression) expr2).getName().resolveBinding();
					if (CPPVisitor.areEquivalentBindings(leftLeftBinding, rightLeftBinding,
							expr1.getTranslationUnit().getIndex())) {
						return true;
					}
				} else if (expr1 instanceof IASTLiteralExpression && expr2 instanceof IASTLiteralExpression) {
					Number n1 = ValueFactory.getConstantNumericalValue(expr1);
					Number n2 = ValueFactory.getConstantNumericalValue(expr2);
					if (n1 != null && n1.equals(n2))
						return true;
				} else if (expr1 instanceof IASTFieldReference && expr2 instanceof IASTFieldReference) {
					IBinding leftLeftBinding = ((IASTFieldReference) expr1).getFieldName().resolveBinding();
					IBinding rightLeftBinding = ((IASTFieldReference) expr2).getFieldName().resolveBinding();
					if (CPPVisitor.areEquivalentBindings(leftLeftBinding, rightLeftBinding,
							expr1.getTranslationUnit().getIndex())) {
						return true;
					}
				}
				return false;
			}

			private boolean processDirect(IASTBinaryExpression expression) {
				IASTBinaryExpression binary = expression;
				if ((binary.getOperator() == IASTBinaryExpression.op_notequals
						|| binary.getOperator() == IASTBinaryExpression.op_equals)
						&& (isFloat(binary.getOperand1().getExpressionType())
								|| isFloat(binary.getOperand2().getExpressionType()))) {
					reportProblem(ERR_ID, expression);
					return true;
				}
				return false;
			}

			private boolean processIndirect(IASTBinaryExpression binary) {
				if (binary.getOperator() == IASTBinaryExpression.op_logicalAnd) {
					return processIndirect(binary, false);
				} else if (binary.getOperator() == IASTBinaryExpression.op_logicalOr) {
					return processIndirect(binary, true);
				}
				return false;
			}

			private boolean processIndirect(IASTBinaryExpression expression, boolean invert) {
				int cond1Test = IASTBinaryExpression.op_lessEqual;
				int cond2Test = IASTBinaryExpression.op_greaterEqual;

				if (invert) {
					cond1Test = IASTBinaryExpression.op_lessThan;
					cond2Test = IASTBinaryExpression.op_greaterThan;
				}

				IASTBinaryExpression binary = expression;
				IASTExpression left = binary.getOperand1();
				IASTExpression right = binary.getOperand2();
				if (left instanceof IASTBinaryExpression && right instanceof IASTBinaryExpression) {
					int leftOp = ((IASTBinaryExpression) left).getOperator();
					int rightOp = ((IASTBinaryExpression) right).getOperator();
					if ((leftOp == cond1Test && rightOp == cond2Test)
							|| (rightOp == cond1Test && leftOp == cond2Test)) {
						//Case 1:
						// a <= b && c >= d
						// Ex. f <= 3.14 && f >= 3.14
						//Case 2:
						// a >= b && c <= d
						// Ex. f >= 3.14 && f <= 3.14
						IASTExpression leftLeft = ((IASTBinaryExpression) left).getOperand1();
						IASTExpression leftRight = ((IASTBinaryExpression) left).getOperand2();
						IASTExpression rightLeft = ((IASTBinaryExpression) right).getOperand1();
						IASTExpression rightRight = ((IASTBinaryExpression) right).getOperand2();
						if (equals(leftLeft, rightLeft) && equals(leftRight, rightRight)
								&& (isFloat(leftLeft.getExpressionType()) || isFloat(leftRight.getExpressionType()))) {
							reportProblem(ERR_ID, expression);
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTBinaryExpression) {
					boolean res = processDirect((IASTBinaryExpression) expression);
					if (!res)
						res = processIndirect((IASTBinaryExpression) expression);
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isFloat(IType type) {
		type = SemanticUtil.getNestedType(type, SemanticUtil.REF | SemanticUtil.TDEF | SemanticUtil.ALLCVQ);
		if (!(type instanceof IBasicType)) {
			return false;
		}
		IBasicType.Kind k = ((IBasicType) type).getKind();
		switch (k) {
		case eFloat:
		case eDouble:
		case eFloat128:
			return true;
		default:
			return false;
		}
	}
}
