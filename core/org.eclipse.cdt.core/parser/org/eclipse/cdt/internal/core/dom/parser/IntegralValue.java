/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
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

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.pdom.dom.TypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents values of variables, enumerators or expressions. The primary purpose of
 * the representation is to support instantiation of templates with non-type template parameters.
 */
public class IntegralValue implements IValue {
	public static final int MAX_RECURSION_DEPTH = 25;

	// IntegralValue.THIS represents the this pointer inside a member function / constructor.
	public static final IntegralValue THIS = new IntegralValue("this".toCharArray(), null); //$NON-NLS-1$

	// IntegralValue.UNKNOWN indicates general inability to determine a value. It doesn't have to be an error,
	// it could be that evaluation ran into a performance limit, or that we can't model this kind of
	// value (such as a pointer to a function).
	public static final IntegralValue UNKNOWN = new IntegralValue("<unknown>".toCharArray(), null) { //$NON-NLS-1$
		@Override
		public void setSubValue(int position, ICPPEvaluation newValue) {
			throw new UnsupportedOperationException();
		}
	};

	// IntegralValue.ERROR indicates that an error, such as a substitution failure, occurred during evaluation.
	public static final IntegralValue ERROR= new IntegralValue("<error>".toCharArray(), null); //$NON-NLS-1$

	public static final IntegralValue NOT_INITIALIZED= new IntegralValue("<__>".toCharArray(), null); //$NON-NLS-1$

	private static final char UNIQUE_CHAR = '_';

	private final static IntegralValue[] TYPICAL= {
			new IntegralValue(new char[] {'0'}, null),
			new IntegralValue(new char[] {'1'}, null),
			new IntegralValue(new char[] {'2'}, null),
			new IntegralValue(new char[] {'3'}, null),
			new IntegralValue(new char[] {'4'}, null),
			new IntegralValue(new char[] {'5'}, null),
			new IntegralValue(new char[] {'6'}, null)};

	private static int sUnique= 0;

	// The following invariant always holds: (fFixedValue == null) != (fEvaluation == null)
	private final char[] fFixedValue;
	private ICPPEvaluation fEvaluation;
	private char[] fSignature;

	private IntegralValue(char[] fixedValue, ICPPEvaluation evaluation) {
		assert (fixedValue == null) != (evaluation == null);
		fFixedValue = fixedValue;
		fEvaluation = evaluation;
	}

	@Override
	public Long numericalValue() {
		return (Long) numberValue();  // IntegralValue.numberValue() always returns a Long
	}

	@Override
	public final Number numberValue() {
		return fFixedValue == null ? null : parseLong(fFixedValue);
	}

	@Override
	public final ICPPEvaluation getEvaluation() {
		return fEvaluation;
	}

	@Override
	public final char[] getSignature() {
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

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		if (UNKNOWN == this) {
			buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG1));
		} else if (THIS == this) {
			buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG5));
		} else {
			Number num= numberValue();
			if (num != null) {
				long lv= num.longValue();
				if (lv >= 0) {
					buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG2));
					buf.putLong(lv);
				} else {
					buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG3));
					buf.putLong(-lv);
				}
			} else if (fFixedValue != null) {
				buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG4));
				buf.putCharArray(fFixedValue);
			} else {
				buf.putShort(ITypeMarshalBuffer.INTEGRAL_VALUE);
				fEvaluation.marshal(buf, true);
			}
		}
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		if (firstBytes == TypeMarshalBuffer.NULL_TYPE)
			return IntegralValue.UNKNOWN;
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0)
			return IntegralValue.UNKNOWN;
		if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0)
			return IntegralValue.create(buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG3) != 0)
			return IntegralValue.create(-buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG4) != 0)
			return new IntegralValue(buf.getCharArray(), null);
		if ((firstBytes & ITypeMarshalBuffer.FLAG5) != 0)
			return IntegralValue.THIS;

		ISerializableEvaluation eval= buf.unmarshalEvaluation();
		if (eval instanceof ICPPEvaluation)
			return new IntegralValue(null, (ICPPEvaluation) eval);
		return IntegralValue.UNKNOWN;
	}

	@Override
	public int hashCode() {
		return CharArrayUtils.hash(getSignature());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntegralValue)) {
			return false;
		}
		final IntegralValue rhs = (IntegralValue) obj;
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
	public static IntegralValue create(long value) {
		if (value >= 0 && value < TYPICAL.length)
			return TYPICAL[(int) value];
		return new IntegralValue(toCharArray(value), null);
	}

	/**
	 * Creates a value object representing the given boolean value.
	 */
	public static IntegralValue create(boolean value) {
		return create(value ? 1 : 0);
	}

	/**
	 * Creates a value representing the given template parameter
	 * in the given template.
	 */
	public static IntegralValue create(ICPPTemplateDefinition template, ICPPTemplateNonTypeParameter tntp) {
		EvalBinding eval = new EvalBinding(tntp, null, template);
		return new IntegralValue(null, eval);
	}

	/**
	 * Create a value wrapping the given evaluation.
	 */
	public static IntegralValue create(ICPPEvaluation eval) {
		return new IntegralValue(null, eval);
	}

	public static IValue incrementedValue(IValue value, int increment) {
		if (value == UNKNOWN)
			return UNKNOWN;
		Number val = value.numberValue();
		if (val != null) {
			return create(val.longValue() + increment);
		}
		ICPPEvaluation arg1 = value.getEvaluation();
		EvalFixed arg2 = new EvalFixed(CPPBasicType.INT, ValueCategory.PRVALUE, create(increment));
		return create(new EvalBinary(IASTBinaryExpression.op_plus, arg1, arg2, arg1.getTemplateDefinition()));
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
	 * Creates a value off its canonical representation.
	 */
	public static IValue fromInternalRepresentation(ICPPEvaluation evaluation) {
		return new IntegralValue(null, evaluation);
	}

	/**
	 * Creates a unique value needed during template instantiation.
	 */
	public static IValue unique() {
		StringBuilder buf= new StringBuilder(10);
		buf.append(UNIQUE_CHAR);
		buf.append(++sUnique);
		return new IntegralValue(CharArrayUtils.extractChars(buf), null);
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

	@Override
	public final int numberOfSubValues() {
		return 1;
	}

	@Override
	public final ICPPEvaluation getSubValue(int index) {
		return index == 0 && fEvaluation != null ? fEvaluation : EvalFixed.INCOMPLETE;
	}

	@Override
	public final ICPPEvaluation[] getAllSubValues() {
		return new ICPPEvaluation[] { getEvaluation() };
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
		if (fEvaluation == null) {
			throw new IllegalStateException("Trying to set incomplete value"); //$NON-NLS-1$
		}
		if (position == 0) {
			fEvaluation = newValue;
		} else {
			throw new IllegalArgumentException("Invalid offset in POD value: " + position); //$NON-NLS-1$
		}
	}

	@Override
	public IValue clone() {
		char[] newFixedValue = fFixedValue == null ? null : Arrays.copyOf(fFixedValue, fFixedValue.length);
		return new IntegralValue(newFixedValue, fEvaluation);
	}
}
