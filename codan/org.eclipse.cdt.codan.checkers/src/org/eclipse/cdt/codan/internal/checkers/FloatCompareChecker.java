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
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
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

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTBinaryExpression) {
					IASTBinaryExpression binary = (IASTBinaryExpression) expression;
					if (binary.getOperator() == IASTBinaryExpression.op_equals
							&& (isFloat(binary.getOperand1().getExpressionType())
									|| isFloat(binary.getOperand2().getExpressionType()))) {
						reportProblem(ERR_ID, expression);
					}
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
