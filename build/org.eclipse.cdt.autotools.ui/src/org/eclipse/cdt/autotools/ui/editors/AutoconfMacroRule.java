/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class AutoconfMacroRule implements IRule {
	/**
	 * The default token to be returned on success and if nothing else has been
	 * specified.
	 */
	protected IToken token;

	protected IWordDetector fDetector;
	protected IWhitespaceDetector fWsDetector = new AutoconfWhitespaceDetector();

	/** The column constraint */
	protected int fColumn = UNDEFINED;

	/** Internal setting for the un-initialized column constraint */
	protected static final int UNDEFINED = -1;

	/** Buffer used for pattern detection */
	private StringBuffer fBuffer = new StringBuffer();

	private String fStartingSequence;

	public AutoconfMacroRule(String startingSequence,
			IWordDetector detector, IToken inToken) {
		token = inToken;
		fDetector = detector;
		fStartingSequence = startingSequence;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		fBuffer.setLength(0);

		for (int i = 0; i < fStartingSequence.length(); i++) {
			fBuffer.append((char) c);
			if (fStartingSequence.charAt(i) != c) {
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
			c = scanner.read();
		}

		while (c != ICharacterScanner.EOF
				&& fDetector.isWordPart((char) c)) {
			fBuffer.append((char) c);
			c = scanner.read();
		}

		if (c != ICharacterScanner.EOF && c != '(' && c != ';'
			&& !fWsDetector.isWhitespace((char)c)) {
			unreadBuffer(scanner);
			return Token.UNDEFINED;
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
