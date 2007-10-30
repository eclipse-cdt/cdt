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



public class ScannerContextMacroExpansion extends ScannerContext {
	private static final Token END_TOKEN = new SimpleToken(Lexer.tEND_OF_INPUT, null, 0, 0);

	private Token fTokens;

	public ScannerContextMacroExpansion(ILocationCtx ctx, ScannerContext parent, TokenList tokens) {
		super(ctx, parent);
		fTokens= tokens.first();
	}

	public boolean changeBranch(Integer state) {
		return false;
	}

	public Token currentLexerToken() {
		Token t= fTokens;
		if (t == null) {
			return END_TOKEN;
		}
		return t;
	}

	public Lexer getLexerForPPDirective() {
		return null;
	}

	public Token nextPPToken() {
		fTokens= (Token) fTokens.getNext();
		return currentLexerToken();
	}

	public boolean expandsMacros() {
		return false;
	}
}
