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

class TokenList {
	private Token fFirst;
	private Token fLast;

	final Token removeFirst() {
		final Token first= fFirst;
		if (first == fLast) {
			fFirst= fLast= null;
			return first;
		}
		fFirst= (Token) first.getNext();
		return first;
	}

	public final void append(Token t) {
		if (fFirst == null) {
			fFirst= fLast= t;
		}
		else {
			fLast.setNext(t);
			fLast= t;
		}
		t.setNext(null);
	}
	
	public final void appendAll(TokenList tl) {
		final Token t= tl.first();
		if (t != null) {
			if (fFirst == null) {
				fFirst= tl.fFirst;
			}
			else {
				fLast.setNext(tl.fFirst);
			}
			fLast= tl.fLast;
		}
		tl.fFirst= tl.fLast= null;
	}

	public final void appendAllButLast(TokenList tl) {
		Token t= tl.first();
		if (t != null) {
			for (Token n= (Token) t.getNext(); n != null; t=n, n= (Token) n.getNext()) {
				append(t);
			}
		}
	}
	
	public final void prepend(Token t) {
		final Token first= t;
		if (first != null) {
			final Token last= t;
			last.setNext(fFirst);
			fFirst= first;
			if (fLast == null) {
				fLast= last;
			}
		}
	}

	public final void prepend(TokenList prepend) {
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
	
	public final TokenList cloneTokens() {
		TokenList result= new TokenList();
		for (Token t= fFirst; t != null; t= (Token) t.getNext()) {
			if (t.getType() != CPreprocessor.tSCOPE_MARKER) {
				result.append((Token) t.clone());
			}
		}
		return result;
	}

	public final Token first() {
		return fFirst;
	}

	public final Token last() {
		return fLast;
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

	public void clear() {
		fFirst= fLast= null;
	}

	public boolean isEmpty() {
		return fFirst==null;
	}
}
