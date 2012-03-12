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

public class InlineDataRule implements IRule {
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

	private String fStartingSequence = "<<";

	public InlineDataRule(IToken inToken) {
		token = inToken;
	}

	// Confirm an EOL delimeter after already matching first delimeter character.
	protected boolean confirmDelimeter(ICharacterScanner scanner, char[] delimeter) {
		int c = scanner.read();
		StringBuffer buffer = new StringBuffer();
		buffer.append((char)c);
		for (int i = 1; i < delimeter.length; ++i) {
			if (c == delimeter[i]) {
				c = scanner.read();
				buffer.append((char)c);
			} else {
				for (int j = buffer.length() - 1; j >= 0; j--)
					scanner.unread();
				return false;
			}
		}
		scanner.unread();
		return true;
	}
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		fBuffer.setLength(0);

		// Looking for <<WORD or <<-WORD or <<'WORD' at end of line.
		for (int i = 0; i < fStartingSequence.length(); i++) {
			fBuffer.append((char) c);
			if (fStartingSequence.charAt(i) != c) {
				unreadBuffer(scanner);
				return Token.UNDEFINED;
			}
			c = scanner.read();
		}

		char[][] lineDelimeters = scanner.getLegalLineDelimiters();
		StringBuffer endMarkerBuffer = new StringBuffer();
		if (c == '-') {
			fBuffer.append((char)c);
			c = scanner.read();
		} else if (c == '\'') {
			fBuffer.append((char)c);
			c = scanner.read();
		}
		
		while (c != ICharacterScanner.EOF &&
				Character.isJavaIdentifierPart((char)c) &&
				c != '\'') {
			fBuffer.append((char)c);
			endMarkerBuffer.append((char)c);
			c = scanner.read();
		}
		
		if (c == '\'') {
			fBuffer.append((char)c);
			c = scanner.read();
		}
		
		if (endMarkerBuffer.length() == 0) {
			unreadBuffer(scanner);
			return Token.UNDEFINED;
		}
			
		// At this point we read until we find id by itself on its own line
		boolean eol = false;
		boolean finished = false;
		boolean foundMarker = false;
		String endMarker = endMarkerBuffer.toString();
		while (!finished && c != ICharacterScanner.EOF) {
			for (int i = 0; i < lineDelimeters.length; ++i) {
				if (c == lineDelimeters[i][0]) {
					if (confirmDelimeter(scanner, lineDelimeters[i])) {
						c = scanner.read();
						eol = true;
						break;
					}
				}
			}
			if (eol) {
				eol = false;
				if (foundMarker) {
					// We get here if we have found the marker by itself
					// on the line
					finished = true;
				} else {
					foundMarker = false;
					int j = 0;
					while (j < endMarker.length()) {
						if (c == endMarker.charAt(j)) {
							c = scanner.read();
							++j;
						} else {
							break;
						}
					}
					if (j == endMarker.length()) {
						foundMarker = true;
					}
				}
			} else {
				// otherwise ignore all other characters
				eol = false;
				foundMarker = false;
				c = scanner.read();
			}
		}

		// unread last character
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
