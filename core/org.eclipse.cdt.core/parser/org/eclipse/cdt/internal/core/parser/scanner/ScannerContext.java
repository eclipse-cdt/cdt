/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * Represents part of the input to the preprocessor. This may be a file or the result of a macro expansion.
 * @since 5.0
 */
final class ScannerContext {
	private static final Token END_TOKEN = new Token(IToken.tEND_OF_INPUT, null, 0, 0);

	public static final Integer BRANCH_IF = new Integer(0);
	public static final Integer BRANCH_ELIF = new Integer(1);
	public static final Integer BRANCH_ELSE = new Integer(2);
	public static final Integer BRANCH_END = new Integer(3);
	
	private final ILocationCtx fLocationCtx;
	private final ScannerContext fParent;
	private final Lexer fLexer;
	private ArrayList<Integer> fBranches= null;

	private Token fTokens;

	/**
	 * @param ctx 
	 * @param parent context to be used after this context is done.
	 */
	public ScannerContext(ILocationCtx ctx, ScannerContext parent, Lexer lexer) {
		fLocationCtx= ctx;
		fParent= parent;
		fLexer= lexer;
	}
	
	public ScannerContext(ILocationCtx ctx, ScannerContext parent, TokenList tokens) {
		fLocationCtx= ctx;
		fParent= parent;
		fLexer= null;
		fTokens= tokens.first();
	}

	/**
	 * Returns the location context associated with this scanner context.
	 */
	public final ILocationCtx getLocationCtx() {
		return fLocationCtx;
	}
	
	/**
	 * Returns the parent context which will be used after this context is finished.
	 * May return <code>null</code>.
	 */
	public final ScannerContext getParent() {
		return fParent;
	}

	/**
	 * Returns the lexer for this context.
	 */
	public final Lexer getLexer() {
		return fLexer;
	}

	/**
	 * Needs to be called whenever we change over to another branch of conditional 
	 * compilation. Returns whether the change is legal at this point or not.
	 */
	public final boolean changeBranch(Integer branchKind) {
		if (fBranches == null) {
			fBranches= new ArrayList<Integer>();
		}
		
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

	/** 
	 * Returns the current token from this context. When called before calling nextPPToken() 
	 * a token of type {@link Lexer#tBEFORE_INPUT} will be returned.
	 * @since 5.0
	 */
	public final Token currentLexerToken() {
		if (fLexer != null) {
			return fLexer.currentToken();
		}
		if (fTokens != null) {
			return fTokens;
		}
		return END_TOKEN;
	}
	
	/** 
	 * Returns the next token from this context. 
	 */
	public Token nextPPToken() throws OffsetLimitReachedException {
		if (fLexer != null) {
			return fLexer.nextToken();
		}
		if (fTokens != null) {
			fTokens= (Token) fTokens.getNext();
		}
		return currentLexerToken();
	}

	/**
	 * If this is a lexer based context the current line is consumed.
	 * @see Lexer#consumeLine(int)
	 */
	public int consumeLine(int originPreprocessorDirective) throws OffsetLimitReachedException {
		if (fLexer != null)
			return fLexer.consumeLine(originPreprocessorDirective);
		return -1;
	}
}
