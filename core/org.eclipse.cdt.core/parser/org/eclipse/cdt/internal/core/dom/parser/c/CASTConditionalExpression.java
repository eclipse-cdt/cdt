/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes;

/**
 * Conditional expression in C
 */
public class CASTConditionalExpression extends ASTNode implements IASTConditionalExpression, IASTAmbiguityParent {

	private IASTExpression condition;
	private IASTExpression negative;
	private IASTExpression positive;

	public CASTConditionalExpression() {
	}

	public CASTConditionalExpression(IASTExpression condition, IASTExpression positive, IASTExpression negative) {
		setLogicalConditionExpression(condition);
		setPositiveResultExpression(positive);
		setNegativeResultExpression(negative);
	}

	@Override
	public CASTConditionalExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTConditionalExpression copy(CopyStyle style) {
		CASTConditionalExpression copy = new CASTConditionalExpression();
		copy.setLogicalConditionExpression(condition == null ? null : condition.copy(style));
		copy.setPositiveResultExpression(positive == null ? null : positive.copy(style));
		copy.setNegativeResultExpression(negative == null ? null : negative.copy(style));
		return copy(copy, style);
	}

	@Override
	public IASTExpression getLogicalConditionExpression() {
		return condition;
	}

	@Override
	public void setLogicalConditionExpression(IASTExpression expression) {
		assertNotFrozen();
		condition = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(LOGICAL_CONDITION);
		}
	}

	@Override
	public IASTExpression getPositiveResultExpression() {
		return positive;
	}

	@Override
	public void setPositiveResultExpression(IASTExpression expression) {
		assertNotFrozen();
		this.positive = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(POSITIVE_RESULT);
		}
	}

	@Override
	public IASTExpression getNegativeResultExpression() {
		return negative;
	}

	@Override
	public void setNegativeResultExpression(IASTExpression expression) {
		assertNotFrozen();
		this.negative = expression;
		if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEGATIVE_RESULT);
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

		if (condition != null)
			if (!condition.accept(action))
				return false;
		if (positive != null)
			if (!positive.accept(action))
				return false;
		if (negative != null)
			if (!negative.accept(action))
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
		if (child == condition) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			condition = (IASTExpression) other;
		}
		if (child == positive) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			positive = (IASTExpression) other;
		}
		if (child == negative) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			negative = (IASTExpression) other;
		}
	}

	@Override
	public IType getExpressionType() {
		IASTExpression positiveExpression = getPositiveResultExpression();
		if (positiveExpression == null) {
			positiveExpression = getLogicalConditionExpression();
		}
		IASTExpression negativeExpression = getNegativeResultExpression();
		IType originalPositiveType = positiveExpression.getExpressionType();
		IType originalNegativeType = getNegativeResultExpression().getExpressionType();
		IType positiveType = CVisitor.unwrapTypedefs(originalPositiveType);
		IType negativeType = CVisitor.unwrapTypedefs(originalNegativeType);
		IType resultType = computeResultType(positiveExpression, negativeExpression, positiveType, negativeType);
		if (resultType == null) {
			return ProblemType.UNKNOWN_FOR_EXPRESSION;
		}
		return ExpressionTypes.restoreTypedefs(resultType, originalPositiveType, originalPositiveType);
	}

	private IType computeResultType(IASTExpression positiveExpression, IASTExpression negativeExpression,
			IType positiveType, IType negativeType) {
		// Unwrap any top-level cv-qualifiers.
		positiveType = CVisitor.unwrapCV(positiveType);
		negativeType = CVisitor.unwrapCV(negativeType);

		// [6.5.15] p5: If both the second and third operands have arithmetic type, the result type
		// that would be determined by the usual arithmetic conversions, were they applied to those
		// two operands, is the type of the result. If both operands have void type, the result has
		// void type.
		if (positiveType instanceof IBasicType && negativeType instanceof IBasicType) {
			if (((IBasicType) positiveType).getKind() == IBasicType.Kind.eVoid
					&& ((IBasicType) negativeType).getKind() == IBasicType.Kind.eVoid) {
				return CBasicType.VOID;
			}

			// It doesn't really matter which operator we use here, so we'll use op_plus.
			return CArithmeticConversion.convertCOperandTypes(IASTBinaryExpression.op_plus, positiveType, negativeType);
		}

		// If both the operands have structure or union type, the result has that type.
		if (positiveType instanceof ICompositeType && negativeType instanceof ICompositeType) {
			// Both operands must have the same structure or union type as per p3.
			if (positiveType.isSameType(negativeType)) {
				return positiveType;
			}
		}

		// Perform array-to-pointer decay on the operand types.
		if (positiveType instanceof ICArrayType) {
			positiveType = Conversions.arrayTypeToPointerType(((ICArrayType) positiveType));
		}
		if (negativeType instanceof ICArrayType) {
			negativeType = Conversions.arrayTypeToPointerType(((ICArrayType) negativeType));
		}

		// [6.5.15] p6: If both the second and third operands are pointers or one is a null pointer
		// constant and the other is a pointer, the result type is a pointer to a type qualified with
		// all the type qualifiers of the types referenced by both operands. Furthermore, if both
		// operands are pointers to compatible types or to differently qualified versions of compatible
		// types, the result type is a pointer to an appropriately qualified version of the composite
		// type; if one operand is a null pointer constant, the result has the type of the other operand;
		// otherwise, one operand is a pointer to void or a qualified version of void, in which case the
		// result type is a pointer to an appropriately qualified version of void.
		if (CVisitor.isNullPointerConstant(positiveExpression) && negativeType instanceof IPointerType) {
			return negativeType;
		} else if (CVisitor.isNullPointerConstant(negativeExpression) && positiveType instanceof IPointerType) {
			return positiveType;
		} else if (positiveType instanceof IPointerType && negativeType instanceof IPointerType) {
			IType positivePointeeCV = ((IPointerType) positiveType).getType();
			IType negativePointeeCV = ((IPointerType) negativeType).getType();
			IType positivePointee = CVisitor.unwrapCV(positivePointeeCV);
			IType negativePointee = CVisitor.unwrapCV(negativePointeeCV);
			IType resultPointee;
			if (positivePointee.isSameType(negativePointee)) {
				resultPointee = negativePointee;
			} else if (positivePointee.isSameType(CBasicType.VOID) || negativePointee.isSameType(CBasicType.VOID)) {
				resultPointee = CBasicType.VOID;
			} else {
				return ProblemType.UNKNOWN_FOR_EXPRESSION;
			}
			return new CPointerType(ExpressionTypes.restoreCV(resultPointee, positivePointeeCV, negativePointeeCV), 0);
		}

		return null;
	}

	@Override
	public boolean isLValue() {
		return false;
	}

	@Override
	public final ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
