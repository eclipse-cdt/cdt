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
 * Context used to run the preprocessor while swallowing all tokens.
 * Needed to process macro-files as specified by the -imacro compiler option of gcc.
 * @since 5.0
 */
public class ScannerContextMacroFile extends ScannerContextFile {
	private final CPreprocessor fCpp;
	private boolean fSkippingTokens= false;

	public ScannerContextMacroFile(CPreprocessor cpp, ILocationCtx ctx, ScannerContext parent, Lexer lexer) {
		super(ctx, parent, lexer);
		fCpp= cpp;
	}

	public Token nextPPToken() throws OffsetLimitReachedException {
		if (fSkippingTokens) {
			final Token t= super.nextPPToken();
			if (t.getType() == Lexer.tEND_OF_INPUT) {
				fSkippingTokens= false;
			}
			return t;
		}
		
		// use preprocessor to read tokens off this context, until this context is done.
		fSkippingTokens= true;
		Token t;
		do {
			t= fCpp.fetchTokenFromPreprocessor();
		} while (fSkippingTokens);
		return t;
	}
}
