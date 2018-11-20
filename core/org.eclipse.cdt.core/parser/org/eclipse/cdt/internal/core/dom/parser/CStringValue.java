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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.core.runtime.CoreException;

public final class CStringValue implements IValue {
	private static final Map<Character, Character> escapeSequences;
	static {
		Map<Character, Character> map = new HashMap<>();
		map.put('\'', '\'');
		map.put('"', '"');
		map.put('?', '?');
		map.put('\\', '\\');
		map.put('a', '\007');
		map.put('b', '\b');
		map.put('f', '\f');
		map.put('n', '\n');
		map.put('r', '\r');
		map.put('t', '\t');
		map.put('v', '\013');
		escapeSequences = Collections.unmodifiableMap(map);
	}

	private final char[] fFixedValue;

	private String fParsedValue;

	private CStringValue(char[] fixedValue) {
		fFixedValue = fixedValue;
	}

	public static IValue create(char[] fixedValue) {
		return new CStringValue(fixedValue);
	}

	public String cStringValue() {
		if (fParsedValue == null) {
			fParsedValue = parseString();
		}
		return fParsedValue;
	}

	private int indexOfStartQuote() {
		final int len = fFixedValue.length;
		int i = 0;
		while (i < len && fFixedValue[i] != '"') {
			++i;
		}
		if (i >= len) {
			return -1;
		} else {
			return i;
		}
	}

	private int indexOfEndQuote() {
		int i = fFixedValue.length - 1;
		while (i >= 0 && fFixedValue[i] != '"') {
			--i;
		}
		if (i < 0) {
			return -1;
		} else {
			return i;
		}
	}

	private boolean isRawStringLiteral() {
		for (int i = 0; i < indexOfStartQuote(); ++i) {
			if (fFixedValue[i] == 'R') {
				return true;
			}
		}
		return false;
	}

	private int getDelimiterLength() {
		if (isRawStringLiteral()) {
			int i = indexOfStartQuote();
			int len = 0;
			while (i < fFixedValue.length && fFixedValue[i] != '(') {
				++i;
				++len;
			}
			return len;
		}
		return 0;
	}

	private int getStart() {
		return indexOfStartQuote() + getDelimiterLength() + 1;
	}

	private int getEnd() {
		return indexOfEndQuote() - getDelimiterLength() - 1;
	}

	private String parseString() {
		// TODO: Reuse code between this and CPPASTLiteralExpression.computeStringLiteralSize().
		boolean isRaw = isRawStringLiteral();
		int end = getEnd();

		StringBuilder builder = new StringBuilder();
		for (int i = getStart(); i <= end; ++i) {
			if (!isRaw && fFixedValue[i] == '\\' && i < end) {
				++i;

				//C-Strings are null-terminated. Therefore, a '\0' character
				//denotes the end of the string, even if the literal contains
				//more characters after that
				if (fFixedValue[i] == '0') {
					break;
				} else {
					i = parseEscapeSequence(i, builder);
				}
			} else {
				builder.append(fFixedValue[i]);
			}
		}
		return builder.toString();
	}

	private int parseEscapeSequence(int i, StringBuilder builder) {
		char c = fFixedValue[i];
		Character escapeSequence = escapeSequences.get(c);
		if (escapeSequence != null) {
			builder.append(escapeSequence);
		} else if (c == 'u' && i + 4 <= getEnd()) {
			StringBuilder hexStr = new StringBuilder();
			++i;
			for (int end = i + 4; i < end; ++i) {
				hexStr.append(fFixedValue[i]);
			}
			int codePoint = Integer.parseInt(hexStr.toString(), 16);
			builder.append(Character.toChars(codePoint));
		}
		return i;
	}

	@Override
	public Number numberValue() {
		return null;
	}

	@Override
	public int numberOfSubValues() {
		String str = cStringValue();
		return str.length();
	}

	@Override
	public ICPPEvaluation getSubValue(int index) {
		String str = cStringValue();
		Character c = null;
		if (index >= 0 && index < str.length()) {
			c = str.charAt(index);
		} else if (index == str.length()) {
			c = '\0';
		}

		if (c != null) {
			IValue val = IntegralValue.create(c);
			return new EvalFixed(CPPBasicType.CHAR, ValueCategory.PRVALUE, val);
		}
		return EvalFixed.INCOMPLETE;
	}

	@Override
	public ICPPEvaluation[] getAllSubValues() {
		return null;
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
		if (!(obj instanceof CStringValue)) {
			return false;
		}
		final CStringValue rhs = (CStringValue) obj;
		if (fFixedValue != null)
			return CharArrayUtils.equals(fFixedValue, rhs.fFixedValue);
		return CharArrayUtils.equals(getSignature(), rhs.getSignature());
	}

	@Override
	public void setSubValue(int position, ICPPEvaluation newValue) {
	}

	@Override
	public IValue clone() {
		char[] newFixedValue = Arrays.copyOf(fFixedValue, fFixedValue.length);
		return new CStringValue(newFixedValue);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buf) throws CoreException {
		buf.putShort(ITypeMarshalBuffer.C_STRING_VALUE);
		buf.putCharArray(fFixedValue);
	}

	public static IValue unmarshal(short firstBytes, ITypeMarshalBuffer buf) throws CoreException {
		return new CStringValue(buf.getCharArray());
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return new String(fFixedValue);
	}

	@Override
	public boolean isEquivalentTo(IValue other) {
		if (!(other instanceof CStringValue)) {
			return false;
		}
		CStringValue o = (CStringValue) other;
		return fFixedValue.equals(o.fFixedValue);
	}
}