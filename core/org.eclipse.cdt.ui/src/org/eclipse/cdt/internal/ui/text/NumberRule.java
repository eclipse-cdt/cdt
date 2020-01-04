/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc. - bug fixes
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Recognizes integer and float numbers.
 *
 * @author P.Tomaszewski
 */
public class NumberRule implements IRule {
	/** Style token. */
	private IToken token;

	/**
	 * Creates new number rule.
	 * @param token Style token.
	 */
	public NumberRule(IToken token) {
		super();
		this.token = token;
	}

	/**
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int startCh = scanner.read();
		int ch;
		int unreadCount = 1;

		if (isNumberStart(startCh)) {
			ch = startCh;
			if (startCh == '-' || startCh == '+') {
				ch = scanner.read();
				++unreadCount;
			}
			if (ch == '0') {
				int xCh = scanner.read();
				if (xCh == 'x' || xCh == 'X') {
					// hexnumber starting with [+-]?0[xX]
					do {
						ch = scanner.read();
					} while (isHexDigitOrSeparator(ch));
					scanner.unread();
					return token;
				} else if (xCh == 'b' || xCh == 'B') {
					// binary number starting with [+-]?0[bB]
					do {
						ch = scanner.read();
					} while (isBinDigitOrSeparator(ch));
					scanner.unread();
					return token;
				}
				scanner.unread();
				// assert ch == '0';
			} else if (ch == '.') {
				ch = scanner.read();
				++unreadCount;
			}
			if (Character.isDigit((char) ch)) {
				// need at least one digit
				do {
					ch = scanner.read();
				} while (isDecDigitOrSeparator(ch));
				if (ch == '.' && startCh != '.') {
					// fraction
					do {
						ch = scanner.read();
					} while (isDecDigitOrSeparator(ch));
				}
				if (ch == 'e' || ch == 'E') {
					// exponent
					ch = scanner.read();
					if (ch == '-' || ch == '+' || Character.isDigit((char) ch)) {
						do {
							ch = scanner.read();
						} while (isDecDigitOrSeparator(ch));
					}
				}
				scanner.unread();
				return token;
			}
		}
		do {
			scanner.unread();
		} while (--unreadCount > 0);
		return Token.UNDEFINED;
	}

	/**
	 * Checks if start of number.
	 * @param ch Char to check.
	 * @return <b>true</b> if Number.
	 */
	private boolean isNumberStart(int ch) {
		return ch == '-' || ch == '+' || ch == '.' || Character.isDigit((char) ch);
	}

	/**
	 * Checks if part of binary number;
	 * @param ch Char to check.
	 * @return <b>true</b>
	 */
	private boolean isBinDigitOrSeparator(int ch) {
		return ch == '0' || ch == '1' || (ch == '\'');
	}

	/**
	 * Checks if part of decimal number;
	 * @param ch Char to check.
	 * @return <b>true</b>
	 */
	private boolean isDecDigitOrSeparator(int ch) {
		return Character.isDigit((char) ch) || (ch == '\'');
	}

	/**
	 * Checks if part of hex number;
	 * @param ch Char to check.
	 * @return <b>true</b>
	 */
	private boolean isHexDigitOrSeparator(int ch) {
		return Character.isDigit((char) ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') || (ch == '\'');
	}
}
