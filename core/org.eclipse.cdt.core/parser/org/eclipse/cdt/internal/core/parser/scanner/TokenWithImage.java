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

class TokenWithImage extends Token {

	final private Lexer fLexer;
	final private int fImageLength;
	private char[] fImage;

	public TokenWithImage(int kind, Lexer source, int offset, int endOffset, int imageLength) {
		super(kind, offset, endOffset);
		fLexer= source;
		fImageLength= imageLength;
	}

	public TokenWithImage(int kind, int offset, int endOffset, char[] image) {
		super(kind, offset, endOffset);
		fLexer= null;
		fImageLength= 0;
		fImage= image;
	}

	public char[] getTokenImage() {
		if (fImage == null) {
			fImage= fLexer.getTokenImage(fOffset, fEndOffset, fImageLength);
		}
		return fImage; 
	}
}
