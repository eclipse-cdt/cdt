/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

public class CPPASTNaryTypeIdExpression extends ASTNode implements ICPPASTNaryTypeIdExpression {
	private Operator fOperator;
	private ICPPASTTypeId[] fOperands;

	public CPPASTNaryTypeIdExpression(Operator operator, ICPPASTTypeId[] operands) {
		fOperator = operator;
		fOperands = operands;
		for (ICPPASTTypeId operand : fOperands) {
			operand.setParent(this);
			operand.setPropertyInParent(OPERAND);
		}
	}

	@Override
	public ICPPASTNaryTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public ICPPASTNaryTypeIdExpression copy(CopyStyle style) {
		ICPPASTTypeId[] operands = new ICPPASTTypeId[fOperands.length];
		for (int i = 0; i < fOperands.length; ++i) {
			operands[i] = fOperands[i].copy(style);
		}
		CPPASTNaryTypeIdExpression copy = new CPPASTNaryTypeIdExpression(fOperator, operands);
		return copy(copy, style);
	}

	@Override
	public Operator getOperator() {
		return fOperator;
	}

	@Override
	public ICPPASTTypeId[] getOperands() {
		return fOperands;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitExpressions) {
			switch (action.visit(this)) {
				case ASTVisitor.PROCESS_ABORT: return false;
				case ASTVisitor.PROCESS_SKIP: return true;
				default: break;
			}
		}

		for (ICPPASTTypeId operand : fOperands) {
			if (!operand.accept(action)) {
				return false;
			}
		}

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT) {
			return false;
		}

		return true;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		// TODO: Implement. This will need a new evaluation type, EvalNaryTypeId.
		return EvalFixed.INCOMPLETE;
	}

	@Override
	public IType getExpressionType() {
		// TODO: When getEvaluation() is implemented, delegate to getEvaluation().getType().
		return CPPBasicType.BOOLEAN;
	}

	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		// N-ary type-id expressions don't call destructors.
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY;
	}
}
