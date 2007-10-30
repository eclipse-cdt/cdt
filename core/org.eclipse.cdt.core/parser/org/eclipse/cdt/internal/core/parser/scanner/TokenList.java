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

class TokenList {
	private Token fFirst;
	private Token fLast;

	final Token removeFirst() {
		final Token first= fFirst;
		if (first == fLast) {
			fFirst= fLast= null;
			return first;
		}
		else {
			fFirst= (Token) first.getNext();
			return first;
		}
	}

	final public void append(Token t) {
		if (fFirst == null) {
			fFirst= fLast= t;
		}
		else {
			fLast.setNext(t);
			fLast= t;
		}
		t.setNext(null);
	}
	
	final public void prepend(TokenList prepend) {
		final Token first= prepend.fFirst;
		if (first != null) {
			final Token last= prepend.fLast;
			last.setNext(fFirst);
			fFirst= first;
			if (fLast == null) {
				fLast= last;
			}
		}
	}
	
	final public TokenList cloneTokens() {
		TokenList result= new TokenList();
		for (Token t= fFirst; t != null; t= (Token) t.getNext()) {
			if (t.getType() != CPreprocessor.tSCOPE_MARKER) {
				result.append((Token) t.clone());
			}
		}
		return result;
	}

	final public Token first() {
		return fFirst;
	}

	final void removeBehind(Token l) {
		if (l == null) {
			Token t= fFirst;
			if (t != null) {
				t= (Token) t.getNext();
				fFirst= t;
				if (t == null) {
					fLast= null;
				}
			}
		}
		else {
			final Token r= (Token) l.getNext();
			if (r != null) {
				l.setNext(r.getNext());
				if (r == fLast) {
					fLast= l;
				}
			}
		}
	}

	void cutAfter(Token l) {
		if (l == null) {
			fFirst= fLast= null;
		}
		else {
			l.setNext(null);
			fLast= l;
		}
	}
}
