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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.ListProblemPreference;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class MagicNumberChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.MagicNumberProblem"; //$NON-NLS-1$
	private static final String OPERATOR_PAREN = "operator ()"; //$NON-NLS-1$
	public static final String PARAM_ARRAY = "checkArray"; //$NON-NLS-1$
	public static final String PARAM_EXCEPTIONS = "exceptions"; //$NON-NLS-1$
	/**
	 * Operator() is often used for matrix manipulation
	 */
	public static final String PARAM_OPERATOR_PAREN = "checkOperatorParen"; //$NON-NLS-1$
	/**
	 * As default we allow the use of zero, one, minus one and two (used often as modulo value and other bit operations) as int
	 */
	private Set<Long> allowedLongValues = new HashSet<>();
	/**
	 * As default we allow the use of zero, one, minus one as float
	 */
	private Set<Double> allowedDoubleValues = new HashSet<>();
	private boolean checkArray;
	private boolean checkOperatorParen;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_ARRAY, CheckersMessages.MagicNumberChecker_ParameterArray, Boolean.TRUE);
		addPreference(problem, PARAM_OPERATOR_PAREN, CheckersMessages.MagicNumberChecker_ParameterOperatorParen,
				Boolean.TRUE);
		ListProblemPreference list = addListPreference(problem, PARAM_EXCEPTIONS,
				CheckersMessages.GenericParameter_ParameterExceptions,
				CheckersMessages.GenericParameter_ParameterExceptionsItem);
		list.addChildValue("1"); //$NON-NLS-1$
		list.addChildValue("0"); //$NON-NLS-1$
		list.addChildValue("-1"); //$NON-NLS-1$
		list.addChildValue("2"); //$NON-NLS-1$
		list.addChildValue("1.0"); //$NON-NLS-1$
		list.addChildValue("0.0"); //$NON-NLS-1$
		list.addChildValue("-1.0"); //$NON-NLS-1$
	}

	private void initExceptions() {
		allowedLongValues.clear();
		allowedDoubleValues.clear();
		Object[] arr = (Object[]) getPreference(getProblemById(ERR_ID, getFile()), PARAM_EXCEPTIONS);
		for (Object o : arr) {
			String s = (String) o;
			try {
				allowedLongValues.add(Long.parseLong(s));
			} catch (NumberFormatException e) {
				try {
					allowedDoubleValues.add(Double.parseDouble(s));
				} catch (NumberFormatException e1) {
				}
			}
		}
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		final IProblem pt = getProblemById(ERR_ID, getFile());
		checkArray = (Boolean) getPreference(pt, PARAM_ARRAY);
		checkOperatorParen = (Boolean) getPreference(pt, PARAM_OPERATOR_PAREN);
		initExceptions();
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					/**
					 * Check if we are inside an initializer, if so it's ok
					 */
					IASTInitializer node = ASTQueries.findAncestorWithType(expression, IASTInitializer.class);
					if (node != null)
						return PROCESS_CONTINUE;
					/**
					 * Check if the literal is used for an array
					 */
					IASTNode parent = expression.getParent();
					if (!checkArray && (parent instanceof IASTArrayModifier || parent instanceof IASTArrayDeclarator
							|| parent instanceof IASTArraySubscriptExpression))
						return PROCESS_CONTINUE;
					/**
					 * Check if the literal is used in operator() of a class instance
					 */
					if (!checkOperatorParen && isOperatorParen(expression)) {
						return PROCESS_CONTINUE;
					}
					IASTLiteralExpression literal = (IASTLiteralExpression) expression;
					int kind = literal.getKind();
					Number value;
					switch (kind) {
					case IASTLiteralExpression.lk_float_constant:
						value = ValueFactory.getConstantNumericalValue(expression);
						if (!allowedDoubleValues.contains(value.doubleValue()))
							reportProblem(ERR_ID, expression);
						break;
					case IASTLiteralExpression.lk_integer_constant:
						value = ValueFactory.getConstantNumericalValue(expression);
						if (!allowedLongValues.contains(value.longValue()))
							reportProblem(ERR_ID, expression);
						break;
					default:
						return PROCESS_CONTINUE;
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isOperatorParen(IASTExpression expression) {
		ICPPASTFunctionCallExpression func = ASTQueries.findAncestorWithType(expression,
				ICPPASTFunctionCallExpression.class);
		if (func == null)
			return false;

		IASTExpression funcName = func.getFunctionNameExpression();
		if (!(funcName instanceof IASTIdExpression))
			return false;

		IType type = SemanticUtil.getUltimateType(funcName.getExpressionType(), true);
		if (type instanceof ICPPClassType) {
			ICPPMethod[] methods = ((ICPPClassType) type).getAllDeclaredMethods();
			for (ICPPMethod m : methods) {
				if (OPERATOR_PAREN.equals(m.getName())) {
					return true;
				}
			}
		}
		return false;
	}
}
