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

/**
 * Represents tokens found by the lexer. The preprocessor reuses the tokens and passes
 * them on to the parsers.
 * @since 5.0
 */
public abstract class Token implements IToken {
	private int fKind;
	private int fOffset;
	private int fEndOffset;
	
	private IToken fNextToken;

	Token(int kind, int offset, int endOffset) {
		fKind= kind;
		fOffset= offset;
		fEndOffset= endOffset;
	}

	public int getType() {
		return fKind;
	}

	public int getOffset() {
		return fOffset;
	}

	public int getEndOffset() {
		return fEndOffset;
	}

	public int getLength() {
		return fEndOffset-fOffset;
	}

	public IToken getNext() {
		return fNextToken;
	}

	
	public void setType(int kind) {
		fKind= kind;
	}

	public void setNext(IToken t) {
		fNextToken= t;
	}

	public abstract char[] getCharImage();

		
	public boolean isOperator() {
		return TokenUtil.isOperator(fKind);
	}

	public String getImage() {
		return new String(getCharImage());
	}


	public char[] getFilename() {
		// mstodo
		throw new UnsupportedOperationException();
	}

	public boolean looksLikeExpression() {
		// mstodo
		throw new UnsupportedOperationException();
	}

	public boolean canBeAPrefix() {
		// mstodo
		throw new UnsupportedOperationException();
	}
	
	public int getLineNumber() {
		// mstodo
		throw new UnsupportedOperationException();
	}

	public boolean isPointer() {
		// mstodo
		throw new UnsupportedOperationException();
	}
}

class SimpleToken extends Token {
	public SimpleToken(int kind, int offset, int endOffset) {
		super(kind, offset, endOffset);
	}

	public char[] getCharImage() {
		return TokenUtil.getImage(getType());
	}
}

class DigraphToken extends Token {
	public DigraphToken(int kind, int offset, int endOffset) {
		super(kind, offset, endOffset);
	}

	public char[] getCharImage() {
		return TokenUtil.getDigraphImage(getType());
	}
}

class ImageToken extends Token {
	private char[] fImage;

	public ImageToken(int kind, int offset, int endOffset, char[] image) {
		super(kind, offset, endOffset);
		fImage= image;
	}

	public char[] getCharImage() {
		return fImage; 
	}
}

class SourceImageToken extends Token {

	private char[] fSource;
	private char[] fImage;

	public SourceImageToken(int kind, int offset, int endOffset, char[] source) {
		super(kind, offset, endOffset);
		fSource= source;
	}

	public char[] getCharImage() {
		if (fImage == null) {
			final int length= getLength();
			fImage= new char[length];
			System.arraycopy(fSource, getOffset(), fImage, 0, length);
		}
		return fImage; 
	}
}


