/*******************************************************************************
 * Copyright (c) 2007, 2020 Wind River Systems, Inc. and others.
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
 *     Alexander Fedorov (ArSysOp) - Bug 561992
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.Keywords;

public class FunctionStyleMacro extends ObjectStyleMacro {
	public static final int NO_VAARGS = 0; // M(a)
	public static final int VAARGS = 1; // M(...)
	public static final int NAMED_VAARGS = 2; // M(a...)

	final private char[][] fParamList;
	final private int fHasVarArgs;
	private char[] fSignature;

	public FunctionStyleMacro(char[] name, char[][] paramList, int hasVarArgs, char[] expansion) {
		this(name, paramList, hasVarArgs, 0, expansion.length, null, new CharArray(expansion));
	}

	public FunctionStyleMacro(char[] name, char[][] paramList, int hasVarArgs, AbstractCharArray expansion,
			int expansionOffset, int expansionEndOffset) {
		this(name, paramList, hasVarArgs, expansionOffset, expansionEndOffset, null, expansion);
	}

	public FunctionStyleMacro(char[] name, char[][] paramList, int hasVarArgs, int expansionFileOffset,
			int endFileOffset, TokenList expansion, AbstractCharArray source) {
		super(name, expansionFileOffset, endFileOffset, expansion, source);
		fParamList = paramList;
		fHasVarArgs = hasVarArgs;
	}

	@Override
	public char[][] getParameterList() {
		final int length = fParamList.length;
		if (fHasVarArgs == NO_VAARGS || length == 0) {
			return fParamList;
		}
		char[][] result = new char[length][];
		System.arraycopy(fParamList, 0, result, 0, length - 1);
		if (fHasVarArgs == VAARGS) {
			result[length - 1] = Keywords.cpELLIPSIS;
		} else {
			final char[] param = fParamList[length - 1];
			final int plen = param.length;
			final int elen = Keywords.cpELLIPSIS.length;
			final char[] rp = new char[plen + elen];
			System.arraycopy(param, 0, rp, 0, plen);
			System.arraycopy(Keywords.cpELLIPSIS, 0, rp, plen, elen);
			result[length - 1] = rp;
		}
		return result;
	}

	@Override
	public char[][] getParameterPlaceholderList() {
		return fParamList;
	}

	public char[] getSignature() {
		if (fSignature != null) {
			return fSignature;
		}

		StringBuffer result = new StringBuffer();
		result.append(getName());
		result.append('(');

		final int lastIdx = fParamList.length - 1;
		if (lastIdx >= 0) {
			for (int i = 0; i < lastIdx; i++) {
				result.append(fParamList[i]);
				result.append(',');
			}
			switch (fHasVarArgs) {
			case VAARGS:
				result.append(Keywords.cpELLIPSIS);
				break;
			case NAMED_VAARGS:
				result.append(fParamList[lastIdx]);
				result.append(Keywords.cpELLIPSIS);
				break;
			default:
				result.append(fParamList[lastIdx]);
				break;
			}
		}
		result.append(')');
		final int len = result.length();
		final char[] sig = new char[len];
		result.getChars(0, len, sig, 0);
		fSignature = sig;
		return sig;
	}

	/**
	 * Returns one of {@link FunctionStyleMacro#NO_VAARGS}, {@link #VAARGS} or {@link #NAMED_VAARGS}.
	 */
	@Override
	public int hasVarArgs() {
		return fHasVarArgs;
	}

	@Override
	public boolean isFunctionStyle() {
		return true;
	}
}
