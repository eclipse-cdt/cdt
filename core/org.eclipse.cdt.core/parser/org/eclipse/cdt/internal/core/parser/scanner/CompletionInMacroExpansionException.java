/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;

/**
 * Thrown when content assist is used within the parameter list of a macro expansion.
 * It transports the token list of the current parameter for further use in attempting
 * a completion.
 * @since 5.0
 */
public class CompletionInMacroExpansionException extends OffsetLimitReachedException {

	private TokenList fParameterTokens;

	public CompletionInMacroExpansionException(int origin, IToken lastToken, TokenList paramTokens) {
		super(origin, lastToken);
		fParameterTokens = paramTokens;
	}

	public TokenList getParameterTokens() {
		return fParameterTokens;
	}
}
