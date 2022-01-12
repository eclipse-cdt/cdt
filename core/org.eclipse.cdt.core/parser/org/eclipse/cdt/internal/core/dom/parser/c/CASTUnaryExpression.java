/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.restoreTypedefs;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Unary expression in C.
 */
public class CASTUnaryExpression extends ASTNode implements IASTUnaryExpression, IASTAmbiguityParent {
	private int operator;
	private IASTExpression operand;

	public CASTUnaryExpression() {
	}

	public CASTUnaryExpression(int operator, IASTExpression operand) {
		this.operator = operator;
		setOperand(operand);
	}

	@Override
	public CASTUnaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTUnaryExpression copy(CopyStyle style) {
		CASTUnaryExpression copy = new CASTUnaryExpression(operator, operand == null ? null : operand.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
		return operator;
	}

	@Override
	public void setOperator(int value) {
		assertNotFrozen();
		this.operator = value;
	}

	@Override
	public IASTExpression getOperand() {
		return operand;
	}

	@Override
	public void setOperand(IASTExpression expression) {
		assertNotFrozen();
		operand = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND);
		}
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

		if (operand != null && !operand.accept(action))
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
		if (child == operand) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand = (IASTExpression) other;
		}
	}

	@Override
	public IType getExpressionType() {
		int op = getOperator();
		if (op == op_sizeof) {
			return CVisitor.get_SIZE_T(this);
		}
		final IType exprType = getOperand().getExpressionType();
		IType type = CVisitor.unwrapTypedefs(exprType);
		switch (op) {
		case op_star:
			if (type instanceof IPointerType || type instanceof IArrayType) {
				return ((ITypeContainer) type).getType();
			}
			break;
		case op_amper:
			return new CPointerType(exprType, 0);
		case op_minus:
		case op_plus:
		case op_tilde:
			IType t = CArithmeticConversion.promoteCType(type);
			if (t != null) {
				return restoreTypedefs(t, exprType);
			}
			break;
		}
		return exprType; // Return the original.
	}

	@Override
	public boolean isLValue() {
		switch (getOperator()) {
		case op_bracketedPrimary:
			return getOperand().isLValue();
		case op_star:
		case op_prefixDecr:
		case op_prefixIncr:
			return true;
		default:
			return false;
		}
	}

	@Override
	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}
}
