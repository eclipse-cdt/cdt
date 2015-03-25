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
	
	@Override
	public int getAdjunctIndex() {
		return adjunctIndex;
	}

	@Override
	public int getColumn() {
		return 0;
	}

	@Override
	public int getEndColumn() {
		return 0;
	}

	@Override
	public int getEndLine() {
		return 0;
	}

	@Override
	public int getEndOffset() {
		return token.getEndOffset();
	}

	@Override
	public lpg.lpgjavaruntime.IToken[] getFollowingAdjuncts() {
		return null;
	}

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	public int getLine() {
		return 0;
	}

	@Override
	public lpg.lpgjavaruntime.IToken[] getPrecedingAdjuncts() {
		return null;
	}

	@Override
	public PrsStream getPrsStream() {
		return null;
	}

	@Override
	public int getStartOffset() {
		return token.getOffset();
	}

	@Override
	public int getTokenIndex() {
		return tokenIndex;
	}

	@Override
	@Deprecated
	public String getValue(@SuppressWarnings("unused") char[] arg0) {
		return toString();
	}

	@Override
	public void setAdjunctIndex(int adjunctIndex) {
		this.adjunctIndex = adjunctIndex;
	}

	@Override
	public void setEndOffset(@SuppressWarnings("unused") int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setKind(int kind) {
		this.kind = kind;
	}

	@Override
	public void setStartOffset(@SuppressWarnings("unused") int arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	@Override 
	public String toString() {
		return token.toString();
	}
	
}
