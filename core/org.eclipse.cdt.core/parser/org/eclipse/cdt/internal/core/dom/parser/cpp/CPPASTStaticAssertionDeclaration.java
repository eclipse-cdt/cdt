/*******************************************************************************
 * Copyright (c) 2009, 2013, 2025 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecStaticAssert;

public class CPPASTStaticAssertionDeclaration extends ASTNode
		implements ICPPASTStaticAssertDeclaration, IASTAmbiguityParent, ICPPExecutionOwner {

	private IASTExpression fCondition;
	private final ICPPASTLiteralExpression fMessage;

	public static final ICPPEvaluation STATIC_ASSERT_FAILED = new EvalFixed(ProblemType.STATIC_ASSERT_FAILED, PRVALUE,
			IntegralValue.STATIC_ASSERT_FAILED_ERROR);

	/**
	 * Constructor for C++17 static_assert with only a condition.
	 *
	 * @param condition The condition of the static assertion
	 */
	public CPPASTStaticAssertionDeclaration(IASTExpression condition) {
		this(condition, null);
	}

	public CPPASTStaticAssertionDeclaration(IASTExpression condition, ICPPASTLiteralExpression message) {
		fCondition = condition;
		fMessage = message;
		if (condition != null) {
			condition.setParent(this);
			condition.setPropertyInParent(CONDITION);
		}
		if (message != null) {
			message.setParent(this);
			message.setPropertyInParent(MESSAGE);
		}
	}

	@Override
	public IASTExpression getCondition() {
		return fCondition;
	}

	@Override
	public ICPPASTLiteralExpression getMessage() {
		return fMessage;
	}

	@Override
	public CPPASTStaticAssertionDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTStaticAssertionDeclaration copy(CopyStyle style) {
		final IASTExpression condCopy = fCondition == null ? null : fCondition.copy(style);
		final ICPPASTLiteralExpression msgCopy = fMessage == null ? null : fMessage.copy(style);
		CPPASTStaticAssertionDeclaration copy = new CPPASTStaticAssertionDeclaration(condCopy, msgCopy);
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}
		}

		if (fCondition != null && !fCondition.accept(action))
			return false;
		if (fMessage != null && !fMessage.accept(action))
			return false;

		if (action.shouldVisitDeclarations && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fCondition) {
			fCondition = (IASTExpression) other;
			other.setParent(child.getParent());
			other.setPropertyInParent(child.getPropertyInParent());
		}
	}

	@Override
	public ICPPExecution getExecution() {
		// Naturally this would be compilation error; simulate executing this statement via ExecStaticAssert.
		// If no evaluation of condition is available, treat it as unsatisfied condition too.
		final ICPPEvaluation conditionExprEval = getCondition() instanceof ICPPASTExpression conditionExpr
				? conditionExpr.getEvaluation()
				: ExecStaticAssert.FAILED;

		return new ExecStaticAssert(conditionExprEval);
	}
}
