/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.core.runtime.CoreException;

public final class FloatingPointValue implements IValue {
	private final char[] fFixedValue;

	private FloatingPointValue(char[] fixedValue) {
		fFixedValue = fixedValue;
	}

	public static FloatingPointValue create(char[] fixedValue) {
		return new FloatingPointValue(fixedValue);
	}

	public static FloatingPointValue create(double value) {
		return new FloatingPointValue(toCharArray(value));
	}

	@Override
	public Number numberValue() {
		return parseDouble(fFixedValue);
	}

	private static Double parseDouble(char[] value) {
		double result = 0.0;
		int i = 0;
		int len = value.length;

		boolean valueIsPositive = true;
		if (i < len && (value[i] == '+' || value[i] == '-')) {
			valueIsPositive = (value[i] == '+');
			++i;
		}

		while (i < len && value[i] >= '0' && value[i] <= '9') {
			int digit = value[i] - '0';
			result = result * 10 + digit;
			++i;
		}

		if (i < len && value[i] == '.') {
			++i;
		}

		double div = 10.0;
		while (i < len && value[i] >= '0' && value[i] <= '9') {
			int digit = value[i] - '0';
			result += digit / div;
			div *= 10.0;
			++i;
		}

		if (i < len && (value[i] == 'e' || value[i] == 'E')) {
			++i;
		}

		boolean exponentIsPositive = true;
		if (i < len && (value[i] == '+' || value[i] == '-')) {
			exponentIsPositive = (value[i] == '+');
			++i;
		}

		int exponent = 0;
		while (i < len && value[i] >= '0' && value[i] <= '9') {
			int digit = value[i] - '0';
			exponent = exponent * 10 + digit;
			++i;
		}

		if (i < len && (value[i] == 'l' || value[i] == 'L' || value[i] == 'f' || value[i] == 'F')) {
			++i;
		}

		if (i == len) {
			if (!exponentIsPositive) {
				exponent *= -1;
			}
			if (!valueIsPositive) {
				result *= -1;
			}
			return result * Math.pow(10, exponent);
		}
		return null;
	}

	@Override
	public int numberOfSubValues() {
		return 1;
	}

	@Override
	public ICPPEvaluation getSubValue(int index) {
		if (index == 0) {
			return getEvaluation();
		}
		return EvalFixed.INCOMPLETE;
	}

	@Override
	public ICPPEvaluation[] getAllSubValues() {
		return new ICPPEvaluation[] { getEvaluation() };
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		return null;
	}

	@Override
	public char[] getSignature() {
		return fFixedValue;
	}

	@Override
	public int hashCode() {
		return CharArrayUtils.hash(getSignature());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FloatingPointValue)) {
			return false;
		}
		final FloatingPointValue rhs = (FloatingPointValue) obj;
		if (fFixedValue != null)
			return CharArrayUtils.equals(fFixedValue, rhs.fFixedValue);
		return CharArrayUtils.equals(getSignature(), rhs.getSignature());
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
	}

	private static char[] toCharArray(double value) {
		StringBuilder buf = new StringBuilder();
		buf.append(value);
		return CharArrayUtils.extractChars(buf);
	}

	@Override
	public String toString() {
		return new String(getSignature());
	}

	@Override
	public IValue clone() {
		char[] newFixedValue = Arrays.copyOf(fFixedValue, fFixedValue.length);
		return new FloatingPointValue(newFixedValue);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		buf.putShort(ITypeMarshalBuffer.FLOATING_POINT_VALUE);
		buf.putCharArray(fFixedValue);
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		return new FloatingPointValue(buf.getCharArray());
	}

	@Override
	public boolean isEquivalentTo(IValue other) {
		if (!(other instanceof FloatingPointValue)) {
			return false;
		}
		FloatingPointValue o = (FloatingPointValue) other;
		return fFixedValue.equals(o.fFixedValue);
	}
}
