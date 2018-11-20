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

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IValue;
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
	public static final IntegralValue THIS = new IntegralValue("this".toCharArray()); //$NON-NLS-1$

	// IntegralValue.UNKNOWN indicates general inability to determine a value. It doesn't have to be an error,
	// it could be that evaluation ran into a performance limit, or that we can't model this kind of
	// value (such as a pointer to a function).
	public static final IntegralValue UNKNOWN = new IntegralValue("<unknown>".toCharArray()) { //$NON-NLS-1$
		@Override
		public void setSubValue(int position, ICPPEvaluation newValue) {
			throw new UnsupportedOperationException();
		}
	};

	// IntegralValue.ERROR indicates that an error, such as a substitution failure, occurred during evaluation.
	public static final IntegralValue ERROR = new IntegralValue("<error>".toCharArray()); //$NON-NLS-1$

	public static final IntegralValue NOT_INITIALIZED = new IntegralValue("<__>".toCharArray()); //$NON-NLS-1$

	private static final char UNIQUE_CHAR = '_';

	private final static IntegralValue[] TYPICAL = { new IntegralValue(new char[] { '-', '1' }),
			new IntegralValue(new char[] { '0' }), new IntegralValue(new char[] { '1' }),
			new IntegralValue(new char[] { '2' }), new IntegralValue(new char[] { '3' }),
			new IntegralValue(new char[] { '4' }), new IntegralValue(new char[] { '5' }),
			new IntegralValue(new char[] { '6' }), new IntegralValue(new char[] { '7' }) };

	private static int sUnique = 0;

	private final char[] fFixedValue;

	private IntegralValue(char[] fixedValue) {
		fFixedValue = fixedValue;
	}

	@Override
	public final Number numberValue() {
		return parseLong(fFixedValue);
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return null;
	}

	@Override
	public final char[] getSignature() {
		return fFixedValue;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		if (UNKNOWN == this) {
			buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG1));
		} else if (ERROR == this) {
			buf.putShort(
					(short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG1 | ITypeMarshalBuffer.FLAG2));
		} else if (THIS == this) {
			buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG5));
		} else {
			Number num = numberValue();
			if (num != null) {
				long lv = num.longValue();
				if (lv >= 0) {
					buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG2));
					buf.putLong(lv);
				} else {
					buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG3));
					buf.putLong(-lv);
				}
			} else {
				buf.putShort((short) (ITypeMarshalBuffer.INTEGRAL_VALUE | ITypeMarshalBuffer.FLAG4));
				buf.putCharArray(fFixedValue);
			}
		}
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		if (firstBytes == TypeMarshalBuffer.NULL_TYPE)
			return UNKNOWN;
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0) {
			if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0)
				return ERROR;
			return UNKNOWN;
		}
		if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0)
			return create(buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG3) != 0)
			return create(-buf.getLong());
		if ((firstBytes & ITypeMarshalBuffer.FLAG4) != 0)
			return new IntegralValue(buf.getCharArray());
		if ((firstBytes & ITypeMarshalBuffer.FLAG5) != 0)
			return THIS;

		return UNKNOWN;
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
		if (value >= -1 && value < TYPICAL.length - 1)
			return TYPICAL[(int) value + 1];
		return new IntegralValue(toCharArray(value));
	}

	/**
	 * Creates a value object representing the given boolean value.
	 */
	public static IntegralValue create(boolean value) {
		return create(value ? 1 : 0);
	}

	public static IValue incrementedValue(IValue value, int increment) {
		if (value == UNKNOWN)
			return UNKNOWN;
		if (value == ERROR)
			return ERROR;
		Number val = value.numberValue();
		if (val != null) {
			return create(val.longValue() + increment);
		}
		ICPPEvaluation arg1 = value.getEvaluation();
		EvalFixed arg2 = new EvalFixed(CPPBasicType.INT, ValueCategory.PRVALUE, create(increment));
		return DependentValue
				.create(new EvalBinary(IASTBinaryExpression.op_plus, arg1, arg2, arg1.getTemplateDefinition()));
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
	 * Creates a unique value needed during template instantiation.
	 */
	public static IValue unique() {
		StringBuilder buf = new StringBuilder(10);
		buf.append(UNIQUE_CHAR);
		buf.append(++sUnique);
		return new IntegralValue(CharArrayUtils.extractChars(buf));
	}

	/**
	 * Parses a long, returns <code>null</code> if not possible
	 */
	private static Long parseLong(char[] value) {
		final long maxvalue = Long.MAX_VALUE / 10;
		final int len = value.length;
		boolean negative = false;
		long result = 0;
		int i = 0;

		if (len > 0 && value[0] == '-') {
			negative = true;
			i++;
		}
		if (i == len)
			return null;

		for (; i < len; i++) {
			if (result > maxvalue)
				return null;

			final int digit = (value[i] - '0');
			if (digit < 0 || digit > 9)
				return null;
			result = result * 10 + digit;
		}
		return negative ? -result : result;
	}

	/**
	 * Converts long to a char array
	 */
	private static char[] toCharArray(long value) {
		StringBuilder buf = new StringBuilder();
		buf.append(value);
		return CharArrayUtils.extractChars(buf);
	}

	@Override
	public final int numberOfSubValues() {
		return 1;
	}

	@Override
	public final ICPPEvaluation getSubValue(int index) {
		return EvalFixed.INCOMPLETE;
	}

	@Override
	public final ICPPEvaluation[] getAllSubValues() {
		return new ICPPEvaluation[] { getEvaluation() };
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
		throw new IllegalStateException("Trying to set incomplete value"); //$NON-NLS-1$
	}

	@Override
	public IValue clone() {
		return new IntegralValue(Arrays.copyOf(fFixedValue, fFixedValue.length));
	}

	@Override
	public boolean isEquivalentTo(IValue other) {
		if (!(other instanceof IntegralValue)) {
			return false;
		}
		IntegralValue o = (IntegralValue) other;
		return fFixedValue.equals(o.fFixedValue);
	}
}
