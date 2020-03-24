/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;

public class NoDiscardChecker extends AbstractAstFunctionChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.NoDiscardProblem"; //$NON-NLS-1$
	public static final String PARAM_MACRO_ID = "macro"; //$NON-NLS-1$

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		func.accept(new FunctionCallVisitor());
	}

	class FunctionCallVisitor extends ASTVisitor {
		FunctionCallVisitor() {
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (!shouldReportInMacro() && enclosedInMacroExpansion(expr))
				return PROCESS_SKIP;
			if (expr instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression func = (IASTFunctionCallExpression) expr;
				IASTExpression functionNameExpression = func.getFunctionNameExpression();

				IASTNode parent = func.getParent();
				if (!(func.getParent() instanceof IASTExpressionStatement)) {
					while (parent instanceof IASTUnaryExpression) {
						int operator = ((IASTUnaryExpression) parent).getOperator();
						if (operator != IASTUnaryExpression.op_bracketedPrimary) {
							return PROCESS_CONTINUE;
						}
						parent = parent.getParent();
					}
					if (!(parent instanceof IASTExpressionStatement))
						return PROCESS_CONTINUE;
				}

				IASTName name;
				if (functionNameExpression instanceof IASTIdExpression) {
					name = ((IASTIdExpression) functionNameExpression).getName();
				} else if (functionNameExpression instanceof IASTFieldReference) {
					name = ((IASTFieldReference) functionNameExpression).getFieldName();
				} else {
					return PROCESS_CONTINUE;
				}
				IBinding binding = name.resolveBinding();
				if (binding instanceof IFunction && ((IFunction) binding).isNoDiscard()) {
					reportProblem(ER_ID, expr, name);
				}
				return PROCESS_CONTINUE;
			}
			return PROCESS_CONTINUE;
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_MACRO_ID, CheckersMessages.NoDiscardChecker_ParameterMacro, Boolean.TRUE);
	}

	/**
	 * @return
	 */
	private boolean shouldReportInMacro() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_MACRO_ID);
	}
}
