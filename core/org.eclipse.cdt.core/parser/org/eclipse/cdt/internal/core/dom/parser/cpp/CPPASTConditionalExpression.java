/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConditional;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

public class CPPASTConditionalExpression extends ASTNode
		implements IASTConditionalExpression, ICPPASTExpression, IASTAmbiguityParent {
	private ICPPASTExpression fCondition;
	private ICPPASTExpression fPositive;
	private ICPPASTExpression fNegative;
	private ICPPEvaluation fEval;
	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	public CPPASTConditionalExpression() {
	}

	public CPPASTConditionalExpression(IASTExpression condition, IASTExpression postive, IASTExpression negative) {
		setLogicalConditionExpression(condition);
		setPositiveResultExpression(postive);
		setNegativeResultExpression(negative);
	}

	@Override
	public CPPASTConditionalExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTConditionalExpression copy(CopyStyle style) {
		CPPASTConditionalExpression copy = new CPPASTConditionalExpression();
		copy.setLogicalConditionExpression(fCondition == null ? null : fCondition.copy(style));
		copy.setPositiveResultExpression(fPositive == null ? null : fPositive.copy(style));
		copy.setNegativeResultExpression(fNegative == null ? null : fNegative.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getLogicalConditionExpression() {
		return fCondition;
	}

	@Override
	public void setLogicalConditionExpression(IASTExpression expression) {
		assertNotFrozen();
		fCondition = (ICPPASTExpression) expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(LOGICAL_CONDITION);
		}
	}

	@Override
	public IASTExpression getPositiveResultExpression() {
		return fPositive;
	}

	@Override
	public void setPositiveResultExpression(IASTExpression expression) {
		assertNotFrozen();
		this.fPositive = (ICPPASTExpression) expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(POSITIVE_RESULT);
		}
	}

	@Override
	public IASTExpression getNegativeResultExpression() {
		return fNegative;
	}

	@Override
	public void setNegativeResultExpression(IASTExpression expression) {
		assertNotFrozen();
		this.fNegative = (ICPPASTExpression) expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEGATIVE_RESULT);
		}
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		if (fImplicitDestructorNames == null) {
			fImplicitDestructorNames = DestructorCallCollector.getTemporariesDestructorCalls(this);
		}

		return fImplicitDestructorNames;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		if (fCondition != null && !fCondition.accept(action))
			return false;
		if (fPositive != null && !fPositive.accept(action))
			return false;
		if (fNegative != null && !fNegative.accept(action))
			return false;

		if (action.shouldVisitImplicitDestructorNames && !acceptByNodes(getImplicitDestructorNames(), action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fCondition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fCondition = (ICPPASTExpression) other;
		}
		if (child == fPositive) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fPositive = (ICPPASTExpression) other;
		}
		if (child == fNegative) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fNegative = (ICPPASTExpression) other;
		}
	}

	private boolean isThrowExpression(IASTExpression expr) {
		while (expr instanceof IASTUnaryExpression) {
			final IASTUnaryExpression unaryExpr = (IASTUnaryExpression) expr;
			final int op = unaryExpr.getOperator();
			if (op == IASTUnaryExpression.op_throw) {
				return true;
			} else if (op == IASTUnaryExpression.op_bracketedPrimary) {
				expr = unaryExpr.getOperand();
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEval == null) {
			if (fCondition == null || fNegative == null) {
				fEval = EvalFixed.INCOMPLETE;
			} else {
				final ICPPEvaluation condEval = fCondition.getEvaluation();
				final ICPPEvaluation posEval = fPositive == null ? null : fPositive.getEvaluation();
				final ICPPEvaluation negEval = fNegative.getEvaluation();
				fEval = new EvalConditional(condEval, posEval, negEval, isThrowExpression(fPositive),
						isThrowExpression(fNegative), this);
			}
		}
		return fEval;
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
	}

	@Override
	public ValueCategory getValueCategory() {
		return CPPEvaluation.getValueCategory(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}
}
