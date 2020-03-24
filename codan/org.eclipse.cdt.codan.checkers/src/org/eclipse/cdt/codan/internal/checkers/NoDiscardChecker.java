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
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

@SuppressWarnings("restriction")
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
				if (!(func.getParent() instanceof IASTExpressionStatement)
						&& func.getPropertyInParent() != IASTExpressionList.NESTED_EXPRESSION) {
					while (parent instanceof IASTUnaryExpression) {
						int operator = ((IASTUnaryExpression) parent).getOperator();
						if (operator != IASTUnaryExpression.op_bracketedPrimary) {
							return PROCESS_CONTINUE;
						}
						parent = parent.getParent();
					}
					if (!(parent instanceof IASTExpressionStatement)) {
						return PROCESS_CONTINUE;
					}
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

				if (checkNestedLeftSide(func, binding, name)) {
					return PROCESS_CONTINUE;
				}

				if (binding instanceof IFunction && ((IFunction) binding).isNoDiscard()) {
					reportProblem(ER_ID, expr, name);
				} else if (binding instanceof ICPPClassType) {
					ICPPConstructor[] ctor = ((ICPPClassType) binding).getConstructors();
					for (ICPPConstructor c : ctor) {
						if (c.isNoDiscard()) {
							reportProblem(ER_ID, expr, name);
							break;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
			return PROCESS_CONTINUE;
		}

		private boolean checkNestedLeftSide(IASTFunctionCallExpression func, IBinding binding, IASTName name) {
			if (func.getPropertyInParent() == IASTExpressionList.NESTED_EXPRESSION) {
				IASTExpressionList list = (IASTExpressionList) func.getParent();
				IASTExpression[] allExpr = list.getExpressions();
				if (allExpr != null && allExpr.length > 1) {
					IASTExpression last = allExpr[allExpr.length - 1];

					IASTExpression functionNameExpression;
					if (last instanceof IASTFunctionCallExpression) {
						functionNameExpression = ((IASTFunctionCallExpression) last).getFunctionNameExpression();
					} else {
						return false;
					}

					IASTName lastName;
					if (functionNameExpression instanceof IASTIdExpression) {
						lastName = ((IASTIdExpression) functionNameExpression).getName();
					} else if (functionNameExpression instanceof IASTFieldReference) {
						lastName = ((IASTFieldReference) functionNameExpression).getFieldName();
					} else {
						return false;
					}
					IBinding lastBinding = lastName.resolveBinding();
					if (CPPVisitor.areEquivalentBindings(binding, lastBinding, name.getTranslationUnit().getIndex())) {
						return true;
					}
				}
			}
			return false;
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
