/**********************************************************************
 * Copyright (c) 2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Implementation of <code>IRule</code> for C/C++ preprocessor scanning.
 * It is capable of detecting a pattern which begins with 0 or more whitespaces 
 * at the beginning of the string, then '#' sign, then 0 or more whitespaces
 * again, and then directive itself.
 */
public class PreprocessorRule extends WordRule implements IRule {

	private StringBuffer fBuffer = new StringBuffer();

	/**
	 * Creates a rule which, with the help of a word detector, will return the token
	 * associated with the detected word. If no token has been associated, the scanner 
	 * will be rolled back and an undefined token will be returned in order to allow 
	 * any subsequent rules to analyze the characters.
	 *
	 * @param detector the word detector to be used by this rule, may not be <code>null</code>
	 *
	 * @see #addWord
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
	 * @see #addWord
	 */
	public PreprocessorRule(IWordDetector detector, IToken defaultToken) {
		super(detector, defaultToken);
	}

	/*
	 * @see IRule#evaluate
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int c;
		int nCharsToRollback = 0;
		boolean hashSignDetected = false;

		if (scanner.getColumn() > 0)
			return Token.UNDEFINED;

		do {
			c = scanner.read();
			nCharsToRollback++;
		} while (Character.isWhitespace((char) c));
		
		
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

			do {
				c = scanner.read();
			} while (Character.isWhitespace((char) c));

			fBuffer.setLength(0);

			do {
				fBuffer.append((char) c);
				c = scanner.read();
			} while (Character.isJavaIdentifierPart((char) c));

			scanner.unread();

			IToken token = (IToken) fWords.get("#" + fBuffer.toString()); //$NON-NLS-1$
			if (token != null)
				return token;

			return fDefaultToken;

		} else { // Doesn't start with '#', roll back scanner

			for (int i = 0; i < nCharsToRollback; i++) {
				scanner.unread();
			}
		}

		return Token.UNDEFINED;
	}
}