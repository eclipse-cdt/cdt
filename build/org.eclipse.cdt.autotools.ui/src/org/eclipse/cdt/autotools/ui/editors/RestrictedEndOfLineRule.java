/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class RestrictedEndOfLineRule extends EndOfLineRule {

	private List<IRule> rules;
	private int startIndex;
	private int endIndex;
	private String startSequence;
	private String restrictedChars;

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param restrictedChars chars that stop the sequence
	 * @param token the token to be returned on success
	 */
	public RestrictedEndOfLineRule(String startSequence, String restrictedChars, IToken token) {
		this(startSequence, restrictedChars, token, (char) 0);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param restrictedChars chars that stop the sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public RestrictedEndOfLineRule(String startSequence, String restrictedChars, IToken token, char escapeCharacter) {
		this(startSequence, restrictedChars, token, escapeCharacter, false);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token. Alternatively, the
	 * line can also be ended with the end of the file.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param restrictedChars chars that stop the sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 * @param breaksOnEOF indicates whether the end of the file successfully terminates this rule
	 * @since 2.1
	 */
	public RestrictedEndOfLineRule(String startSequence, String restrictedChars, IToken token, char escapeCharacter,
			boolean breaksOnEOF) {
		super(startSequence, token, escapeCharacter, breaksOnEOF);
		this.startSequence = startSequence;
		this.restrictedChars = restrictedChars;
		rules = new ArrayList<>();
		startIndex = 0;
		endIndex = 0;
	}

	public void addRule(SingleLineRule rule) {
		rules.add(rule);
	}

	protected void backupScanner(ICharacterScanner scanner, int position) {
		int count = scanner.getColumn() - position;
		while (count-- > 0)
			scanner.unread();
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		int column = scanner.getColumn();
		// Check if we are at EOF, in which case rules don't hold
		if (column < 0)
			return Token.UNDEFINED;
		if (!resume) {
			startIndex = scanner.getColumn();
			if (super.evaluate(scanner, false) != Token.UNDEFINED) {
				// Outer rule is true for a section.  Now we can
				// set the boundaries for the internal rules.
				// End boundary for internal rules is the start of
				// the end sequence.
				endIndex = scanner.getColumn();
				// Back up scanner to just after start sequence.
				backupScanner(scanner, startIndex + startSequence.length());
			} else
				// Base rule doesn't hold.
				return Token.UNDEFINED;
		}

		// At this point, we want to check for restricted chars in the
		// token.  If we find them, we stop there.

		int start = scanner.getColumn();
		column = start;
		while (column < endIndex) {
			int ch = scanner.read();
			if (ch == ICharacterScanner.EOF || restrictedChars.indexOf(ch) >= 0) {
				scanner.unread();
				return getSuccessToken();
			}
			++column;
		}
		startIndex = 0;
		endIndex = 0;
		return getSuccessToken();
	}
}
