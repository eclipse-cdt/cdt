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
public abstract class Token implements IToken, Cloneable {
	private int fKind;
	private int fOffset;
	private int fEndOffset;
	private IToken fNextToken;
	Object fSource;

	Token(int kind, Object source, int offset, int endOffset) {
		fKind= kind;
		fOffset= offset;
		fEndOffset= endOffset;
		fSource= source;
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

	public void setOffset(int offset, int endOffset) {
		fOffset= offset;
		fEndOffset= endOffset;
	}

	public abstract char[] getCharImage();

	public boolean hasGap(Token t) {
		return fSource == t.fSource && fEndOffset != t.getOffset();
	}

	public String toString() {
		return getImage();
	}
	
	public boolean isOperator() {
		return TokenUtil.isOperator(fKind);
	}

	public String getImage() {
		return new String(getCharImage());
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public char[] getFilename() {
		// mstodo- parser removal
		throw new UnsupportedOperationException();
	}

	public boolean looksLikeExpression() {
		// mstodo- parser removal
		throw new UnsupportedOperationException();
	}

	public boolean canBeAPrefix() {
		// mstodo- parser removal
		throw new UnsupportedOperationException();
	}
	
	public int getLineNumber() {
		// mstodo- parser removal
		throw new UnsupportedOperationException();
	}

	public boolean isPointer() {
		// mstodo- parser removal
		throw new UnsupportedOperationException();
	}
}

class SimpleToken extends Token {
	public SimpleToken(int kind, Object source, int offset, int endOffset) {
		super(kind, source, offset, endOffset);
	}

	public char[] getCharImage() {
		return TokenUtil.getImage(getType());
	}
}

class PlaceHolderToken extends ImageToken {
	private final int fIndex;

	public PlaceHolderToken(int type, int idx, Object source, int offset, int endOffset, char[] name) {
		super(type, source, offset, endOffset, name);
		fIndex= idx;
	}

	public int getIndex() {
		return fIndex;
	}
	
	public String toString() {
		return "[" + fIndex + "]";  //$NON-NLS-1$ //$NON-NLS-2$
	}
}

class DigraphToken extends Token {
	public DigraphToken(int kind, Object source, int offset, int endOffset) {
		super(kind, source, offset, endOffset);
	}

	public char[] getCharImage() {
		return TokenUtil.getDigraphImage(getType());
	}
}

class ImageToken extends Token {
	private char[] fImage;

	public ImageToken(int kind, Object source, int offset, int endOffset, char[] image) {
		super(kind, source, offset, endOffset);
		fImage= image;
	}

	public char[] getCharImage() {
		return fImage; 
	}
}

class SourceImageToken extends Token {
	private char[] fSourceImage;
	private char[] fImage;

	public SourceImageToken(int kind, Object source, int offset, int endOffset, char[] sourceImage) {
		super(kind, source, offset, endOffset);
		fSourceImage= sourceImage;
	}

	public char[] getCharImage() {
		if (fImage == null) {
			final int length= getLength();
			fImage= new char[length];
			System.arraycopy(fSourceImage, getOffset(), fImage, 0, length);
		}
		return fImage; 
	}

	public void setOffset(int offset, int endOffset) {
		getCharImage();
		super.setOffset(offset, endOffset);
	}
}


