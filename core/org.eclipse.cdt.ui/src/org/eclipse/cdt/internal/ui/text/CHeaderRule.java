/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Recognizes headers specified using angle brackets (e.g. #include <stdio.h>).
 */
public class CHeaderRule implements IRule {

	/** Style token. */
	private IToken fToken;

	/**
	 * Creates a new CHeaderRule.
	 * 
	 * @param token
	 *            Style token.
	 */
	public CHeaderRule(IToken token) {
		fToken = token;
	}

	/**
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int current = scanner.read();
		int lookAhead = 1;
		int contentLength = 0;

		if (current == '<' || current == '"') {
			int expected = current == '<' ? '>' : current;
			do {
				current = scanner.read();
				lookAhead++;
				if (current == expected) {
					if (contentLength < 1) {
						break;
					}
					
					// Rewind and check for an #include.
					seek(scanner, -lookAhead);
					if (!followsIncludeDirective(scanner)) {
						return Token.UNDEFINED;
					}
					
					seek(scanner, lookAhead);
					return fToken;
				}
				contentLength++;
			} while (current != ICharacterScanner.EOF && current != '\n');
		}

		seek(scanner, -lookAhead);
		return Token.UNDEFINED;
	}

	/**
	 * Repositions the scanner.
	 * 
	 * @param scanner
	 *            Scanner.
	 * @param characters
	 *            Number of characters to move ahead (if positive) or behind (if
	 *            negative).
	 */
	private void seek(ICharacterScanner scanner, int characters) {
		if (characters < 0) {
			while (characters < 0) {
				scanner.unread();
				characters++;
			}
		} else {
			while (characters > 0) {
				scanner.read();
				characters--;
			}
		}
	}

	/**
	 * Returns true if the previous contents of the scanner is an #include
	 * directive.
	 * 
	 * @param scanner
	 *            Scanner.
	 * @return true if the previous contents of the scanner is an #include
	 *         directive.
	 */
	private boolean followsIncludeDirective(ICharacterScanner scanner) {
		int lookBehind = 0;
		boolean result = false;

		int current = unread(scanner);
		lookBehind++;
		if (Character.isWhitespace((char) current)) {
			do {
				current = unread(scanner);
				lookBehind++;
			} while (Character.isWhitespace((char) current));
		}
		scanner.read();
		--lookBehind;
		if (current == 'e' && searchBackwards(scanner, "include") ||  //$NON-NLS-1$
				current == 't' && searchBackwards(scanner, "include_next")) { //$NON-NLS-1$
			result = true;
		}

		seek(scanner, lookBehind);
		return result;
	}

	/**
	 * Returns true if the given String was the last String read from the
	 * scanner.
	 * 
	 * @param scanner
	 *            Scanner.
	 * @param string
	 *            Expected String.
	 * @return true if the given String was the last String read from the
	 *         scanner.
	 */
	private boolean searchBackwards(ICharacterScanner scanner, String string) {
		int offset = 0;
		for (int i = string.length() - 1; i >= 0; i--) {
			offset++;
			if (string.charAt(i) != unread(scanner)) {
				seek(scanner, offset);
				return false;
			}
		}
		seek(scanner, offset);
		return true;
	}

	/**
	 * Unreads a single character from the scanner.
	 * 
	 * @param scanner
	 *            Scanner.
	 * @return the last character read from the scanner.
	 */
	private int unread(ICharacterScanner scanner) {
		scanner.unread();
		int character = scanner.read();
		scanner.unread();
		return character;
	}

}
