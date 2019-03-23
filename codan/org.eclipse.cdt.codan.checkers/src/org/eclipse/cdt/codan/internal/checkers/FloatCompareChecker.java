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
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
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

			private boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
				if (binding1.equals(binding2)) {
					return true;
				}
				if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) && index != null) {
					if (binding1 instanceof IIndexBinding) {
						binding2 = index.adaptBinding(binding2);
					} else {
						binding1 = index.adaptBinding(binding1);
					}
					if (binding1 == null || binding2 == null) {
						return false;
					}
					if (binding1.equals(binding2)) {
						return true;
					}
				}
				return false;
			}

			private boolean equals(IASTExpression expr1, IASTExpression expr2) {
				if (expr1 instanceof IASTIdExpression && expr2 instanceof IASTIdExpression) {
					IBinding leftLeftBinding = ((IASTIdExpression) expr1).getName().resolveBinding();
					IBinding rightLeftBinding = ((IASTIdExpression) expr2).getName().resolveBinding();
					if (areEquivalentBindings(leftLeftBinding, rightLeftBinding,
							expr1.getTranslationUnit().getIndex())) {
						return true;
					}
				} else if (expr1 instanceof IASTLiteralExpression && expr2 instanceof IASTLiteralExpression) {
					Number n1 = ValueFactory.getConstantNumericalValue(expr1);
					Number n2 = ValueFactory.getConstantNumericalValue(expr2);
					if (n1.equals(n2))
						return true;
				} else if (expr1 instanceof IASTFieldReference && expr2 instanceof IASTFieldReference) {
					IBinding leftLeftBinding = ((IASTFieldReference) expr1).getFieldName().resolveBinding();
					IBinding rightLeftBinding = ((IASTFieldReference) expr2).getFieldName().resolveBinding();
					if (areEquivalentBindings(leftLeftBinding, rightLeftBinding,
							expr1.getTranslationUnit().getIndex())) {
						return true;
					}
				}
				return false;
			}

			private int processBinary(IASTBinaryExpression expression, boolean invert) {
				int directTest = IASTBinaryExpression.op_equals;
				int logicTest = IASTBinaryExpression.op_logicalAnd;
				int cond1Test = IASTBinaryExpression.op_lessEqual;
				int cond2Test = IASTBinaryExpression.op_greaterEqual;

				if (invert) {
					directTest = IASTBinaryExpression.op_notequals;
					logicTest = IASTBinaryExpression.op_logicalOr;
					cond1Test = IASTBinaryExpression.op_lessThan;
					cond2Test = IASTBinaryExpression.op_greaterThan;
				}

				IASTBinaryExpression binary = expression;
				if (binary.getOperator() == directTest && (isFloat(binary.getOperand1().getExpressionType())
						|| isFloat(binary.getOperand2().getExpressionType()))) {
					reportProblem(ERR_ID, expression);
				} else if (binary.getOperator() == logicTest) {
					IASTExpression left = binary.getOperand1();
					IASTExpression right = binary.getOperand2();
					if (left instanceof IASTBinaryExpression && right instanceof IASTBinaryExpression) {
						int leftOp = ((IASTBinaryExpression) left).getOperator();
						int rightOp = ((IASTBinaryExpression) right).getOperator();
						if (leftOp == cond1Test && rightOp == cond2Test) {
							// a <= b && c >= d
							// Ex. f <= 3.14 && f >= 3.14
							IASTExpression leftLeft = ((IASTBinaryExpression) left).getOperand1();
							IASTExpression leftRight = ((IASTBinaryExpression) left).getOperand2();
							IASTExpression rightLeft = ((IASTBinaryExpression) right).getOperand1();
							IASTExpression rightRight = ((IASTBinaryExpression) right).getOperand2();
							if (equals(leftLeft, rightLeft) && equals(leftRight, rightRight)
									&& (isFloat(leftLeft.getExpressionType())
											|| isFloat(leftRight.getExpressionType()))) {
								reportProblem(ERR_ID, expression);
								return PROCESS_SKIP;
							}
						} else if (rightOp == cond1Test && leftOp == cond2Test) {
							// a >= b && c <= d
							// Ex. f >= 3.14 && f <= 3.14
							IASTExpression leftLeft = ((IASTBinaryExpression) left).getOperand1();
							IASTExpression leftRight = ((IASTBinaryExpression) left).getOperand2();
							IASTExpression rightLeft = ((IASTBinaryExpression) right).getOperand1();
							IASTExpression rightRight = ((IASTBinaryExpression) right).getOperand2();
							if (equals(leftRight, rightRight) && equals(leftLeft, rightLeft)
									&& (isFloat(leftLeft.getExpressionType())
											|| isFloat(leftRight.getExpressionType()))) {
								reportProblem(ERR_ID, expression);
								return PROCESS_SKIP;
							}
						}
					}
				}
				return PROCESS_CONTINUE;
			}

			private IASTExpression unwrapUnaryExpression(IASTUnaryExpression expression) {
				if (expression.getOperand() instanceof IASTUnaryExpression)
					return unwrapUnaryExpression((IASTUnaryExpression) expression.getOperand());
				return expression.getOperand();
			}

			@Override
			public int visit(IASTExpression expression) {
				int ret = PROCESS_CONTINUE;
				if (expression instanceof IASTBinaryExpression) {
					ret = processBinary((IASTBinaryExpression) expression, false);
				} else if (expression instanceof IASTUnaryExpression
						&& ((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_not) {
					IASTExpression expr = unwrapUnaryExpression((IASTUnaryExpression) expression);
					if (expr instanceof IASTBinaryExpression) {
						processBinary((IASTBinaryExpression) expr, true);
					}
					return PROCESS_SKIP;
				}
				return ret;
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
