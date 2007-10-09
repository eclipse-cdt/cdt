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


public abstract class Token implements IToken {
	private int fKind;

	int fOffset;
	int fEndOffset;
	
	private IToken fNextGrammarToken;

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
		return fNextGrammarToken;
	}
	
	public abstract char[] getTokenImage();

	
	// for the preprocessor to classify preprocessor tokens
	public void setType(int kind) {
		// mstodo make non-public
		fKind= kind;
	}
	
	// for the preprocessor to chain the tokens
	public void setNext(IToken t) {
		// mstodo make non-public
		fNextGrammarToken= t;
	}


	
	
	public boolean isOperator() {
		// mstodo
		return TokenUtil.isOperator(fKind);
	}

	public char[] getCharImage() {
		// mstodo
		throw new UnsupportedOperationException();
	}

	public String getImage() {
		// mstodo 
		throw new UnsupportedOperationException();
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
