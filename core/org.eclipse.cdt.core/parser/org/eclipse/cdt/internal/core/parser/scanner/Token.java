/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
public class Token implements IToken, Cloneable {
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

	final public int getType() {
		return fKind;
	}

	final public int getOffset() {
		return fOffset;
	}

	final public int getEndOffset() {
		return fEndOffset;
	}

	final public int getLength() {
		return fEndOffset-fOffset;
	}

	final public IToken getNext() {
		return fNextToken;
	}

	
	final public void setType(int kind) {
		fKind= kind;
	}

	final public void setNext(IToken t) {
		fNextToken= t;
	}

	public void setOffset(int offset, int endOffset) {
		fOffset= offset;
		fEndOffset= endOffset;
	}

	public void shiftOffset(int shift) {
		fOffset+= shift;
		fEndOffset+= shift;
	}

	public char[] getCharImage() {
		return TokenUtil.getImage(getType());
	}

	@Override
	public String toString() {
		return getImage();
	}
	
	final public boolean isOperator() {
		return TokenUtil.isOperator(fKind);
	}

	public String getImage() {
		return new String(getCharImage());
	}

	@Override
	final public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
