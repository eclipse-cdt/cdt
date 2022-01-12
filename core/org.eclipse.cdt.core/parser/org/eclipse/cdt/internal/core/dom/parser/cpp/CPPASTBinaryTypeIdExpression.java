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

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinaryTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

public class CPPASTBinaryTypeIdExpression extends ASTNode implements ICPPASTExpression, IASTBinaryTypeIdExpression {
	private Operator fOperator;
	private IASTTypeId fOperand1;
	private IASTTypeId fOperand2;
	private ICPPEvaluation fEvaluation;

	public CPPASTBinaryTypeIdExpression() {
	}

	public CPPASTBinaryTypeIdExpression(Operator op, IASTTypeId typeId1, IASTTypeId typeId2) {
		fOperator = op;
		setOperand1(typeId1);
		setOperand2(typeId2);
	}

	@Override
	public CPPASTBinaryTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTBinaryTypeIdExpression copy(CopyStyle style) {
		CPPASTBinaryTypeIdExpression copy = new CPPASTBinaryTypeIdExpression(fOperator,
				fOperand1 == null ? null : fOperand1.copy(style), fOperand2 == null ? null : fOperand2.copy(style));
		return copy(copy, style);
	}

	@Override
	public Operator getOperator() {
		return fOperator;
	}

	@Override
	public void setOperator(Operator value) {
		assertNotFrozen();
		fOperator = value;
	}

	@Override
	public void setOperand1(IASTTypeId typeId) {
		assertNotFrozen();
		fOperand1 = typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(OPERAND1);
		}
	}

	@Override
	public void setOperand2(IASTTypeId typeId) {
		assertNotFrozen();
		fOperand2 = typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(OPERAND2);
		}
	}

	@Override
	public IASTTypeId getOperand1() {
		return fOperand1;
	}

	@Override
	public IASTTypeId getOperand2() {
		return fOperand2;
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

		if (fOperand1 != null && !fOperand1.accept(action))
			return false;
		if (fOperand2 != null && !fOperand2.accept(action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			if (fOperand1 == null || fOperand2 == null) {
				fEvaluation = EvalFixed.INCOMPLETE;
			} else {
				IType t1 = CPPVisitor.createType(fOperand1);
				IType t2 = CPPVisitor.createType(fOperand2);
				if (t1 == null || t2 == null) {
					fEvaluation = EvalFixed.INCOMPLETE;
				} else {
					fEvaluation = new EvalBinaryTypeId(fOperator, t1, t2, this);
				}
			}
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
		return PRVALUE;
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY; // Binary type-id expressions don't call destructors.
	}
}
