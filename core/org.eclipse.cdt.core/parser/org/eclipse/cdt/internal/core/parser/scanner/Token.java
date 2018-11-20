/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IParserSettings;
import org.eclipse.cdt.core.parser.IParserSettings2;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParseError;

/**
 * Represents tokens found by the lexer. The preprocessor reuses the tokens and passes
 * them on to the parsers.
 * @since 5.0
 */
public class Token implements IToken, Cloneable {
	private int fKind;
	private int fOffset;
	private int fEndOffset;
	private IToken fNextToken;
	Object fSource;

	private static final Counter tokenCounter = new Counter();

	Token(int kind, Object source, int offset, int endOffset) {
		tokenCounter.inc();
		fKind = kind;
		fOffset = offset;
		fEndOffset = endOffset;
		fSource = source;
	}

	@Override
	final public int getType() {
		return fKind;
	}

	@Override
	final public int getOffset() {
		return fOffset;
	}

	@Override
	final public int getEndOffset() {
		return fEndOffset;
	}

	@Override
	final public int getLength() {
		return fEndOffset - fOffset;
	}

	@Override
	final public IToken getNext() {
		return fNextToken;
	}

	@Override
	final public void setType(int kind) {
		fKind = kind;
	}

	@Override
	final public void setNext(IToken t) {
		fNextToken = t;
	}

	public void setOffset(int offset, int endOffset) {
		fOffset = offset;
		fEndOffset = endOffset;
	}

	public void shiftOffset(int shift) {
		fOffset += shift;
		fEndOffset += shift;
	}

	@Override
	public char[] getCharImage() {
		return TokenUtil.getImage(getType());
	}

	@Override
	public String toString() {
		return getImage();
	}

	@Override
	final public boolean isOperator() {
		return TokenUtil.isOperator(fKind);
	}

	@Override
	public String getImage() {
		return new String(getCharImage());
	}

	@Override
	final public Token clone() {
		try {
			tokenCounter.inc();
			return (Token) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * Either disable the token counter or reset it to the limit in the given scanner info object.
	 */
	public static void resetCounterFor(IScannerInfo info) {
		tokenCounter.reset(info);
	}

	/**
	 * Bug 425711: Some source files cause the CPreprocessor to try to allocate an unmanageable number
	 * of Tokens.  For example, boost has a file, delay.c, that caused over 250 million instances to
	 * be created -- that is where the VM overflowed my 3Gb heap.  Both gcc and clang also ran
	 * out of memory and crashed while processing that file.
	 * <p>
	 * Giving up on a file is better than crashing the entire IDE, so a new user-preference provide
	 * a way to specify a limit.  The preference is implemented by counting the number of instances
	 * of Token that are created by a single instance of CPreprocessor.
	 * <p>
	 * This counter records the total and throws an exception if the limit is surpassed.
	 */
	private static class Counter {
		public int count = 0;
		public int limit = -1;

		public void reset(IScannerInfo info) {
			// The counters are always reset, we optionally apply a new limit if the settings
			// are found.
			count = 0;
			limit = -1;

			if (info instanceof ExtendedScannerInfo) {
				IParserSettings settings = ((ExtendedScannerInfo) info).getParserSettings();
				if (settings instanceof IParserSettings2) {
					IParserSettings2 parserSettings = (IParserSettings2) settings;
					if (parserSettings.shouldLimitTokensPerTranslationUnit()) {
						int maxTokens = parserSettings.getMaximumTokensPerTranslationUnit();
						if (maxTokens > 0)
							limit = maxTokens;
					}
				}
			}
		}

		public void inc() throws ParseError {
			if (limit > 0 && ++count > limit)
				throw new ParseError(Integer.toString(count) + " tokens", ParseError.ParseErrorKind.TOO_MANY_TOKENS);//$NON-NLS-1$
		}
	}
}
