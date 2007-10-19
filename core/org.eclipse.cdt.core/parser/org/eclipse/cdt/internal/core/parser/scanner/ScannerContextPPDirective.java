/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Wraps a ScannerContext and modifies its behavior by limiting the tokens
 * to the ones on the current line. Instead of the newline token an end-of-input
 * token is returned. The newline token of the underlying context is not consumed.
 * @since 5.0
 */
public final class ScannerContextPPDirective extends ScannerContext {

	private static final int STATE_PREVENT_EXPANSION = 1;
	private static final int STATE_DEFINED_LPAREN = 2;
	private static final int STATE_DEFINED = 3;
	private final Lexer fLexer;
	private Token fToken;
	private boolean fConvertDefinedToken;
	private int fPreventMacroExpansion= 0;
	private int fLastEndOffset;

	public ScannerContextPPDirective(Lexer lexer, boolean convertDefinedToken) {
		super(null, null);
		fLexer= lexer;
		final Token currentToken = lexer.currentToken();
		fLastEndOffset= currentToken.getOffset();
		fToken= convertToken(currentToken);
		fConvertDefinedToken= convertDefinedToken;
	}

	public Token currentPPToken() {
		return fToken;
	}

	public Token nextPPToken() throws OffsetLimitReachedException {
		if (fToken.getType() == Lexer.tEND_OF_INPUT) {
			return fToken;
		}
		Token t1= fLexer.nextToken();
		t1 = convertToken(t1);
		fToken= t1;
		
		Token t = t1;
		return t;
	}

	public Lexer getLexerForPPDirective() {
		return null;
	}

	public boolean changeBranch(Integer state) {
		return false;
	}
	
	private Token convertToken(Token t) {
		switch (t.getType()) {
		case Lexer.tNEWLINE:
			t= new SimpleToken(Lexer.tEND_OF_INPUT, fToken.getEndOffset(), fToken.getEndOffset());
			break;
		case IToken.tIDENTIFIER:
			if (fConvertDefinedToken && CharArrayUtils.equals(Keywords.cDEFINED, fToken.getCharImage())) {
				t.setType(CPreprocessor.tDEFINED);
				fPreventMacroExpansion= STATE_DEFINED;	
			}
			else {
				switch(fPreventMacroExpansion) {
				case STATE_DEFINED:
				case STATE_DEFINED_LPAREN:
					fPreventMacroExpansion= STATE_PREVENT_EXPANSION;
					break;
				default:
					fPreventMacroExpansion= 0;
				}
			}
			fLastEndOffset= t.getEndOffset();
			break;
		case IToken.tLPAREN:
			if (fPreventMacroExpansion == STATE_DEFINED) { 
				fPreventMacroExpansion= STATE_DEFINED_LPAREN;    // suppress macro-expansion for 'defined (id)'
			}
			else {
				fPreventMacroExpansion= 0;
			}
			fLastEndOffset= t.getEndOffset();
			break;
		default:
			fPreventMacroExpansion= 0;
			fLastEndOffset= t.getEndOffset();
			break;
		}
		return t;
	}
	
	public boolean expandsMacros() {
		return fPreventMacroExpansion == 0;
	}

	public void setInsideIncludeDirective() {
		fLexer.setInsideIncludeDirective();
	}

	public int getLastEndOffset() {
		return fLastEndOffset;
	}
}
