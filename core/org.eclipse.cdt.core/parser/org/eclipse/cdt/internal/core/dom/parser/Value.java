/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;
import org.eclipse.cdt.internal.core.pdom.dom.TypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents values of variables, enumerators or expressions. The primary purpose of
 * the representation is to support instantiation of templates with non-type template parameters.
 */
public class Value implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;
	// Value.UNKNOWN indicates general inability to determine a value. It doesn't have to be an error,
	// it could be that evaluation ran into a performance limit, or that we can't model this kind of
	// value (such as a pointer to a function).
	public static final Value UNKNOWN= new Value("<unknown>".toCharArray(), null); //$NON-NLS-1$
	// Value.ERROR indicates that an error, such as a substitution failure, occurred during evaluation.
	public static final Value ERROR= new Value("<error>".toCharArray(), null); //$NON-NLS-1$
	public static final Value NOT_INITIALIZED= new Value("<__>".toCharArray(), null); //$NON-NLS-1$
	private static final IType INT_TYPE= new CPPBasicType(ICPPBasicType.Kind.eInt, 0);

	private static final Number VALUE_CANNOT_BE_DETERMINED = new Number() {
		@Override
		public int intValue() {	throw new UnsupportedOperationException(); }

		@Override
		public long longValue() { throw new UnsupportedOperationException(); }

		@Override
		public float floatValue() { throw new UnsupportedOperationException(); }

		@Override
		public double doubleValue() { throw new UnsupportedOperationException(); }
	};

	private static final char UNIQUE_CHAR = '_';

	private final static IValue[] TYPICAL= {
		new Value(new char[] {'0'}, null),
		new Value(new char[] {'1'}, null),
		new Value(new char[] {'2'}, null),
		new Value(new char[] {'3'}, null),
		new Value(new char[] {'4'}, null),
		new Value(new char[] {'5'}, null),
		new Value(new char[] {'6'}, null)};


	private static int sUnique= 0;

	// The following invariant always holds: (fFixedValue == null) != (fEvaluation == null)
	private final char[] fFixedValue;
	private final ICPPEvaluation fEvaluation;
	private char[] fSignature;

	private Value(char[] fixedValue, ICPPEvaluation evaluation) {
		assert (fixedValue == null) != (evaluation == null);
		fFixedValue = fixedValue;
		fEvaluation = evaluation;
	}

	@Override
	public Long numericalValue() {
		return fFixedValue == null ? null : parseLong(fFixedValue);
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return fEvaluation;
	}

	@Override
	public char[] getSignature() {
		if (fSignature == null) {
			fSignature = fFixedValue != null ? fFixedValue : fEvaluation.getSignature();
		}
		return fSignature;
	}

	@Deprecated
	@Override
	public char[] getInternalExpression() {
		return CharArrayUtils.EMPTY_CHAR_ARRAY;
	}

	@Deprecated
	@Override
	public IBinding[] getUnknownBindings() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		if (UNKNOWN == this) {
			buf.putShort((short) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG1));
		} else {
			Long num= numericalValue();
			if (num != null) {
				long lv= num;
				if (lv >= 0) {
					buf.putShort((short) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG2));
					buf.putLong(lv);
				} else {
					buf.putShort((short) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG3));
					buf.putLong(-lv);
				}
			} else if (fFixedValue != null) {
				buf.putShort((short) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG4));
				buf.putCharArray(fFixedValue);
			} else {
				buf.putShort(ITypeMarshalBuffer.VALUE);
				fEvaluation.marshal(buf, true);
			}
		}
	}

	public static IValue unmarshal(ITypeMarshalBuffer buf) throws CoreException {
		short firstBytes= buf.getShort();
		if (firstBytes == TypeMarshalBuffer.NULL_TYPE)
			return Value.UNKNOWN;
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0)
			return Value.UNKNOWN;
		if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0)
			return Value.create(buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG3) != 0)
			return Value.create(-buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG4) != 0)
			return new Value(buf.getCharArray(), null);

		ISerializableEvaluation eval= buf.unmarshalEvaluation();
		if (eval instanceof ICPPEvaluation)
			return new Value(null, (ICPPEvaluation) eval);
		return Value.UNKNOWN;
	}

	@Override
	public int hashCode() {
		return CharArrayUtils.hash(getSignature());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Value)) {
			return false;
		}
		final Value rhs = (Value) obj;
		if (fFixedValue != null)
			return CharArrayUtils.equals(fFixedValue, rhs.fFixedValue);
		return CharArrayUtils.equals(getSignature(), rhs.getSignature());
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return new String(getSignature());
	}

	/**
	 * Creates a value representing the given number.
	 */
	public static IValue create(long value) {
		if (value >= 0 && value < TYPICAL.length)
			return TYPICAL[(int) value];
		return new Value(toCharArray(value), null);
	}

	/**
	 * Creates a value object representing the given boolean value.
	 */
	public static IValue create(boolean value) {
		return create(value ? 1 : 0);
	}

	/**
	 * Creates a value representing the given template parameter
	 * in the given template.
	 */
	public static IValue create(ICPPTemplateDefinition template, ICPPTemplateNonTypeParameter tntp) {
		EvalBinding eval = new EvalBinding(tntp, null, template);
		return new Value(null, eval);
	}

	/**
	 * Create a value wrapping the given evaluation.
	 */
	public static IValue create(ICPPEvaluation eval) {
		return new Value(null, eval);
	}

	public static IValue evaluateBinaryExpression(final int op, final long v1, final long v2) {
		Number val = applyBinaryOperator(op, v1, v2);
		if (val != null && val != VALUE_CANNOT_BE_DETERMINED)
			return create(val.longValue());
		return UNKNOWN;
	}

	public static IValue evaluateUnaryExpression(final int unaryOp, final long value) {
		Number val = applyUnaryOperator(unaryOp, value);
		if (val != null && val != VALUE_CANNOT_BE_DETERMINED)
			return create(val.longValue());
		return UNKNOWN;
	}

	public static IValue evaluateUnaryTypeIdExpression(int operator, IType type, IASTNode point) {
		Number val = applyUnaryTypeIdOperator(operator, type, point);
		if (val != null && val != VALUE_CANNOT_BE_DETERMINED)
			return create(val.longValue());
		return UNKNOWN;
	}

	public static IValue evaluateBinaryTypeIdExpression(IASTBinaryTypeIdExpression.Operator operator,
			IType type1, IType type2, IASTNode point) {
		Number val = applyBinaryTypeIdOperator(operator, type1, type2, point);
		if (val != null && val != VALUE_CANNOT_BE_DETERMINED)
			return create(val.longValue());
		return UNKNOWN;
	}

	public static IValue incrementedValue(IValue value, int increment) {
		if (value == UNKNOWN)
			return UNKNOWN;
		Long val = value.numericalValue();
		if (val != null) {
			return create(val.longValue() + increment);
		}
		ICPPEvaluation arg1 = value.getEvaluation();
		EvalFixed arg2 = new EvalFixed(INT_TYPE, ValueCategory.PRVALUE, create(increment));
		return create(new EvalBinary(IASTBinaryExpression.op_plus, arg1, arg2, arg1.getTemplateDefinition()));
	}

	private static Number applyUnaryTypeIdOperator(int operator, IType type, IASTNode point) {
		switch (operator) {
			case op_sizeof:
				return getSize(type, point);
			case op_alignof:
				return getAlignment(type, point);
			case op_typeid:
				break;
			case op_has_nothrow_copy:
				break;  // TODO(sprigogin): Implement
			case op_has_nothrow_constructor:
				break;  // TODO(sprigogin): Implement
			case op_has_trivial_assign:
				break;  // TODO(sprigogin): Implement
			case op_has_trivial_constructor:
				break;  // TODO(sprigogin): Implement
			case op_has_trivial_copy:
				return !(type instanceof ICPPClassType) ||
						TypeTraits.hasTrivialCopyCtor((ICPPClassType) type, point) ? 1 : 0;
			case op_has_trivial_destructor:
				break;  // TODO(sprigogin): Implement
			case op_has_virtual_destructor:
				break;  // TODO(sprigogin): Implement
			case op_is_abstract:
				return type instanceof ICPPClassType &&
						TypeTraits.isAbstract((ICPPClassType) type, point) ? 1 : 0;
			case op_is_class:
				return type instanceof ICompositeType &&
						((ICompositeType) type).getKey() != ICompositeType.k_union ? 1 : 0;
			case op_is_empty:
				return TypeTraits.isEmpty(type, point) ? 1 : 0;
			case op_is_enum:
				return type instanceof IEnumeration ? 1 : 0;
			case op_is_final:
				return type instanceof ICPPClassType && ((ICPPClassType) type).isFinal() ? 1 : 0;
			case op_is_literal_type:
				break;  // TODO(sprigogin): Implement
			case op_is_pod:
				return TypeTraits.isPOD(type, point) ? 1 : 0;
			case op_is_polymorphic:
				return type instanceof ICPPClassType &&
						TypeTraits.isPolymorphic((ICPPClassType) type, point) ? 1 : 0;
			case op_is_standard_layout:
				return TypeTraits.isStandardLayout(type, point) ? 1 : 0;
			case op_is_trivial:
				return type instanceof ICPPClassType &&
						TypeTraits.isTrivial((ICPPClassType) type, point) ? 1 : 0;
			case op_is_trivially_copyable:
				return TypeTraits.isTriviallyCopyable(type, point) ? 1 : 0;
			case op_is_union:
				return type instanceof ICompositeType &&
						((ICompositeType) type).getKey() == ICompositeType.k_union ? 1 : 0;
			case op_typeof:
				break;
		}
		return VALUE_CANNOT_BE_DETERMINED;
	}

	public static Number applyBinaryTypeIdOperator(IASTBinaryTypeIdExpression.Operator operator,
			IType type1, IType type2, IASTNode point) {
		switch (operator) {
		case __is_base_of:
			type1 = SemanticUtil.getNestedType(type1, TDEF);
			type2 = SemanticUtil.getNestedType(type2, TDEF);
			if (type1 instanceof ICPPClassType && type2 instanceof ICPPClassType &&
					(type1.isSameType(type2) ||
							ClassTypeHelper.isSubclass((ICPPClassType) type2, (ICPPClassType) type1, point))) {
				return 1;
			}
			return 0;
		case __is_trivially_assignable:
			return VALUE_CANNOT_BE_DETERMINED;  // TODO: Implement.
		}
		return VALUE_CANNOT_BE_DETERMINED;
	}

	private static Number getAlignment(IType type, IASTNode point) {
		SizeAndAlignment sizeAndAlignment = SizeofCalculator.getSizeAndAlignment(type, point);
		if (sizeAndAlignment == null)
			 return VALUE_CANNOT_BE_DETERMINED;
		return sizeAndAlignment.alignment;
	}

	private static Number getSize(IType type, IASTNode point) {
		SizeAndAlignment sizeAndAlignment = SizeofCalculator.getSizeAndAlignment(type, point);
		if (sizeAndAlignment == null)
			 return VALUE_CANNOT_BE_DETERMINED;
		return sizeAndAlignment.size;
	}

	/**
	 * Tests whether the value is a template parameter (or a parameter pack).
	 *
	 * @return the parameter id of the parameter, or <code>-1</code> if it is not a template
	 *         parameter.
	 */
	public static int isTemplateParameter(IValue tval) {
		ICPPEvaluation eval = tval.getEvaluation();
		if (eval instanceof EvalBinding) {
			return ((EvalBinding) eval).getTemplateParameterID();
		}
		return -1;
	}

	/**
	 * Tests whether the value references some template parameter.
	 */
	public static boolean referencesTemplateParameter(IValue tval) {
		ICPPEvaluation eval = tval.getEvaluation();
		if (eval == null)
			return false;
		return eval.referencesTemplateParameter();
	}

	/**
	 * Tests whether the value depends on a template parameter.
	 */
	public static boolean isDependentValue(IValue nonTypeValue) {
		if (nonTypeValue == null)
			return false;
		ICPPEvaluation eval = nonTypeValue.getEvaluation();
		return eval != null && eval.isValueDependent();
	}

	/**
	 * Creates the value for an expression.
	 */
	public static IValue create(IASTExpression expr) {
		Number val= evaluate(expr);
		if (val == VALUE_CANNOT_BE_DETERMINED)
			return UNKNOWN;
		if (val != null)
			return create(val.longValue());

		if (expr instanceof ICPPASTInitializerClause) {
			ICPPEvaluation evaluation = ((ICPPASTInitializerClause) expr).getEvaluation();
			return evaluation.getValue(expr);
		}
		return UNKNOWN;
	}

	/**
	 * Creates a value off its canonical representation.
	 */
	public static IValue fromInternalRepresentation(ICPPEvaluation evaluation) {
		return new Value(null, evaluation);
	}

	/**
	 * Creates a unique value needed during template instantiation.
	 */
	public static IValue unique() {
		StringBuilder buf= new StringBuilder(10);
		buf.append(UNIQUE_CHAR);
		buf.append(++sUnique);
		return new Value(CharArrayUtils.extractChars(buf), null);
	}

	/**
	 * Computes the canonical representation of the value of the expression.
	 * Returns a {@code Number} for numerical values or {@code null}, otherwise.
	 * @throws UnknownValueException
	 */
	private static Number evaluate(IASTExpression exp) {
		if (exp == null)
			return VALUE_CANNOT_BE_DETERMINED;

		if (exp instanceof IASTArraySubscriptExpression) {
			return VALUE_CANNOT_BE_DETERMINED;
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
			IASTConditionalExpression cexpr= (IASTConditionalExpression) exp;
			Number v= evaluate(cexpr.getLogicalConditionExpression());
			if (v == null || v == VALUE_CANNOT_BE_DETERMINED)
				return v;
			if (v.longValue() == 0) {
				return evaluate(cexpr.getNegativeResultExpression());
			}
			final IASTExpression pe = cexpr.getPositiveResultExpression();
			if (pe == null) // gnu-extension allows to omit the positive expression.
				return v;
			return evaluate(pe);
		}
		if (exp instanceof IASTIdExpression) {
			IBinding b= ((IASTIdExpression) exp).getName().resolvePreBinding();
			return evaluateBinding(b);
		}
		if (exp instanceof IASTLiteralExpression) {
			IASTLiteralExpression litEx= (IASTLiteralExpression) exp;
			switch (litEx.getKind()) {
			case IASTLiteralExpression.lk_false:
			case IASTLiteralExpression.lk_nullptr:
				return Long.valueOf(0);
			case IASTLiteralExpression.lk_true:
				return Long.valueOf(1);
			case IASTLiteralExpression.lk_integer_constant:
				try {
					return ExpressionEvaluator.getNumber(litEx.getValue());
				} catch (EvalException e) {
					return VALUE_CANNOT_BE_DETERMINED;
				}
			case IASTLiteralExpression.lk_char_constant:
				try {
					final char[] image= litEx.getValue();
					if (image.length > 1 && image[0] == 'L')
						return ExpressionEvaluator.getChar(image, 2);
					return ExpressionEvaluator.getChar(image, 1);
				} catch (EvalException e) {
					return VALUE_CANNOT_BE_DETERMINED;
				}
			}
		}
		if (exp instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression typeIdExp = (IASTTypeIdExpression) exp;
			ASTTranslationUnit ast = (ASTTranslationUnit) exp.getTranslationUnit();
			final IType type = ast.createType(typeIdExp.getTypeId());
			if (type instanceof ICPPUnknownType)
				return null;
			return applyUnaryTypeIdOperator(typeIdExp.getOperator(), type, exp);
		}
		if (exp instanceof IASTBinaryTypeIdExpression) {
			IASTBinaryTypeIdExpression typeIdExp = (IASTBinaryTypeIdExpression) exp;
			ASTTranslationUnit ast = (ASTTranslationUnit) exp.getTranslationUnit();
			IType t1= ast.createType(typeIdExp.getOperand1());
			IType t2= ast.createType(typeIdExp.getOperand2());
			if (CPPTemplates.isDependentType(t1) || CPPTemplates.isDependentType(t2))
				return null;
			return applyBinaryTypeIdOperator(typeIdExp.getOperator(), t1, t2, exp);
		}
		if (exp instanceof IASTFunctionCallExpression || exp instanceof ICPPASTSimpleTypeConstructorExpression) {
			return null;  // The value will be obtained from the evaluation.
		}
		return VALUE_CANNOT_BE_DETERMINED;
	}

	/**
	 * Extract a value off a binding.
	 */
	private static Number evaluateBinding(IBinding b) {
		if (b instanceof IType) {
			return VALUE_CANNOT_BE_DETERMINED;
		}
		if (b instanceof ICPPTemplateNonTypeParameter) {
			return null;
		}

		if (b instanceof ICPPUnknownBinding) {
			return null;
		}

		IValue value= null;
		if (b instanceof IVariable) {
			value= ((IVariable) b).getInitialValue();
		} else if (b instanceof IEnumerator) {
			value= ((IEnumerator) b).getValue();
		}
		if (value != null && value != Value.UNKNOWN) {
			return value.numericalValue();
		}

		return VALUE_CANNOT_BE_DETERMINED;
	}

	private static Number evaluateUnaryExpression(IASTUnaryExpression exp) {
		final int unaryOp= exp.getOperator();

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
					return info.size;
			}
			return VALUE_CANNOT_BE_DETERMINED;
		}

		if (unaryOp == IASTUnaryExpression.op_amper || unaryOp == IASTUnaryExpression.op_star ||
				unaryOp == IASTUnaryExpression.op_sizeofParameterPack) {
			return VALUE_CANNOT_BE_DETERMINED;
		}

		final Number value= evaluate(exp.getOperand());
		if (value == null || value == VALUE_CANNOT_BE_DETERMINED)
			return value;
		return applyUnaryOperator(unaryOp, value.longValue());
	}

	private static Number applyUnaryOperator(final int unaryOp, final long value) {
		switch (unaryOp) {
		case IASTUnaryExpression.op_bracketedPrimary:
		case IASTUnaryExpression.op_plus:
			return value;
		}

		switch (unaryOp) {
		case IASTUnaryExpression.op_prefixIncr:
		case IASTUnaryExpression.op_postFixIncr:
			return value + 1;
		case IASTUnaryExpression.op_prefixDecr:
		case IASTUnaryExpression.op_postFixDecr:
			return value - 1;
		case IASTUnaryExpression.op_minus:
			return -value;
		case IASTUnaryExpression.op_tilde:
			return ~value;
		case IASTUnaryExpression.op_not:
			return value == 0 ? 1 : 0;
		}
		return VALUE_CANNOT_BE_DETERMINED;
	}

	private static Number evaluateBinaryExpression(IASTBinaryExpression exp) {
		final int op= exp.getOperator();
		switch (op) {
		case IASTBinaryExpression.op_equals:
			if (exp.getOperand1().equals(exp.getOperand2()))
				return Long.valueOf(1);
			break;
		case IASTBinaryExpression.op_notequals:
			if (exp.getOperand1().equals(exp.getOperand2()))
				return Long.valueOf(0);
			break;
		}

		final Number o1= evaluate(exp.getOperand1());
		if (o1 == null || o1 == VALUE_CANNOT_BE_DETERMINED)
			return o1;
		final Number o2= evaluate(exp.getOperand2());
		if (o2 == null || o2 == VALUE_CANNOT_BE_DETERMINED)
			return o2;

		return applyBinaryOperator(op, o1.longValue(), o2.longValue());
	}

	private static Number applyBinaryOperator(final int op, final long v1, final long v2) {
		switch (op) {
		case IASTBinaryExpression.op_multiply:
			return v1 * v2;
		case IASTBinaryExpression.op_divide:
			if (v2 == 0)
				return VALUE_CANNOT_BE_DETERMINED;
			return v1 / v2;
		case IASTBinaryExpression.op_modulo:
			if (v2 == 0)
				return VALUE_CANNOT_BE_DETERMINED;
			return v1 % v2;
		case IASTBinaryExpression.op_plus:
			return v1 + v2;
		case IASTBinaryExpression.op_minus:
			return v1 - v2;
		case IASTBinaryExpression.op_shiftLeft:
			return v1 << v2;
		case IASTBinaryExpression.op_shiftRight:
			return v1 >> v2;
		case IASTBinaryExpression.op_lessThan:
			return v1 < v2 ? 1 : 0;
		case IASTBinaryExpression.op_greaterThan:
			return v1 > v2 ? 1 : 0;
		case IASTBinaryExpression.op_lessEqual:
			return v1 <= v2 ? 1 : 0;
		case IASTBinaryExpression.op_greaterEqual:
			return v1 >= v2 ? 1 : 0;
		case IASTBinaryExpression.op_binaryAnd:
			return v1 & v2;
		case IASTBinaryExpression.op_binaryXor:
			return v1 ^ v2;
		case IASTBinaryExpression.op_binaryOr:
			return v1 | v2;
		case IASTBinaryExpression.op_logicalAnd:
			return v1 != 0 && v2 != 0 ? 1 : 0;
		case IASTBinaryExpression.op_logicalOr:
			return v1 != 0 || v2 != 0 ? 1 : 0;
		case IASTBinaryExpression.op_equals:
			return v1 == v2 ? 1 : 0;
		case IASTBinaryExpression.op_notequals:
			return v1 != v2 ? 1 : 0;
        case IASTBinaryExpression.op_max:
			return Math.max(v1, v2);
        case IASTBinaryExpression.op_min:
			return Math.min(v1, v2);
		}
		return VALUE_CANNOT_BE_DETERMINED;
	}

	/**
	 * Parses a long, returns <code>null</code> if not possible
	 */
	private static Long parseLong(char[] value) {
		final long maxvalue= Long.MAX_VALUE / 10;
		final int len= value.length;
		boolean negative= false;
		long result = 0;
		int i= 0;

		if (len > 0 && value[0] == '-') {
			negative = true;
			i++;
		}
		if (i == len)
			return null;

		for (; i < len; i++) {
			if (result > maxvalue)
				return null;

			final int digit= (value[i] - '0');
			if (digit < 0 || digit > 9)
				return null;
			result= result * 10 + digit;
		}
		return negative ? -result : result;
	}

	/**
	 * Converts long to a char array
	 */
	private static char[] toCharArray(long value) {
		StringBuilder buf= new StringBuilder();
		buf.append(value);
		return CharArrayUtils.extractChars(buf);
	}
}
