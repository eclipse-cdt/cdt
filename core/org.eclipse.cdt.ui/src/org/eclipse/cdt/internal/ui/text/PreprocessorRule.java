/*******************************************************************************
 * Copyright (c) 2003, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Implementation of <code>IRule</code> for C/C++ preprocessor scanning.
 * It is capable of detecting a pattern which begins with 0 or more whitespaces 
 * at the beginning of the string, then '#' sign, then 0 or more whitespaces
 * again, and then directive itself.
 */
public class PreprocessorRule extends WordRule {

	private StringBuffer fBuffer = new StringBuffer();
	private IToken fMalformedToken;

	/**
	 * Creates a rule which, with the help of a word detector, will return the token
	 * associated with the detected word. If no token has been associated, the scanner 
	 * will be rolled back and an undefined token will be returned in order to allow 
	 * any subsequent rules to analyze the characters.
	 *
	 * @param detector the word detector to be used by this rule, may not be <code>null</code>
	 *
	 * @see WordRule#addWord
	 */
	public PreprocessorRule(IWordDetector detector) {
		this(detector, Token.UNDEFINED);
	}

	/**
	 * Creates a rule which, with the help of an word detector, will return the token
	 * associated with the detected word. If no token has been associated, the
	 * specified default token will be returned.
	 *
	 * @param detector the word detector to be used by this rule, may not be <code>null</code>
	 * @param defaultToken the default token to be returned on success 
	 *  if nothing else is specified, may not be <code>null</code>
	 *
	 * @see WordRule#addWord
	 */
	public PreprocessorRule(IWordDetector detector, IToken defaultToken) {
		super(detector, defaultToken);
	}

	/**
	 * Creates a rule which, with the help of an word detector, will return the token
	 * associated with the detected word. If no token has been associated, the
	 * specified default token will be returned.
	 *
	 * @param detector the word detector to be used by this rule, may not be <code>null</code>
	 * @param defaultToken the default token to be returned on success 
	 *  if nothing else is specified, may not be <code>null</code>
	 * @param malformedToken  the token to be returned if the directive is malformed
	 * 
	 * @see WordRule#addWord
	 */
	public PreprocessorRule(IWordDetector detector, IToken defaultToken, IToken malformedToken) {
		super(detector, defaultToken);
		fMalformedToken= malformedToken;
	}

	/*
	 * @see org.eclipse.jface.text.rules.WordRule#addWord(java.lang.String, org.eclipse.jface.text.rules.IToken)
	 */
	@Override
	public void addWord(String word, IToken token) {
		if (word.charAt(0) == '#') {
			word= word.substring(1);
		}
		super.addWord(word, token);
	}

	/*
	 * @see IRule#evaluate
	 */
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c;
		int nCharsToRollback = 0;
		boolean hashSignDetected = false;

		do {
			c = scanner.read();
			nCharsToRollback++;
		} while (c == ' ' || c == '\t');
		
		// Di- and trigraph support
		if (c == '#') {
			hashSignDetected = true;
		} else if (c == '%') {
			c = scanner.read();
			nCharsToRollback++;
			if (c == ':') {
				hashSignDetected = true;
			}
		} else if (c == '?') {
			c = scanner.read();
			nCharsToRollback++;
			if (c == '?') {
				c = scanner.read();
				nCharsToRollback++;
				if (c == '=') {
					hashSignDetected = true;
				}
			}
		}

		if (hashSignDetected) {

			fBuffer.setLength(0);
			c = scanner.read();
			if (c == '#') {
				// ## operator
				fBuffer.append((char) c);
			} else {
				while (c == ' ' || c == '\t') {
					c = scanner.read();
				}
				if (fDetector.isWordStart((char) c)) {
					do {
						fBuffer.append((char) c);
						c = scanner.read();
					} while (fDetector.isWordPart((char) c));
				}
				scanner.unread();
			}
			IToken token = (IToken) fWords.get(fBuffer.toString());
			if (token != null)
				return token;
			
			if (fMalformedToken != null) {
				do {
					c = scanner.read();
				} while (c != ICharacterScanner.EOF);
				return fMalformedToken;
			}

			return fDefaultToken;

		}
		// Doesn't start with '#', roll back scanner
		
		for (int i = 0; i < nCharsToRollback; i++) {
			scanner.unread();
		}

		return Token.UNDEFINED;
	}
}
