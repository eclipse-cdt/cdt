/*******************************************************************************
 * Copyright (c) 2022 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFoldExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.DestructorCallCollector;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFoldExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalPackExpansion;

/**
 * Implementation for fold expressions.
 */
public class CPPASTFoldExpression extends ASTNode implements ICPPASTFoldExpression, IASTAmbiguityParent {
	private final int fOperator;
	private final boolean fIsComma;
	private ICPPASTExpression fLhs;
	private ICPPASTExpression fRhs;
	private ICPPEvaluation fEvaluation;

	private IASTImplicitDestructorName[] fImplicitDestructorNames;

	public CPPASTFoldExpression(int operator, boolean isComma, IASTExpression lhs, IASTExpression rhs) {
		fOperator = operator;
		fIsComma = isComma;
		setOperand1(lhs);
		setOperand2(rhs);
	}

	private void setOperand1(IASTExpression expression) {
		assertNotFrozen();
		if (expression != null) {
			if (!(expression instanceof ICPPASTExpression)) {
				throw new IllegalArgumentException(expression.getClass().getName());
			}

			expression.setParent(this);
		}
		fLhs = (ICPPASTExpression) expression;
	}

	public void setOperand2(IASTExpression operand) {
		assertNotFrozen();
		if (operand != null) {
			if (!(operand instanceof ICPPASTExpression)) {
				throw new IllegalArgumentException(operand.getClass().getName());
			}
			operand.setParent(this);
		}
		fRhs = (ICPPASTExpression) operand;
	}

	@Override
	public IASTExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTExpression copy(CopyStyle style) {
		IASTExpression fLhsCopy = fLhs == null ? null : fLhs.copy(style);
		IASTExpression fRhsCopy = fRhs == null ? null : fRhs.copy(style);

		CPPASTFoldExpression copy = new CPPASTFoldExpression(fOperator, fIsComma, fLhsCopy, fRhsCopy);
		return copy(copy, style);
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			fEvaluation = computeEvaluation();
		}

		return fEvaluation;
	}

	private final class UnexpandedParameterPackCounter extends ASTVisitor {
		int count;

		public UnexpandedParameterPackCounter() {
			super(false);
			shouldVisitExpressions = true;
			shouldVisitTypeIds = true;
			count = 0;
		}

		public int getCount() {
			return count;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof ICPPASTPackExpansionExpression) {
				return PROCESS_SKIP;
			}

			IType type = expression.getExpressionType();
			if (type instanceof ICPPParameterPackType) {
				++count;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTTypeId typeId) {
			IType type = CPPVisitor.createType(typeId);
			if (type instanceof ICPPTemplateParameter templateParameter) {
				if (templateParameter.isParameterPack()) {
					++count;
				} else {
					boolean notParameterPack = true;
				}
			} else {
				boolean notTemplateParameter = true;
			}
			return PROCESS_CONTINUE;
		}
	}

	private int countUnexpandedParameterPacks(IASTExpression e) {
		if (e == null) {
			return 0;
		}
		UnexpandedParameterPackCounter counter = new UnexpandedParameterPackCounter();
		e.accept(counter);
		return counter.getCount();
	}

	private ICPPEvaluation computeEvaluation() {
		int lhsParameterPackCount = countUnexpandedParameterPacks(fLhs);
		int rhsParameterPackCount = countUnexpandedParameterPacks(fRhs);

		// Either left or right hand side expression shall contain an unexpanded parameter pack,
		// but not both.
		if (!((lhsParameterPackCount != 0) ^ (rhsParameterPackCount != 0))) {
			return EvalFixed.INCOMPLETE;
		}

		ICPPEvaluation packEval;
		ICPPEvaluation initEval;
		boolean isLeftFold;

		ICPPEvaluation evalL = fLhs == null ? null : fLhs.getEvaluation();
		ICPPEvaluation evalR = fRhs == null ? null : fRhs.getEvaluation();

		if (lhsParameterPackCount == 0) {
			isLeftFold = true;
			initEval = evalL;
			packEval = evalR;
		} else {
			isLeftFold = false;
			initEval = evalR;
			packEval = evalL;
		}

		ICPPEvaluation[] foldPattern = new ICPPEvaluation[] { new EvalPackExpansion(packEval, this) };
		return new EvalFoldExpression(fOperator, fIsComma, isLeftFold, foldPattern, initEval, this);
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
	}

	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public ValueCategory getValueCategory() {
		return CPPEvaluation.getValueCategory(this);
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

		if (fLhs != null && !fLhs.accept(action)) {
			return false;
		}

		if (fRhs != null && !fRhs.accept(action)) {
			return false;
		}

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == fLhs) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fLhs = (ICPPASTExpression) other;
		}
		if (child == fRhs) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			fRhs = (ICPPASTExpression) other;
		}
	}
}
