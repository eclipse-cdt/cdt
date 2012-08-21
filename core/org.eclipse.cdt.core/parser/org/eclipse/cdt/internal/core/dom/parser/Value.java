/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator.EvalException;
import org.eclipse.cdt.internal.core.pdom.db.TypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents values of variables, enumerators or expressions. The primary purpose of
 * the representation is to support instantiation of templates with non-type template parameters.
 */
public class Value implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;
	public static final Value UNKNOWN= new Value("<unknown>".toCharArray(), null); //$NON-NLS-1$
	public static final Value NOT_INITIALIZED= new Value("<__>".toCharArray(), null); //$NON-NLS-1$

	private static final char UNIQUE_CHAR = '_';

	private final static IValue[] TYPICAL= {
		new Value(new char[] {'0'}, null),
		new Value(new char[] {'1'}, null),
		new Value(new char[] {'2'}, null),
		new Value(new char[] {'3'}, null),
		new Value(new char[] {'4'}, null),
		new Value(new char[] {'5'}, null),
		new Value(new char[] {'6'}, null)};


	private static class UnknownValueException extends Exception {}
	private static UnknownValueException UNKNOWN_EX= new UnknownValueException();
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

	public void marshall(ITypeMarshalBuffer buf) throws CoreException {
		if (UNKNOWN == this) {
			buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG1));
		} else {
			Long num= numericalValue();
			if (num != null) {
				long lv= num;
				if (lv >= Integer.MIN_VALUE && lv <= Integer.MAX_VALUE) {
					buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG2));
					buf.putInt((int) lv);
				} else {
					buf.putByte((byte) (ITypeMarshalBuffer.VALUE | ITypeMarshalBuffer.FLAG3));
					buf.putLong(lv);
				}
			} else {
				buf.putByte((ITypeMarshalBuffer.VALUE));
				fEvaluation.marshal(buf, true);
			}
		}
	}

	public static IValue unmarshal(ITypeMarshalBuffer buf) throws CoreException {
		int firstByte= buf.getByte();
		if (firstByte == TypeMarshalBuffer.NULL_TYPE)
			return null;
		if ((firstByte & ITypeMarshalBuffer.FLAG1) != 0)
			return Value.UNKNOWN;
		if ((firstByte & ITypeMarshalBuffer.FLAG2) != 0) {
			int val= buf.getInt();
			return Value.create(val);
		}
		if ((firstByte & ITypeMarshalBuffer.FLAG3) != 0) {
			long val= buf.getLong();
			return Value.create(val);
		}

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

	@Override
	public String toString() {
		return new String(getSignature());
	}

	/**
	 * Creates a value representing the given number.
	 */
	public static IValue create(long value) {
		if (value >=0 && value < TYPICAL.length)
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
	 * Creates a value representing the given template parameter.
	 */
	public static IValue create(ICPPTemplateNonTypeParameter tntp) {
		EvalBinding eval = new EvalBinding(tntp, null);
		return new Value(null, eval);
	}

	/**
	 * Create a value wrapping the given evaluation.
	 */
	public static IValue create(ICPPEvaluation eval) {
		return new Value(null, eval);
	}

	public static IValue evaluateBinaryExpression(final int op, final long v1, final long v2) {
		try {
			return create(combineBinary(op, v1, v2));
		} catch (UnknownValueException e) {
		}
		return UNKNOWN;
	}

	public static IValue evaluateUnaryExpression(final int unaryOp, final long value) {
		try {
			return create(combineUnary(unaryOp, value));
		} catch (UnknownValueException e) {
		}
		return UNKNOWN;
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
			IBinding binding = ((EvalBinding) eval).getBinding();
			if (binding instanceof ICPPTemplateParameter) {
				return ((ICPPTemplateParameter) binding).getParameterID();
			}
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
		return nonTypeValue.getEvaluation() != null;
	}

	/**
	 * Creates the value for an expression.
	 */
	public static IValue create(IASTExpression expr, int maxRecursionDepth) {
		try {
			Object obj= evaluate(expr, maxRecursionDepth);
			if (obj instanceof Long)
				return create(((Long) obj).longValue());

			if (expr instanceof ICPPASTInitializerClause) {
				ICPPEvaluation evaluation = ((ICPPASTInitializerClause) expr).getEvaluation();
				return new Value(null, evaluation);
			}
		} catch (UnknownValueException e) {
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
	 * Returns a {@code Long} for numerical values or {@code null}, otherwise.
	 * @throws UnknownValueException
	 */
	private static Long evaluate(IASTExpression exp, int maxdepth) throws UnknownValueException {
		if (maxdepth < 0 || exp == null)
			throw UNKNOWN_EX;

		if (exp instanceof IASTArraySubscriptExpression) {
			throw UNKNOWN_EX;
		}
		if (exp instanceof IASTBinaryExpression) {
			return evaluateBinaryExpression((IASTBinaryExpression) exp, maxdepth);
		}
		if (exp instanceof IASTCastExpression) { // must be ahead of unary
			return evaluate(((IASTCastExpression) exp).getOperand(), maxdepth);
		}
		if (exp instanceof IASTUnaryExpression) {
			return evaluateUnaryExpression((IASTUnaryExpression) exp, maxdepth);
		}
		if (exp instanceof IASTConditionalExpression) {
			IASTConditionalExpression cexpr= (IASTConditionalExpression) exp;
			Long v= evaluate(cexpr.getLogicalConditionExpression(), maxdepth);
			if (v == null)
				return null;
			if (v.longValue() == 0) {
				return evaluate(cexpr.getNegativeResultExpression(), maxdepth);
			}
			final IASTExpression pe = cexpr.getPositiveResultExpression();
			if (pe == null) // gnu-extension allows to omit the positive expression.
				return v;
			return evaluate(pe, maxdepth);
		}
		if (exp instanceof IASTIdExpression) {
			IBinding b= ((IASTIdExpression) exp).getName().resolvePreBinding();
			return evaluateBinding(b, maxdepth);
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
					throw UNKNOWN_EX;
				}
			case IASTLiteralExpression.lk_char_constant:
				try {
					final char[] image= litEx.getValue();
					if (image.length > 1 && image[0] == 'L')
						return ExpressionEvaluator.getChar(image, 2);
					return ExpressionEvaluator.getChar(image, 1);
				} catch (EvalException e) {
					throw UNKNOWN_EX;
				}
			}
		}
		if (exp instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression typeIdEx = (IASTTypeIdExpression) exp;
			switch (typeIdEx.getOperator()) {
			case IASTTypeIdExpression.op_sizeof:
				ASTTranslationUnit ast = (ASTTranslationUnit) typeIdEx.getTranslationUnit();
				final IType type = ast.createType(typeIdEx.getTypeId());
				if (type instanceof ICPPUnknownType)
					return null;
				SizeofCalculator calculator = ast.getSizeofCalculator();
				SizeAndAlignment info = calculator.sizeAndAlignment(type);
				if (info == null)
					throw UNKNOWN_EX;
				return info.size;
			}
		}
		throw UNKNOWN_EX;
	}

	/**
	 * Extract a value off a binding.
	 */
	private static Long evaluateBinding(IBinding b, int maxdepth) throws UnknownValueException {
		if (b instanceof IType) {
			throw UNKNOWN_EX;
		}
		if (b instanceof ICPPTemplateNonTypeParameter) {
			return null;
		}

		if (b instanceof ICPPUnknownBinding) {
			return null;
		}

		IValue value= null;
		if (b instanceof IInternalVariable) {
			value= ((IInternalVariable) b).getInitialValue(maxdepth - 1);
		} else if (b instanceof IVariable) {
			value= ((IVariable) b).getInitialValue();
		} else if (b instanceof IEnumerator) {
			value= ((IEnumerator) b).getValue();
		}
		if (value != null && value != Value.UNKNOWN) {
			return value.numericalValue();
		}

		throw UNKNOWN_EX;
	}

	private static Long evaluateUnaryExpression(IASTUnaryExpression exp, int maxdepth)
			throws UnknownValueException {
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
			throw UNKNOWN_EX;
		}

		if (unaryOp == IASTUnaryExpression.op_amper || unaryOp == IASTUnaryExpression.op_star ||
				unaryOp == IASTUnaryExpression.op_sizeofParameterPack) {
			throw UNKNOWN_EX;
		}

		final Long value= evaluate(exp.getOperand(), maxdepth);
		if (value == null)
			return null;
		return combineUnary(unaryOp, value);
	}

	private static long combineUnary(final int unaryOp, final long value) throws UnknownValueException {
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
		throw UNKNOWN_EX;
	}

	private static Long evaluateBinaryExpression(IASTBinaryExpression exp, int maxdepth)
			throws UnknownValueException {
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

		final Long o1= evaluate(exp.getOperand1(), maxdepth);
		if (o1 == null)
			return null;
		final Long o2= evaluate(exp.getOperand2(), maxdepth);
		if (o2 == null)
			return null;

		return combineBinary(op, o1, o2);
	}

	private static long combineBinary(final int op, final long v1, final long v2)
			throws UnknownValueException {
		switch (op) {
		case IASTBinaryExpression.op_multiply:
			return v1 * v2;
		case IASTBinaryExpression.op_divide:
			if (v2 == 0)
				throw UNKNOWN_EX;
			return v1 / v2;
		case IASTBinaryExpression.op_modulo:
			if (v2 == 0)
				throw UNKNOWN_EX;
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
		throw UNKNOWN_EX;
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
