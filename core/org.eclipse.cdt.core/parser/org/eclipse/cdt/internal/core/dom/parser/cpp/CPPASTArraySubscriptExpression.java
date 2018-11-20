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
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

public class CPPASTArraySubscriptExpression extends ASTNode
		implements ICPPASTArraySubscriptExpression, IASTAmbiguityParent {
	private ICPPASTExpression arrayExpression;
	private ICPPASTInitializerClause subscriptExp;
	private ICPPEvaluation evaluation;
	private IASTImplicitName[] implicitNames;

	public CPPASTArraySubscriptExpression() {
	}

	public CPPASTArraySubscriptExpression(IASTExpression arrayExpression, IASTInitializerClause operand) {
		setArrayExpression(arrayExpression);
		setArgument(operand);
	}

	@Override
	public CPPASTArraySubscriptExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTArraySubscriptExpression copy(CopyStyle style) {
		CPPASTArraySubscriptExpression copy = new CPPASTArraySubscriptExpression();
		copy.setArrayExpression(arrayExpression == null ? null : arrayExpression.copy(style));
		copy.setArgument(subscriptExp == null ? null : subscriptExp.copy(style));
		return copy(copy, style);
	}

	@Override
	public ICPPASTExpression getArrayExpression() {
		return arrayExpression;
	}

	@Override
	public void setArrayExpression(IASTExpression expression) {
		assertNotFrozen();
		if (expression != null) {
			if (!(expression instanceof ICPPASTExpression))
				throw new IllegalArgumentException(expression.getClass().getName());
			expression.setParent(this);
			expression.setPropertyInParent(ARRAY);
		}
		arrayExpression = (ICPPASTExpression) expression;
	}

	@Override
	public ICPPASTInitializerClause getArgument() {
		return subscriptExp;
	}

	@Override
	public void setArgument(IASTInitializerClause arg) {
		assertNotFrozen();
		if (arg != null) {
			if (!(arg instanceof ICPPASTInitializerClause))
				throw new IllegalArgumentException(arg.getClass().getName());
			arg.setParent(this);
			arg.setPropertyInParent(SUBSCRIPT);
		}
		subscriptExp = (ICPPASTInitializerClause) arg;
	}

	@Override
	@Deprecated
	public IASTExpression getSubscriptExpression() {
		if (subscriptExp instanceof IASTExpression)
			return (IASTExpression) subscriptExp;
		return null;
	}

	@Override
	@Deprecated
	public void setSubscriptExpression(IASTExpression expression) {
		setArgument(expression);
	}

	@Override
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPFunction overload = getOverload();
			if (overload == null || overload instanceof CPPImplicitFunction)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;

			// create separate implicit names for the two brackets
			CPPASTImplicitName n1 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n1.setBinding(overload);
			n1.computeOperatorOffsets(arrayExpression, true);

			CPPASTImplicitName n2 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n2.setBinding(overload);
			n2.computeOperatorOffsets(subscriptExp, true);
			n2.setAlternate(true);

			implicitNames = new IASTImplicitName[] { n1, n2 };
		}

		return implicitNames;
	}

	private ICPPFunction getOverload() {
		ICPPEvaluation eval = getEvaluation();
		if (eval instanceof EvalBinary) {
			CPPSemantics.pushLookupPoint(this);
			try {
				return ((EvalBinary) eval).getOverload();
			} finally {
				CPPSemantics.popLookupPoint();
			}
		}
		return null;
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
		if (arrayExpression != null && !arrayExpression.accept(action))
			return false;

		IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;

		if (implicits != null && implicits.length > 0 && !implicits[0].accept(action))
			return false;

		if (subscriptExp != null && !subscriptExp.accept(action))
			return false;

		if (implicits != null && implicits.length > 0 && !implicits[1].accept(action))
			return false;

		if (action.shouldVisitExpressions) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == subscriptExp) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			subscriptExp = (ICPPASTExpression) other;
		}
		if (child == arrayExpression) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			arrayExpression = (ICPPASTExpression) other;
		}
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (evaluation == null)
			evaluation = computeEvaluation();

		return evaluation;
	}

	private ICPPEvaluation computeEvaluation() {
		if (arrayExpression == null || subscriptExp == null)
			return EvalFixed.INCOMPLETE;
		return new EvalBinary(EvalBinary.op_arrayAccess, arrayExpression.getEvaluation(), subscriptExp.getEvaluation(),
				this);
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

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY;
	}
}
