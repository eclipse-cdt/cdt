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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;

@SuppressWarnings("restriction")
public class NoDiscardChecker extends AbstractAstFunctionChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.NoDiscardProblem"; //$NON-NLS-1$
	public static final String PARAM_MACRO_ID = "macro"; //$NON-NLS-1$

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		func.accept(new FunctionCallVisitor());
	}

	class FunctionCallVisitor extends ASTVisitor {

		IASTNode res;

		FunctionCallVisitor() {
			shouldVisitExpressions = true;
			res = null;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (!shouldReportInMacro() && enclosedInMacroExpansion(expr))
				return PROCESS_SKIP;
			if (isDiscardedValueExpression(expr) && isCallToNodiscardFunction(expr)) {
				reportProblem(ER_ID, expr, res);
				res = null;
			}
			return PROCESS_CONTINUE;
		}

		private boolean isDiscardedValueExpression(IASTExpression expr) {
			IASTNode parent = expr.getParent();
			IASTNode child = expr;

			while (parent instanceof IASTUnaryExpression) {
				int operator = ((IASTUnaryExpression) parent).getOperator();
				if (operator != IASTUnaryExpression.op_bracketedPrimary) {
					return false;
				}
				child = parent;
				parent = parent.getParent();
			}
			if (parent instanceof IASTExpressionStatement) {
				return true;
			}

			if (child instanceof IASTExpression && checkNestedLeftSide((IASTExpression) child)) {
				return true;
			}

			return false;
		}

		private boolean isCallToNodiscardFunction(IASTExpression expr) {
			if (expr instanceof IASTFunctionCallExpression) {
				IASTFunctionCallExpression func = (IASTFunctionCallExpression) expr;
				IASTExpression functionNameExpression = func.getFunctionNameExpression();

				IASTName name;
				if (functionNameExpression instanceof IASTIdExpression) {
					name = ((IASTIdExpression) functionNameExpression).getName();
				} else if (functionNameExpression instanceof IASTFieldReference) {
					name = ((IASTFieldReference) functionNameExpression).getFieldName();
				} else {
					return false;
				}
				IBinding binding = name.resolveBinding();

				if (binding instanceof IFunction && ((IFunction) binding).isNoDiscard()) {
					res = name;
					return true;
				} else if (binding instanceof ICPPClassType && expr instanceof ICPPASTExpression
						&& checkEvaluation((ICPPASTExpression) expr)) {
					res = name;
					return true;
				}

				return false;
			} else if (expr instanceof ICPPASTCastExpression) {
				ICPPASTCastExpression cast = (ICPPASTCastExpression) expr;
				if (cast.getOperator() != ICPPASTCastExpression.op_static_cast) {
					return false;
				}
				if (checkEvaluation(cast)) {
					res = cast.getTypeId();
					return true;
				}
				return false;
			} else
				return false;
		}

		/**
		 * Check if the expression is 'nodiscard' looking for its evaluation
		 * @param expr A cpp expression
		 * @return True if nodiscard, false otherwise
		 */
		private boolean checkEvaluation(ICPPASTExpression expr) {
			ICPPEvaluation eval = expr.getEvaluation();
			if (eval instanceof EvalTypeId) {
				ICPPFunction evalFunc = ((EvalTypeId) eval).getConstructor();
				if (evalFunc != null && evalFunc.isNoDiscard()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Check if the function call is a left side of a nested expression. If it's a
		 * left side we need to go ahead and we need to evaluate the expression since it
		 * can be a discarded expression.
		 * @param func The expression list
		 * @return True if it's a nested expression and it's the left side, false otherwise
		 */
		private boolean checkNestedLeftSide(IASTExpression expr) {
			if (expr.getPropertyInParent() == IASTExpressionList.NESTED_EXPRESSION) {
				IASTExpressionList list = (IASTExpressionList) expr.getParent();
				IASTExpression[] allExpr = list.getExpressions();
				if (expr != allExpr[allExpr.length - 1])
					return true;
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
