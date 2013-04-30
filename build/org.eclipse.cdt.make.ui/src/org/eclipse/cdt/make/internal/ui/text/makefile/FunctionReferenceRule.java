/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class FunctionReferenceRule extends WordRule {
	/** Buffer used for pattern detection. */
	private StringBuffer fBuffer= new StringBuffer();

	@SuppressWarnings("nls")
	private final static String[] functions = {
		"subst", "patsubst", "strip", "findstring",
		"filter", "filter-out", "sort",
		"word", "words", "wordlist", "firstword", "lastword",
		"dir", "notdir",
		"suffix", "basename", "addsuffix", "addprefix",
		"join", "wildcard", "realpath", "abspath",
		"if", "or", "and", "foreach",
		"call", "value", "eval", "origin", "flavor",
		"shell", "error", "warning", "info",
	};

	static class TagDetector implements IWordDetector {
		private boolean isClosedBracket = false;
		private int bracketNesting = 0;
		@Override
		public boolean isWordStart(char c) {
			isClosedBracket = c == ')';
			return isClosedBracket || c == '$';
		}
		@Override
		public boolean isWordPart(char c) {
			return !isClosedBracket && (c == '(' || Character.isJavaIdentifierPart(c));
		}
	}

	public FunctionReferenceRule(IToken token) {
		super(new TagDetector());
		for (String f : functions) {
			addWord("$(" + f, token); //$NON-NLS-1$
		}
		addWord(")", token); //$NON-NLS-1$
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (c == ')') {
			if (((TagDetector)fDetector).bracketNesting > 0) {
				((TagDetector)fDetector).bracketNesting--;
				return (IToken)fWords.get(")"); //$NON-NLS-1$
			}
			return fDefaultToken;
		}

		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

				fBuffer.setLength(0);
				do {
					fBuffer.append((char) c);
					c= scanner.read();
				} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
				scanner.unread();

				String buffer= fBuffer.toString();

				IToken token= (IToken)fWords.get(buffer);

				if (token != null) {
					((TagDetector)fDetector).bracketNesting++;
					return token;
				}

				if (fDefaultToken.isUndefined())
					unreadBuffer(scanner);

				return fDefaultToken;
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	@Override
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i= fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}
}
