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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnaryTypeID;

public class CPPASTTypeIdExpression extends ASTNode implements ICPPASTTypeIdExpression {
	private int fOperator;
	private IASTTypeId fTypeId;
	private ICPPEvaluation fEvaluation;

	public CPPASTTypeIdExpression() {
	}

	public CPPASTTypeIdExpression(int op, IASTTypeId typeId) {
		this.fOperator = op;
		setTypeId(typeId);
	}

	@Override
	public CPPASTTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTypeIdExpression copy(CopyStyle style) {
		CPPASTTypeIdExpression copy = new CPPASTTypeIdExpression(fOperator,
				fTypeId == null ? null : fTypeId.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
		return fOperator;
	}

	@Override
	public void setOperator(int value) {
		assertNotFrozen();
		fOperator = value;
	}

	@Override
	public void setTypeId(IASTTypeId typeId) {
		assertNotFrozen();
		this.fTypeId = typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
	}

	@Override
	public IASTTypeId getTypeId() {
		return fTypeId;
	}

	@Override
	public IASTImplicitDestructorName[] getImplicitDestructorNames() {
		return IASTImplicitDestructorName.EMPTY_NAME_ARRAY; // Type-id expression does not call destructors.
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

		if (fTypeId != null && !fTypeId.accept(action))
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
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			IType type = CPPVisitor.createType(fTypeId);
			if (type == null || type instanceof IProblemType) {
				fEvaluation = EvalFixed.INCOMPLETE;
			} else {
				fEvaluation = new EvalUnaryTypeID(fOperator, type, this);
			}
		}
		return fEvaluation;
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
