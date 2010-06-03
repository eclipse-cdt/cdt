/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;

/**
 * Checker that detects statements without effect such as
 * 
 * a+b;
 * or
 * +b;
 * 
 * 
 */
public class StatementHasNoEffectChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.StatementHasNoEffectProblem"; //$NON-NLS-1$
	public static final String PARAM_MACRO_ID = "macro"; //$NON-NLS-1$
	public static final String PARAM_EXCEPT_ARG_LIST = "exceptions"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new CheckStmpVisitor());
	}

	class CheckStmpVisitor extends ASTVisitor {
		CheckStmpVisitor() {
			shouldVisitStatements = true;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTExpressionStatement) {
				IASTExpression expression = ((IASTExpressionStatement) stmt)
						.getExpression();
				if (hasNoEffect(expression)) {
					boolean inMacro = CxxAstUtils.getInstance().isInMacro(
							expression);
					boolean shouldReportInMacro = shouldReportInMacro();
					if (inMacro && !shouldReportInMacro)
						return PROCESS_SKIP;
					String arg = expression.getRawSignature();
					if (!isFilteredArg(arg))
						reportProblem(ER_ID, stmt, arg);
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
				if (isPossibleAssignment(binExpr))
					return false;
				switch (binExpr.getOperator()) {
					case IASTBinaryExpression.op_logicalOr:
					case IASTBinaryExpression.op_logicalAnd:
						return hasNoEffect(binExpr.getOperand1())
								&& hasNoEffect(binExpr.getOperand2());
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
					case IASTUnaryExpression.op_throw:
						return false;
					case IASTUnaryExpression.op_bracketedPrimary:
						return hasNoEffect(unaryExpr.getOperand());
				}
				return true;
			}
			// simply a;
			if (e instanceof IASTIdExpression) {
				// check if it is part of GNU comp stmt expression i.e. ({foo();a;})
				IASTNode parent = e.getParent();
				if (parent instanceof IASTExpressionStatement) {
					IASTNode parent2 = parent.getParent();
					if (parent2 instanceof IASTCompoundStatement) {
						IASTNode parent3 = parent2.getParent();
						if (parent3 instanceof IGNUASTCompoundStatementExpression) {
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}
	}

	public void initPreferences(IProblemWorkingCopy problem) {
		addPreference(problem, PARAM_MACRO_ID,
				CheckersMessages.StatementHasNoEffectChecker_ParameterMacro,
				Boolean.TRUE);
		addListPreference(
				problem,
				PARAM_EXCEPT_ARG_LIST,
				CheckersMessages.GenericParameter_ParameterExceptions,
				CheckersMessages.GenericParameter_ParameterExceptionsItem);
	}

	/**s
	 * @param paramExceptArgList
	 * @param arg
	 * @return
	 */
	public boolean isFilteredArg(String arg) {
		Object[] arr = (Object[]) getPreference(
				getProblemById(ER_ID, getFile()), PARAM_EXCEPT_ARG_LIST);
		for (int i = 0; i < arr.length; i++) {
			String str = (String) arr[i];
			if (arg.equals(str))
				return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	public boolean shouldReportInMacro() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()),
				PARAM_MACRO_ID);
	}

	@SuppressWarnings("restriction")
	public boolean isPossibleAssignment(IASTBinaryExpression expr) {
		switch (expr.getOperator()) {
			case IASTBinaryExpression.op_assign:
			case IASTBinaryExpression.op_binaryAndAssign:
			case IASTBinaryExpression.op_binaryOrAssign:
			case IASTBinaryExpression.op_binaryXorAssign:
			case IASTBinaryExpression.op_divideAssign:
			case IASTBinaryExpression.op_minusAssign:
			case IASTBinaryExpression.op_moduloAssign:
			case IASTBinaryExpression.op_multiplyAssign:
			case IASTBinaryExpression.op_plusAssign:
			case IASTBinaryExpression.op_shiftLeftAssign:
			case IASTBinaryExpression.op_shiftRightAssign:
				return true;
		}
		if (expr instanceof CPPASTBinaryExpression) {
			// unfortunately ICPPASTBinaryExpression does not have
			// getOverload public method
			CPPASTBinaryExpression cppBin = (CPPASTBinaryExpression) expr;
			ICPPFunction overload = cppBin.getOverload();
			if (overload != null)
				return true;
			IType expressionType = cppBin.getOperand1().getExpressionType();
			if (!(expressionType instanceof IBasicType)) {
				return true; // must be overloaded but parser could not
				// find it
			}
		}
		return false;
	}
}
