/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

class TokenList {
	private Token fFirst;
	private Token fLast;

	final Token removeFirst() {
		final Token first = fFirst;
		if (first == fLast) {
			fFirst = null;
			fLast = null;
			return first;
		}
		fFirst = (Token) first.getNext();
		return first;
	}

	public final void append(Token t) {
		if (fFirst == null) {
			fFirst = t;
			fLast = t;
		} else {
			fLast.setNext(t);
			fLast = t;
		}
		t.setNext(null);
	}

	public final void appendAll(TokenList tl) {
		final Token t = tl.first();
		if (t != null) {
			if (fFirst == null) {
				fFirst = tl.fFirst;
			} else {
				fLast.setNext(tl.fFirst);
			}
			fLast = tl.fLast;
		}
		tl.fFirst = null;
		tl.fLast = null;
	}

	public final void appendAllButLast(TokenList tl) {
		Token t = tl.first();
		if (t != null) {
			for (Token n = (Token) t.getNext(); n != null; t = n, n = (Token) n.getNext()) {
				append(t);
			}
		}
	}

	public final void prepend(Token t) {
		final Token first = t;
		if (first != null) {
			final Token last = t;
			last.setNext(fFirst);
			fFirst = first;
			if (fLast == null) {
				fLast = last;
			}
		}
	}

	public final void prepend(TokenList prepend) {
		final Token first = prepend.fFirst;
		if (first != null) {
			final Token last = prepend.fLast;
			last.setNext(fFirst);
			fFirst = first;
			if (fLast == null) {
				fLast = last;
			}
		}
	}

	public final TokenList cloneTokens() {
		TokenList result = new TokenList();
		for (Token t = fFirst; t != null; t = (Token) t.getNext()) {
			if (t.getType() != CPreprocessor.tSCOPE_MARKER) {
				result.append(t.clone());
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
			Token t = fFirst;
			if (t != null) {
				t = (Token) t.getNext();
				fFirst = t;
				if (t == null) {
					fLast = null;
				}
			}
		} else {
			final Token r = (Token) l.getNext();
			if (r != null) {
				l.setNext(r.getNext());
				if (r == fLast) {
					fLast = l;
				}
			}
		}
	}

	void cutAfter(Token l) {
		if (l == null) {
			fFirst = null;
			fLast = null;
		} else {
			l.setNext(null);
			fLast = l;
		}
	}

	public void clear() {
		fFirst = null;
		fLast = null;
	}

	public boolean isEmpty() {
		return fFirst == null;
	}
}
