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

import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;

public class ObjectStyleMacro extends PreprocessorMacro {
	private final AbstractCharArray fExpansion;
	private final int fExpansionOffset;
	private final int fEndOffset;
	private TokenList fExpansionTokens;

	public ObjectStyleMacro(char[] name, char[] expansion) {
		this(name, 0, expansion.length, null, new CharArray(expansion));
	}

	public ObjectStyleMacro(char[] name, int expansionOffset, int endOffset, TokenList expansion,
			AbstractCharArray source) {
		super(name);
		fExpansionOffset = expansionOffset;
		fEndOffset = endOffset;
		fExpansion = source;
		fExpansionTokens = expansion;
		if (expansion != null) {
			setSource(expansion.first());
		}
	}

	public int getExpansionOffset() {
		return fExpansionOffset;
	}

	public int getExpansionEndOffset() {
		return fEndOffset;
	}

	private void setSource(Token t) {
		final int shift = -fExpansionOffset;
		while (t != null) {
			t.fSource = this;
			t.shiftOffset(shift);
			t = (Token) t.getNext();
		}
	}

	@Override
	public char[] getExpansion() {
		return MacroDefinitionParser.getExpansion(fExpansion, fExpansionOffset, fEndOffset);
	}

	@Override
	public char[] getExpansionImage() {
		final int length = fEndOffset - fExpansionOffset;
		char[] result = new char[length];
		fExpansion.arraycopy(fExpansionOffset, result, 0, length);
		return result;
	}

	@Override
	public TokenList getTokens(MacroDefinitionParser mdp, LexerOptions lexOptions, MacroExpander expander) {
		if (fExpansionTokens == null) {
			fExpansionTokens = new TokenList();
			Lexer lex = new Lexer(fExpansion, fExpansionOffset, fEndOffset, lexOptions, ILexerLog.NULL, this);
			try {
				lex.nextToken(); // consume the start token
				mdp.parseExpansion(lex, ILexerLog.NULL, getNameCharArray(), getParameterPlaceholderList(),
						fExpansionTokens);
			} catch (OffsetLimitReachedException e) {
			}
		}
		return fExpansionTokens;
	}

	@Override
	public final boolean isDynamic() {
		return false;
	}
}
