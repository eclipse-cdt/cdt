/*******************************************************************************
 * Copyright (c) 2006, 2016 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class AutoconfMacroPartitionRule implements IPredicateRule {
	/**
	 * The default token to be returned on success and if nothing else has been
	 * specified.
	 */
	protected IToken token;

	private IWordDetector generalMacroWordDetector;
	private IWordDetector m4MacroWordDetector;
	protected IWordDetector fDetector;
	protected IWhitespaceDetector fWsDetector = new AutoconfWhitespaceDetector();

	/** The column constraint */
	protected int fColumn = UNDEFINED;

	/** Internal setting for the un-initialized column constraint */
	protected static final int UNDEFINED = -1;

	/** Buffer used for pattern detection */
	private StringBuilder fBuffer = new StringBuilder();

	public AutoconfMacroPartitionRule(IToken inToken) {
		token = inToken;
		generalMacroWordDetector = new AutoconfMacroWordDetector();
		m4MacroWordDetector = new AutoconfM4WordDetector();
	}

	@Override
	public IToken getSuccessToken() {
		return token;
	}

	protected void matchParentheses(ICharacterScanner scanner) {
		boolean finished = false;
		int depth = 1;
		int quoteDepth = 0;
		int c = scanner.read();
		while (!finished && c != ICharacterScanner.EOF) {
			if (c == '[') {
				++quoteDepth;
			} else if (c == ']') {
				--quoteDepth;
				if (quoteDepth < 0)
					finished = true;
			}
			if (quoteDepth == 0) {
				if (c == ')') {
					--depth;
					if (depth <= 0)
						finished = true;
				} else if (c == '(') {
					++depth;
				}
			}
			c = scanner.read();
		}
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		//		if (resume)
		//			return Token.UNDEFINED;
		return evaluate(scanner);
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		fBuffer.setLength(0);

		fBuffer.append((char) c);
		if (c == 'A') {
			c = scanner.read();
			fBuffer.append((char) c);
			if (c != 'C' && c != 'H' && c != 'M') {
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
			fDetector = generalMacroWordDetector;
		} else if (c == 'm') {
			c = scanner.read();
			fBuffer.append((char) c);
			if (c != 4) {
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
			fDetector = m4MacroWordDetector;
		} else {
			unreadBuffer(scanner);
			return Token.UNDEFINED;
		}

		c = scanner.read();
		while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c)) {
			fBuffer.append((char) c);
			c = scanner.read();
		}

		if (c != ICharacterScanner.EOF) {
			if (c == ';' || fWsDetector.isWhitespace((char) c)) {
				// We are done
			} else if (c == '(') {
				matchParentheses(scanner);
			} else {
				scanner.unread();
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
		}

		scanner.unread();
		return token;
	}

	/**
	 * Returns the characters in the buffer to the scanner.
	 *
	 * @param scanner
	 *            the scanner to be used
	 */
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i = fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}

}
