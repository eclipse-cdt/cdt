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
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

/**
 * This checker finds a problems which are caused by lack of understanding
 * operator
 * precedence in C. In any case it is better to surround expressions in
 * parenthesis to improve readability. Example: ! x>0 && x<10 (this would be
 * (!x)>0 && x<10 in C) We only look for &&, || and ! operators (and binary | &
 * ^ ~)
 * 
 * @author Alena
 * 
 */
public class SuggestedParenthesisChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.SuggestedParenthesisProblem"; //$NON-NLS-1$
	public static final String PARAM_NOT = "paramNot"; //$NON-NLS-1$



	public void processAst(IASTTranslationUnit ast) {
		// traverse the ast using the visitor pattern.
		ast.accept(new ExpressionVisitor());
	}

	class ExpressionVisitor extends ASTVisitor {
		ExpressionVisitor() {
			shouldVisitExpressions = true;
		}

		public int visit(IASTExpression expression) {
			int precedence = getPrecedence(expression);
			IASTNode parent = expression.getParent();
			if (parent instanceof IASTExpression) {
				IASTExpression parentExpr = (IASTExpression) parent;
				if (isInParentesis(expression))
					return PROCESS_CONTINUE;
				if (precedence == 2) { // unary not
					if (isParamNot() && isUsedAsOperand(expression)) {
						reportProblem(ER_ID, expression);
						return PROCESS_SKIP;
					}
				} else if (precedence >= 0) {
					int pp = getPrecedence(parentExpr);
					if (pp == -1 || pp == precedence)
						return PROCESS_CONTINUE;
					reportProblem(ER_ID, expression);
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean isUsedAsOperand(IASTExpression expression) {
			ASTNodeProperty prop = expression.getPropertyInParent();
			if (prop == IASTBinaryExpression.OPERAND_ONE
			// || prop == IASTBinaryExpression.OPERAND_TWO
					|| prop == IASTUnaryExpression.OPERAND)
				return true;
			return false;
		}
	}

	private int getPrecedence(IASTExpression e) {
		if (e instanceof IASTBinaryExpression) {
			IASTBinaryExpression binExpr = (IASTBinaryExpression) e;
			int operator = binExpr.getOperator();
			if (operator == IASTBinaryExpression.op_binaryAnd)
				return 8;
			if (operator == IASTBinaryExpression.op_binaryXor)
				return 9;
			if (operator == IASTBinaryExpression.op_binaryOr)
				return 10;
			if (operator == IASTBinaryExpression.op_logicalAnd)
				return 11;
			if (operator == IASTBinaryExpression.op_logicalOr)
				return 12;
		}
		if (e instanceof IASTUnaryExpression) {
			IASTUnaryExpression binExpr = (IASTUnaryExpression) e;
			int operator = binExpr.getOperator();
			if (operator == IASTUnaryExpression.op_not)
				return 2;
			if (operator == IASTUnaryExpression.op_tilde)
				return 2;
		}
		return -1;
	}

	/**
	 * @param parent
	 * @return
	 */
	private boolean isInParentesis(IASTExpression node) {
		IASTNode parent = node.getParent();
		if (parent instanceof IASTUnaryExpression) {
			IASTUnaryExpression br = (IASTUnaryExpression) parent;
			if (br.getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
				return true;
			}
		}
		return false;
	}

	public boolean isParamNot() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()),
				PARAM_NOT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences
	 * #initPreferences(org.eclipse.cdt.codan.core.model.IProblemWorkingCopy)
	 */
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_NOT,
				"Suggest parentesis around not operator", Boolean.FALSE);
	}
}
