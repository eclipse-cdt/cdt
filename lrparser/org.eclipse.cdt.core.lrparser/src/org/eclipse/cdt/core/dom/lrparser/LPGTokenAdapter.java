/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;


import lpg.lpgjavaruntime.PrsStream;

/**
 * The CPreprocessor from the CDT core returns tokens that 
 * are of the type org.eclipse.cdt.core.parser.IToken,
 * however LPG wants the tokens to be of the type lpg.lpgjavaruntime.IToken.
 * 
 * So these adapter objects are used to wrap the tokens returned
 * by CPreprocessor so that they can be used with LPG.
 * 
 * @author Mike Kucera
 */
public class LPGTokenAdapter implements lpg.lpgjavaruntime.IToken {
	
	/** The token object that is being wrapped */
	private final org.eclipse.cdt.core.parser.IToken token;
	
	
	private int tokenIndex;
	private int adjunctIndex;
	
	private int kind;
	
	public LPGTokenAdapter(org.eclipse.cdt.core.parser.IToken token, int parserKind) {
		this.token = token;
		this.kind = parserKind;
	}

	public org.eclipse.cdt.core.parser.IToken getWrappedToken() {
		return token;
	}
	
	public int getAdjunctIndex() {
		return adjunctIndex;
	}

	public int getColumn() {
		return 0;
	}

	public int getEndColumn() {
		return 0;
	}

	public int getEndLine() {
		return 0;
	}

	public int getEndOffset() {
		return token.getEndOffset();
	}

	public lpg.lpgjavaruntime.IToken[] getFollowingAdjuncts() {
		return null;
	}

	public int getKind() {
		return kind;
	}

	public int getLine() {
		return 0;
	}

	public lpg.lpgjavaruntime.IToken[] getPrecedingAdjuncts() {
		return null;
	}

	public PrsStream getPrsStream() {
		return null;
	}

	public int getStartOffset() {
		return token.getOffset();
	}

	public int getTokenIndex() {
		return tokenIndex;
	}

	@Deprecated
	public String getValue(@SuppressWarnings("unused") char[] arg0) {
		return toString();
	}

	public void setAdjunctIndex(int adjunctIndex) {
		this.adjunctIndex = adjunctIndex;
	}

	public void setEndOffset(@SuppressWarnings("unused") int arg0) {
		throw new UnsupportedOperationException();
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public void setStartOffset(@SuppressWarnings("unused") int arg0) {
		throw new UnsupportedOperationException();

	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	@Override 
	public String toString() {
		return token.toString();
	}
	
}
