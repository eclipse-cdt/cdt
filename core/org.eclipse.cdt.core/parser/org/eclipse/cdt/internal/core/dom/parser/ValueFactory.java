/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_alignof;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_nothrow_constructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_nothrow_copy;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_assign;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_constructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_copy;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_trivial_destructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_has_virtual_destructor;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_abstract;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_class;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_empty;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_enum;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_final;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_literal_type;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_pod;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_polymorphic;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_standard_layout;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_trivial;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_trivially_copyable;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_is_union;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_sizeof;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_typeid;
import static org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression.op_typeof;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNaryTypeIdExpression.Operator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;

public class ValueFactory {
	/**
	 * Creates the value for an expression.
	 */
	public static IValue create(IASTExpression expr) {
		try {
			CPPSemantics.pushLookupPoint(expr);
			IValue val = evaluate(expr);
			if (val != null) {
				return val;
			}

			if (expr instanceof ICPPASTInitializerClause) {
				ICPPEvaluation evaluation = ((ICPPASTInitializerClause) expr).getEvaluation();
				return evaluation.getValue();
			}
			return IntegralValue.UNKNOWN;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	public static IValue evaluateUnaryExpression(final int unaryOp, final IValue value) {
		IValue val = applyUnaryOperator(unaryOp, value);
		if (isInvalidValue(val))
			return IntegralValue.UNKNOWN;
		return val;
	}

	public static IValue evaluateBinaryExpression(final int op, final IValue v1, final IValue v2) {
		if (v1 instanceof FloatingPointValue && v2 instanceof FloatingPointValue) {
			FloatingPointValue fv1 = (FloatingPointValue) v1;
			FloatingPointValue fv2 = (FloatingPointValue) v2;
			return applyBinaryOperator(op, fv1.numberValue().doubleValue(), fv2.numberValue().doubleValue());
		} else if (v1 instanceof FloatingPointValue && v2 instanceof IntegralValue) {
			FloatingPointValue fv1 = (FloatingPointValue) v1;
			IntegralValue iv2 = (IntegralValue) v2;
			return applyBinaryOperator(op, fv1.numberValue().doubleValue(), iv2.numberValue().doubleValue());
		} else if (v1 instanceof IntegralValue && v2 instanceof FloatingPointValue) {
			IntegralValue iv1 = (IntegralValue) v1;
			FloatingPointValue fv2 = (FloatingPointValue) v2;
			return applyBinaryOperator(op, iv1.numberValue().doubleValue(), fv2.numberValue().doubleValue());
		} else if (v1 instanceof IntegralValue && v2 instanceof IntegralValue) {
			IntegralValue iv1 = (IntegralValue) v1;
			IntegralValue iv2 = (IntegralValue) v2;
			return applyBinaryOperator(op, iv1.numberValue().longValue(), iv2.numberValue().longValue());
		}
		return IntegralValue.UNKNOWN;
	}

	private static IValue applyBinaryOperator(final int op, final double v1, final double v2) {
		Double doubleValue = null;
		Long longValue = null;

		switch (op) {
		case IASTBinaryExpression.op_multiply:
			doubleValue = v1 * v2;
			break;
		case IASTBinaryExpression.op_divide:
			if (v2 != 0) {
				doubleValue = v1 / v2;
			}
			break;
		case IASTBinaryExpression.op_plus:
			doubleValue = v1 + v2;
			break;
		case IASTBinaryExpression.op_minus:
			doubleValue = v1 - v2;
			break;
		case IASTBinaryExpression.op_lessThan:
			longValue = v1 < v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_greaterThan:
			longValue = v1 > v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_lessEqual:
			longValue = v1 <= v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_greaterEqual:
			longValue = v1 >= v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_logicalAnd:
			longValue = v1 != 0 && v2 != 0 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_logicalOr:
			longValue = v1 != 0 || v2 != 0 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_equals:
			longValue = v1 == v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_notequals:
			longValue = v1 != v2 ? 1l : 0l;
			break;
		}

		if (doubleValue != null) {
			return FloatingPointValue.create(doubleValue);
		} else if (longValue != null) {
			return IntegralValue.create(longValue);
		} else {
			return IntegralValue.UNKNOWN;
		}
	}

	private static IntegralValue applyBinaryOperator(final int op, final long v1, final long v2) {
		Long value = null;
		switch (op) {
		case IASTBinaryExpression.op_multiply:
			value = v1 * v2;
			break;
		case IASTBinaryExpression.op_divide:
			if (v2 != 0) {
				value = v1 / v2;
			}
			break;
		case IASTBinaryExpression.op_modulo:
			if (v2 != 0) {
				value = v1 % v2;
			}
			break;
		case IASTBinaryExpression.op_plus:
			value = v1 + v2;
			break;
		case IASTBinaryExpression.op_minus:
			value = v1 - v2;
			break;
		case IASTBinaryExpression.op_shiftLeft:
			value = v1 << v2;
			break;
		case IASTBinaryExpression.op_shiftRight:
			value = v1 >> v2;
			break;
		case IASTBinaryExpression.op_lessThan:
			value = v1 < v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_greaterThan:
			value = v1 > v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_lessEqual:
			value = v1 <= v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_greaterEqual:
			value = v1 >= v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_binaryAnd:
			value = v1 & v2;
			break;
		case IASTBinaryExpression.op_binaryXor:
			value = v1 ^ v2;
			break;
		case IASTBinaryExpression.op_binaryOr:
			value = v1 | v2;
			break;
		case IASTBinaryExpression.op_logicalAnd:
			value = v1 != 0 && v2 != 0 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_logicalOr:
			value = v1 != 0 || v2 != 0 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_equals:
			value = v1 == v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_notequals:
			value = v1 != v2 ? 1l : 0l;
			break;
		case IASTBinaryExpression.op_max:
			value = Math.max(v1, v2);
			break;
		case IASTBinaryExpression.op_min:
			value = Math.min(v1, v2);
			break;
		}

		if (value != null) {
			return IntegralValue.create(value);
		} else {
			return IntegralValue.UNKNOWN;
		}
	}

	public static IValue evaluateUnaryTypeIdExpression(int operator, IType type) {
		IValue val = applyUnaryTypeIdOperator(operator, type);
		if (isInvalidValue(val))
			return IntegralValue.UNKNOWN;
		return val;
	}

	public static IValue evaluateBinaryTypeIdExpression(IASTBinaryTypeIdExpression.Operator operator, IType type1,
			IType type2) {
		IValue val = applyBinaryTypeIdOperator(operator, type1, type2);
		if (isInvalidValue(val))
			return IntegralValue.UNKNOWN;
		return val;
	}

	public static IValue evaluateNaryTypeIdExpression(Operator operator, IType[] operands, IBinding pointOfDefinition) {
		IValue val = applyNaryTypeIdOperator(operator, operands, pointOfDefinition);
		if (isInvalidValue(val))
			return IntegralValue.UNKNOWN;
		return val;
	}

	/**
	 * Computes the canonical representation of the value of the expression.
	 */
	private static IValue evaluate(IASTExpression exp) {
		// Some C++ expression types can involve evaluating functions.
		// For these, the value will be computed based on the evaluation.
		if (exp instanceof ICPPASTFunctionCallExpression || exp instanceof ICPPASTSimpleTypeConstructorExpression
				|| exp instanceof ICPPASTUnaryExpression || exp instanceof ICPPASTBinaryExpression) {
			return null;
		}

		if (exp == null)
			return IntegralValue.UNKNOWN;

		if (exp instanceof IASTArraySubscriptExpression) {
			return IntegralValue.UNKNOWN;
		}
		if (exp instanceof IASTBinaryExpression) {
			return evaluateBinaryExpression((IASTBinaryExpression) exp);
		}
		if (exp instanceof IASTCastExpression) { // must be ahead of unary
			return evaluate(((IASTCastExpression) exp).getOperand());
		}
		if (exp instanceof IASTUnaryExpression) {
			return evaluateUnaryExpression((IASTUnaryExpression) exp);
		}
		if (exp instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr = (IASTConditionalExpression) exp;
			IValue v = evaluate(cexpr.getLogicalConditionExpression());
			if (isInvalidValue(v))
				return v;
			if (isDeferredValue(v))
				return null; // The value will be computed using the evaluation.
			Number numericValue = v.numberValue();
			if (numericValue == null)
				return IntegralValue.UNKNOWN;
			if (v instanceof IntegralValue ? numericValue.longValue() == 0 : numericValue.doubleValue() == 0)
				return evaluate(cexpr.getNegativeResultExpression());
			final IASTExpression pe = cexpr.getPositiveResultExpression();
			if (pe == null) // A gnu-extension allows to omit the positive expression.
				return v;
			return evaluate(pe);
		}
		if (exp instanceof IASTIdExpression) {
			IBinding b = ((IASTIdExpression) exp).getName().resolvePreBinding();
			return evaluateBinding(b);
		}
		if (exp instanceof IASTLiteralExpression) {
			IASTLiteralExpression litEx = (IASTLiteralExpression) exp;
			switch (litEx.getKind()) {
			case IASTLiteralExpression.lk_false:
			case IASTLiteralExpression.lk_nullptr:
				return IntegralValue.create(0);
			case IASTLiteralExpression.lk_true:
				return IntegralValue.create(1);
			case IASTLiteralExpression.lk_integer_constant:
				try {
					return IntegralValue.create(ExpressionEvaluator.getNumber(litEx.getValue()));
				} catch (EvalException e) {
					return IntegralValue.UNKNOWN;
				}
			case IASTLiteralExpression.lk_char_constant:
				try {
					final char[] image = litEx.getValue();
					if (image.length > 1 && image[0] == 'L')
						return IntegralValue.create(ExpressionEvaluator.getChar(image, 2));
					return IntegralValue.create(ExpressionEvaluator.getChar(image, 1));
				} catch (EvalException e) {
					return IntegralValue.UNKNOWN;
				}
			case IASTLiteralExpression.lk_float_constant:
				return FloatingPointValue.create(litEx.getValue());
			case IASTLiteralExpression.lk_string_literal:
				return CStringValue.create(litEx.getValue());
			}
		}

		if (exp instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression typeIdExp = (IASTTypeIdExpression) exp;
			ASTTranslationUnit ast = (ASTTranslationUnit) exp.getTranslationUnit();
			final IType type = ast.createType(typeIdExp.getTypeId());
			if (type instanceof ICPPUnknownType)
				return null;
			return applyUnaryTypeIdOperator(typeIdExp.getOperator(), type);
		}
		if (exp instanceof IASTBinaryTypeIdExpression) {
			IASTBinaryTypeIdExpression typeIdExp = (IASTBinaryTypeIdExpression) exp;
			ASTTranslationUnit ast = (ASTTranslationUnit) exp.getTranslationUnit();
			IType t1 = ast.createType(typeIdExp.getOperand1());
			IType t2 = ast.createType(typeIdExp.getOperand2());
			if (CPPTemplates.isDependentType(t1) || CPPTemplates.isDependentType(t2))
				return null;
			return applyBinaryTypeIdOperator(typeIdExp.getOperator(), t1, t2);
		}
		return IntegralValue.UNKNOWN;
	}

	/**
	 * Extract a value off a binding.
	 */
	private static IValue evaluateBinding(IBinding b) {
		if (b instanceof IType) {
			return IntegralValue.UNKNOWN;
		}
		if (b instanceof ICPPTemplateNonTypeParameter) {
			return null;
		}

		if (b instanceof ICPPUnknownBinding) {
			return null;
		}

		IValue value = null;
		if (b instanceof IVariable) {
			value = ((IVariable) b).getInitialValue();
		} else if (b instanceof IEnumerator) {
			value = ((IEnumerator) b).getValue();
		}
		if (isInvalidValue(value)) {
			return IntegralValue.UNKNOWN;
		}
		return value;
	}

	private static IValue applyUnaryTypeIdOperator(int operator, IType type) {
		type = SemanticUtil.getNestedType(type, TDEF | CVTYPE);

		switch (operator) {
		case op_sizeof:
			return getSize(type);
		case op_alignof:
			return getAlignment(type);
		case op_typeid:
			break;
		case op_has_nothrow_copy:
			break; // TODO(sprigogin): Implement
		case op_has_nothrow_constructor:
			break; // TODO(sprigogin): Implement
		case op_has_trivial_assign:
			break; // TODO(sprigogin): Implement
		case op_has_trivial_constructor:
			break; // TODO(sprigogin): Implement
		case op_has_trivial_copy:
			return IntegralValue.create(
					!(type instanceof ICPPClassType) || TypeTraits.hasTrivialCopyCtor((ICPPClassType) type) ? 1 : 0);
		case op_has_trivial_destructor:
			break; // TODO(sprigogin): Implement
		case op_has_virtual_destructor:
			break; // TODO(sprigogin): Implement
		case op_is_abstract:
			return IntegralValue
					.create(type instanceof ICPPClassType && TypeTraits.isAbstract((ICPPClassType) type) ? 1 : 0);
		case op_is_class:
			return IntegralValue.create(
					type instanceof ICompositeType && ((ICompositeType) type).getKey() != ICompositeType.k_union ? 1
							: 0);
		case op_is_empty:
			return IntegralValue.create(TypeTraits.isEmpty(type) ? 1 : 0);
		case op_is_enum:
			return IntegralValue.create(type instanceof IEnumeration ? 1 : 0);
		case op_is_final:
			return IntegralValue.create(type instanceof ICPPClassType && ((ICPPClassType) type).isFinal() ? 1 : 0);
		case op_is_literal_type:
			break; // TODO(sprigogin): Implement
		case op_is_pod:
			return IntegralValue.create(TypeTraits.isPOD(type) ? 1 : 0);
		case op_is_polymorphic:
			return IntegralValue
					.create(type instanceof ICPPClassType && TypeTraits.isPolymorphic((ICPPClassType) type) ? 1 : 0);
		case op_is_standard_layout:
			return IntegralValue.create(TypeTraits.isStandardLayout(type) ? 1 : 0);
		case op_is_trivial:
			return IntegralValue
					.create(type instanceof ICPPClassType && TypeTraits.isTrivial((ICPPClassType) type) ? 1 : 0);
		case op_is_trivially_copyable:
			return IntegralValue.create(TypeTraits.isTriviallyCopyable(type) ? 1 : 0);
		case op_is_union:
			return IntegralValue.create(
					type instanceof ICompositeType && ((ICompositeType) type).getKey() == ICompositeType.k_union ? 1
							: 0);
		case op_typeof:
			break;
		}
		return IntegralValue.UNKNOWN;
	}

	private static IValue getAlignment(IType type) {
		SizeAndAlignment sizeAndAlignment = SizeofCalculator.getSizeAndAlignment(type);
		if (sizeAndAlignment == null)
			return IntegralValue.UNKNOWN;
		return IntegralValue.create(sizeAndAlignment.alignment);
	}

	private static IValue getSize(IType type) {
		SizeAndAlignment sizeAndAlignment = SizeofCalculator.getSizeAndAlignment(type);
		if (sizeAndAlignment == null)
			return IntegralValue.UNKNOWN;
		return IntegralValue.create(sizeAndAlignment.size);
	}

	private static IValue evaluateUnaryExpression(IASTUnaryExpression exp) {
		final int unaryOp = exp.getOperator();

		if (unaryOp == IASTUnaryExpression.op_sizeof) {
			final IASTExpression operand = exp.getOperand();
			if (operand != null) {
				IType type = operand.getExpressionType();
				if (type instanceof ICPPUnknownType)
					return null;
				ASTTranslationUnit ast = (ASTTranslationUnit) exp.getTranslationUnit();
				SizeofCalculator calculator = ast.getSizeofCalculator();
				SizeAndAlignment info = calculator.sizeAndAlignment(type);
				if (info != null)
					return IntegralValue.create(info.size);
			}
			return IntegralValue.UNKNOWN;
		}

		if (unaryOp == IASTUnaryExpression.op_amper || unaryOp == IASTUnaryExpression.op_star
				|| unaryOp == IASTUnaryExpression.op_sizeofParameterPack) {
			return IntegralValue.UNKNOWN;
		}

		final IValue value = evaluate(exp.getOperand());
		if (isInvalidValue(value))
			return value;
		if (isDeferredValue(value))
			return null; // the value will be computed using the evaluation
		return applyUnaryOperator(unaryOp, value);
	}

	private static IValue applyUnaryOperator(final int unaryOp, final IValue value) {
		if (isInvalidValue(value) || value.numberValue() == null) {
			return IntegralValue.UNKNOWN;
		}

		if (!(value instanceof IntegralValue) && !(value instanceof FloatingPointValue)) {
			return IntegralValue.UNKNOWN;
		}

		switch (unaryOp) {
		case IASTUnaryExpression.op_bracketedPrimary:
		case IASTUnaryExpression.op_plus:
			return value;
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_postFixIncr:
			if (value instanceof IntegralValue) {
				return IntegralValue.create(value.numberValue().longValue() + 1);
			} else {
				FloatingPointValue fpv = (FloatingPointValue) value;
				return FloatingPointValue.create(fpv.numberValue().doubleValue() + 1);
			}
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_postFixDecr:
			if (value instanceof IntegralValue) {
				return IntegralValue.create(value.numberValue().longValue() - 1);
			} else {
				FloatingPointValue fpv = (FloatingPointValue) value;
				return FloatingPointValue.create(fpv.numberValue().doubleValue() - 1);
			}
		case IASTUnaryExpression.op_minus:
			if (value instanceof IntegralValue) {
				return IntegralValue.create(-value.numberValue().longValue());
			} else {
				FloatingPointValue fpv = (FloatingPointValue) value;
				return FloatingPointValue.create(-fpv.numberValue().doubleValue());
			}
		case IASTUnaryExpression.op_tilde:
			if (value instanceof IntegralValue) {
				return IntegralValue.create(~value.numberValue().longValue());
			} else {
				return IntegralValue.UNKNOWN;
			}
		case IASTUnaryExpression.op_not:
			if (value instanceof IntegralValue) {
				Long num = value.numberValue().longValue();
				return IntegralValue.create(num == 0 ? 1 : 0);
			} else {
				FloatingPointValue fpv = (FloatingPointValue) value;
				Double num = fpv.numberValue().doubleValue();
				return IntegralValue.create(num == 0 ? 1 : 0);
			}
		}
		return IntegralValue.UNKNOWN;
	}

	private static IValue evaluateBinaryExpression(IASTBinaryExpression exp) {
		final int op = exp.getOperator();

		// Optimization: if the operator is == or != and the AST nodes
		// themselves are equal, we know the answer without having to
		// do any evaluation.
		if (op == IASTBinaryExpression.op_equals && exp.getOperand1().equals(exp.getOperand2())) {
			return IntegralValue.create(true);
		}
		if (op == IASTBinaryExpression.op_notequals && exp.getOperand1().equals(exp.getOperand2())) {
			return IntegralValue.create(false);
		}

		final IValue o1 = evaluate(exp.getOperand1());
		if (isInvalidValue(o1))
			return o1;
		final IValue o2 = evaluate(exp.getOperand2());
		if (isInvalidValue(o2))
			return o2;
		if (isDeferredValue(o1) || isDeferredValue(o2))
			return null; // the value will be computed using the evaluation
		return evaluateBinaryExpression(op, o1, o2);
	}

	private static IValue applyBinaryTypeIdOperator(IASTBinaryTypeIdExpression.Operator operator, IType type1,
			IType type2) {
		switch (operator) {
		case __is_base_of:
			type1 = SemanticUtil.getNestedType(type1, TDEF);
			type2 = SemanticUtil.getNestedType(type2, TDEF);
			if (type1 instanceof ICPPClassType && type2 instanceof ICPPClassType && (type1.isSameType(type2)
					|| ClassTypeHelper.isSubclass((ICPPClassType) type2, (ICPPClassType) type1))) {
				return IntegralValue.create(1);
			}
			return IntegralValue.create(0);
		case __is_trivially_assignable:
			return IntegralValue.UNKNOWN; // TODO: Implement.
		}
		return IntegralValue.UNKNOWN;
	}

	private static IValue applyNaryTypeIdOperator(ICPPASTNaryTypeIdExpression.Operator operator, IType[] operands,
			IBinding pointOfDefinition) {
		switch (operator) {
		case __is_constructible:
		case __is_trivially_constructible:
			if (operands.length == 0) {
				return IntegralValue.UNKNOWN;
			}
			boolean checkTrivial = (operator == Operator.__is_trivially_constructible);
			IType typeToConstruct = operands[0];
			IType[] argumentTypes = Arrays.copyOfRange(operands, 1, operands.length);
			return IntegralValue.create(
					TypeTraits.isConstructible(typeToConstruct, argumentTypes, pointOfDefinition, checkTrivial) ? 1
							: 0);
		}
		return IntegralValue.UNKNOWN;
	}

	private static boolean isInvalidValue(IValue value) {
		return value == null || value == IntegralValue.UNKNOWN || value == IntegralValue.ERROR;
	}

	private static boolean isDeferredValue(IValue value) {
		return value instanceof DependentValue
				|| (value instanceof IntegralValue && ((IntegralValue) value).numberValue() == null);
	}

	/**
	 * Returns the numerical value of the given expression if the expression can be evaluated
	 * at compile time.
	 *
	 * @param expr the expression to evaluate
	 * @return the numerical value of the expression, or {@code null} if the expression cannot be evaluated
	 *     to a constant
	 */
	public static Number getConstantNumericalValue(IASTExpression expr) {
		try {
			CPPSemantics.pushLookupPoint(expr);
			IValue val = evaluate(expr);
			if (val != null) {
				return val.numberValue();
			}

			if (expr instanceof ICPPASTInitializerClause) {
				ICPPEvaluation eval = ((ICPPASTInitializerClause) expr).getEvaluation();
				if (eval.isConstantExpression() && !eval.isValueDependent())
					return eval.getValue().numberValue();
			}
			return null;
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}
}
