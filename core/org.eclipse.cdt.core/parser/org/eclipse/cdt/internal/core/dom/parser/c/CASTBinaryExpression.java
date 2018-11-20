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
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Binary expression for c
 */
public class CASTBinaryExpression extends ASTNode implements IASTBinaryExpression, IASTAmbiguityParent {
	private int op;
	private IASTExpression operand1;
	private IASTExpression operand2;

	public CASTBinaryExpression() {
	}

	public CASTBinaryExpression(int op, IASTExpression operand1, IASTExpression operand2) {
		this.op = op;
		setOperand1(operand1);
		setOperand2(operand2);
	}

	@Override
	public CASTBinaryExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTBinaryExpression copy(CopyStyle style) {
		CASTBinaryExpression copy = new CASTBinaryExpression();
		copy.op = op;
		copy.setOperand1(operand1 == null ? null : operand1.copy(style));
		copy.setOperand2(operand2 == null ? null : operand2.copy(style));
		return copy(copy, style);
	}

	@Override
	public int getOperator() {
		return op;
	}

	@Override
	public IASTExpression getOperand1() {
		return operand1;
	}

	@Override
	public IASTExpression getOperand2() {
		return operand2;
	}

	@Override
	public IASTInitializerClause getInitOperand2() {
		return operand2;
	}

	/**
	 * @param op An op_X field from {@link IASTBinaryExpression}
	 */
	@Override
	public void setOperator(int op) {
		assertNotFrozen();
		this.op = op;
	}

	@Override
	public void setOperand1(IASTExpression expression) {
		assertNotFrozen();
		operand1 = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_ONE);
		}
	}

	@Override
	public void setOperand2(IASTExpression expression) {
		assertNotFrozen();
		operand2 = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(OPERAND_TWO);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (operand1 instanceof IASTBinaryExpression || operand2 instanceof IASTBinaryExpression) {
			return acceptWithoutRecursion(this, action);
		}

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

		if (operand1 != null && !operand1.accept(action))
			return false;
		if (operand2 != null && !operand2.accept(action))
			return false;

		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	private static class N {
		final IASTBinaryExpression fExpression;
		int fState;
		N fNext;

		N(IASTBinaryExpression expr) {
			fExpression = expr;
		}
	}

	public static boolean acceptWithoutRecursion(IASTBinaryExpression bexpr, ASTVisitor action) {
		N stack = new N(bexpr);
		while (stack != null) {
			IASTBinaryExpression expr = stack.fExpression;
			if (stack.fState == 0) {
				if (action.shouldVisitExpressions) {
					switch (action.visit(expr)) {
					case ASTVisitor.PROCESS_ABORT:
						return false;
					case ASTVisitor.PROCESS_SKIP:
						stack = stack.fNext;
						continue;
					}
				}
				stack.fState = 1;
				IASTExpression op1 = expr.getOperand1();
				if (op1 instanceof IASTBinaryExpression) {
					N n = new N((IASTBinaryExpression) op1);
					n.fNext = stack;
					stack = n;
					continue;
				}
				if (op1 != null && !op1.accept(action))
					return false;
			}
			if (stack.fState == 1) {
				stack.fState = 2;

				IASTExpression op2 = expr.getOperand2();
				if (op2 instanceof IASTBinaryExpression) {
					N n = new N((IASTBinaryExpression) op2);
					n.fNext = stack;
					stack = n;
					continue;
				}
				if (op2 != null && !op2.accept(action))
					return false;
			}

			if (action.shouldVisitExpressions && action.leave(expr) == ASTVisitor.PROCESS_ABORT)
				return false;

			stack = stack.fNext;
		}

		return true;
	}

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == operand1) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand1 = (IASTExpression) other;
		}
		if (child == operand2) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			operand2 = (IASTExpression) other;
		}
	}

	@Override
	public IType getExpressionType() {
		final int op = getOperator();
		IType originalType1 = getOperand1().getExpressionType();
		IType originalType2 = getOperand2().getExpressionType();
		final IType type1 = CVisitor.unwrapTypedefs(originalType1);
		final IType type2 = CVisitor.unwrapTypedefs(originalType2);
		IType type = CArithmeticConversion.convertCOperandTypes(op, type1, type2);
		if (type != null) {
			return restoreTypedefs(type, originalType1, originalType2);
		}

		switch (op) {
		case op_lessEqual:
		case op_lessThan:
		case op_greaterEqual:
		case op_greaterThan:
		case op_logicalAnd:
		case op_logicalOr:
		case op_equals:
		case op_notequals:
			return new CBasicType(Kind.eInt, 0, this);

		case IASTBinaryExpression.op_plus:
			if (type1 instanceof IArrayType) {
				return Conversions.arrayTypeToPointerType((ICArrayType) type1);
			} else if (type2 instanceof IPointerType) {
				return restoreTypedefs(type2, originalType2);
			} else if (type2 instanceof IArrayType) {
				return Conversions.arrayTypeToPointerType((ICArrayType) type2);
			}
			break;

		case IASTBinaryExpression.op_minus:
			if (type2 instanceof IPointerType || type2 instanceof IArrayType) {
				if (type1 instanceof IPointerType || type1 instanceof IArrayType) {
					return CVisitor.getPtrDiffType(this);
				}
				return restoreTypedefs(type1, originalType1);
			}
			break;
		}
		return restoreTypedefs(type1, originalType1);
	}

	@Override
	public boolean isLValue() {
		switch (getOperator()) {
		case op_assign:
		case op_binaryAndAssign:
		case op_binaryOrAssign:
		case op_binaryXorAssign:
		case op_divideAssign:
		case op_minusAssign:
		case op_moduloAssign:
		case op_multiplyAssign:
		case op_plusAssign:
		case op_shiftLeftAssign:
		case op_shiftRightAssign:
			return true;
		}
		return false;
	}

	@Override
	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}
}
