/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;

/**
 * The exception is thrown, when content-assist is requested within a context that is handled
 * by the lexer or the preprocessor.
 * <p>
 * {@link #ORIGIN_LEXER}: char-literal, string-literal, number-literal, header-name.
 * <p>
 * {@link #ORIGIN_PREPROCESSOR}: preprocessor-directive.
 * <p>
 * {@link #ORIGIN_INACTIVE_CODE}: within an inactive branch of conditional compilation.
 * <p>
 * {@link #ORIGIN_MACRO_EXPANSION}: within a macro-expansion.
 */
public class OffsetLimitReachedException extends EndOfFileException {

	private static final long serialVersionUID= -4315255081891716385L;

	public static final int ORIGIN_UNKNOWN = 0;
	public static final int ORIGIN_LEXER = 1;
	public static final int ORIGIN_PREPROCESSOR_DIRECTIVE = 2;
	public static final int ORIGIN_INACTIVE_CODE = 3;
	public static final int ORIGIN_MACRO_EXPANSION = 4;
	
	private final IASTCompletionNode node;
	private final IToken finalToken;
	private final int fOrigin;
	
	public OffsetLimitReachedException( IASTCompletionNode node )
	{
		this.node = node;
		finalToken = null;
		fOrigin= ORIGIN_UNKNOWN;
	}
	
	public OffsetLimitReachedException( IToken token ) 
	{
		fOrigin= ORIGIN_UNKNOWN;
		finalToken = token;
		node = null;
	}
	
	public OffsetLimitReachedException(int origin, IToken lastToken) {
		fOrigin= origin;
		finalToken= lastToken;
		node= null;
	}
	
	/**
	 * Returns one of ORIGIN_...
	 */
	public int getOriginator() {
		return fOrigin;
	}
	
	/**
	 * @return Returns the finalToken.
	 */
	public IToken getFinalToken() {
		return finalToken;
	}
	
	/**
	 * @return returns the IASTCompletionNode
	 */
	public IASTCompletionNode getCompletionNode()
	{
		return node;
	}
}
