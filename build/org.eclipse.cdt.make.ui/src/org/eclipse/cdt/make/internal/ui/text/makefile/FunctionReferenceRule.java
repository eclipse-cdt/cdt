/*******************************************************************************
 * Copyright (c) 2013, 2016 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.cdt.make.internal.core.makefile.gnu.GNUMakefileConstants;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class FunctionReferenceRule extends WordRule {
	/** Buffer used for pattern detection. */
	private StringBuilder fBuffer = new StringBuilder();
	private String startSeq;
	private String endSeq;

	@SuppressWarnings("nls")
	private final static String[] functions = { "subst", "patsubst", "strip", "findstring", "filter", "filter-out",
			"sort", "word", "words", "wordlist", "firstword", "lastword", "dir", "notdir", "suffix", "basename",
			"addsuffix", "addprefix", "join", "wildcard", "realpath", "abspath", "if", "or", "and", "foreach", "call",
			"value", "eval", "origin", "flavor", "shell", "error", "warning", "info", };

	private static class TagDetector implements IWordDetector {
		private char openBracket;
		private char closedBracket;
		private boolean isClosedBracket = false;
		private int bracketNesting = 0;

		public TagDetector(String endSeq) {
			if (endSeq.length() > 0 && endSeq.charAt(0) == '}') {
				openBracket = '{';
				closedBracket = '}';
			} else {
				openBracket = '(';
				closedBracket = ')';
			}
		}

		@Override
		public boolean isWordStart(char c) {
			isClosedBracket = c == closedBracket;
			return isClosedBracket || c == '$';
		}

		@Override
		public boolean isWordPart(char c) {
			return !isClosedBracket && (c == '$' || c == openBracket || Character.isJavaIdentifierPart(c) || c == '-');
		}

		public boolean isBracket(char c) {
			return "(){}".contains(Character.toString(c)); //$NON-NLS-1$
		}
	}

	public FunctionReferenceRule(IToken token, String startSeq, String endSeq) {
		super(new TagDetector(endSeq));
		this.startSeq = startSeq;
		this.endSeq = endSeq;
		for (String f : functions) {
			addWord(startSeq + f, token);
			addWord('$' + startSeq + f, token);
		}
		addWord(endSeq, token);
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		TagDetector tagDetector = (TagDetector) fDetector;
		int c = scanner.read();
		if (c == tagDetector.closedBracket) {
			if (tagDetector.bracketNesting > 0) {
				tagDetector.bracketNesting--;
				return fWords.get(endSeq);
			}
			return fDefaultToken;
		}

		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

				fBuffer.setLength(0);
				do {
					fBuffer.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
				scanner.unread();

				String buffer = fBuffer.toString();

				IToken token = fWords.get(buffer);

				if (token != null) {
					if (buffer.equals(startSeq + GNUMakefileConstants.FUNCTION_CALL)
							|| buffer.equals('$' + startSeq + GNUMakefileConstants.FUNCTION_CALL)) {
						if ((char) scanner.read() == ' ') {
							do {
								c = scanner.read();
							} while (((TagDetector) fDetector).isBracket((char) c) || fDetector.isWordPart((char) c));
						}
						scanner.unread();
					}
					((TagDetector) fDetector).bracketNesting++;
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
		for (int i = fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}
}
