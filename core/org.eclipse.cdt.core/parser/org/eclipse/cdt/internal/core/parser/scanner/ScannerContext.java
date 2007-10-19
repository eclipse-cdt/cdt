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

import org.eclipse.cdt.core.parser.OffsetLimitReachedException;



/**
 * Represents part of the input to the preprocessor. This may be a file or the result of a macro expansion.
 * @since 5.0
 */
abstract class ScannerContext {
	public static final Integer BRANCH_IF = new Integer(0);
	public static final Integer BRANCH_ELIF = new Integer(1);
	public static final Integer BRANCH_ELSE = new Integer(2);
	public static final Integer BRANCH_END = new Integer(3);
	
	private final ILocationCtx fLocationCtx;
	private final ScannerContext fParent;
	
	/**
	 * @param ctx 
	 * @param parent context to be used after this context is done.
	 */
	public ScannerContext(ILocationCtx ctx, ScannerContext parent) {
		fLocationCtx= ctx;
		fParent= parent;
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
	 * Tests whether or not the current identifier of this context are subject to macro-expansion.
	 */
	public boolean expandsMacros() {
		return true;
	}

	/**
	 * Returns the lexer for a preprocessing directive or <code>null</code> if the current
	 * token is not the start of a preprocessing directive.
	 * <p>
	 * The current token starts a directive, whenever the context supports directives,
	 * and the current token is a pound that occurs as the first token on the line. 
	 */
	public abstract Lexer getLexerForPPDirective();

	/**
	 * Needs to be called whenever we change over to another branch of conditional 
	 * compilation. Returns whether the change is legal at this point or not.
	 */
	public abstract boolean changeBranch(Integer state);


	public abstract Token currentPPToken();
	public abstract Token nextPPToken() throws OffsetLimitReachedException;
}
