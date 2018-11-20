/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalNaryTypeId;

public class CPPASTNaryTypeIdExpression extends ASTNode implements ICPPASTNaryTypeIdExpression {
	private Operator fOperator;
	private ICPPASTTypeId[] fOperands;
	private ICPPEvaluation fEvaluation;

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
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
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
		if (fEvaluation == null) {
			IType[] types = new IType[fOperands.length];
			for (int i = 0; i < fOperands.length; i++) {
				types[i] = CPPVisitor.createType(fOperands[i]);
				if (types[i] == null) {
					fEvaluation = EvalFixed.INCOMPLETE;
					break;
				}
			}
			fEvaluation = new EvalNaryTypeId(fOperator, types, this);
		}
		return fEvaluation;
	}

	@Override
	public IType getExpressionType() {
		return CPPEvaluation.getType(this);
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
