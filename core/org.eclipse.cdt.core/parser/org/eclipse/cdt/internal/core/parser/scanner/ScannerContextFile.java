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

import java.util.ArrayList;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;

/**
 * Wraps a {@link Lexer} and provides additional information for the preprocessor.
 * <p>
 * Note that for parsing the preprocessor directives the lexer is used directly, so this class
 * is not allowed to store any state about the lexing process.
 *
 * since 5.0
 */
public class ScannerContextFile extends ScannerContext {
	
	private final Lexer fLexer;
	private final ArrayList fBranches= new ArrayList();

	public ScannerContextFile(ILocationCtx ctx, ScannerContext parent, Lexer lexer) {
		super(ctx, parent);
		fLexer= lexer;
	}

	public Token currentPPToken() {
		return fLexer.currentToken();
	}

	public Token nextPPToken() throws OffsetLimitReachedException {
		return fLexer.nextToken();
	}

	public Lexer getLexerForPPDirective() {
		 if (fLexer.currentTokenIsFirstOnLine() && fLexer.currentToken().getType() == IToken.tPOUND) {
			 return fLexer;
		 }
		 return null;
	}

	public boolean changeBranch(Integer branchKind) {
		// an if starts a new conditional construct
		if (branchKind == BRANCH_IF) {
			fBranches.add(branchKind);
			return true;
		}
		// if we are not inside of an conditional there shouldn't be an #else, #elsif or #end
		final int pos= fBranches.size()-1;
		if (pos < 0) {
			return false;
		}
		// an #end just pops one construct.
		if (branchKind == BRANCH_END) {
			fBranches.remove(pos);
			return true;
		}
		// #elsif or #else cannot appear after another #else
		if (fBranches.get(pos) == BRANCH_ELSE) {
			return false;
		}
		// overwrite #if, #elsif with #elsif or #else
		fBranches.set(pos, branchKind);
		return true;
	}
}