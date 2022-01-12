/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class RecursiveSingleLineRule extends SingleLineRule {

	private List<IRule> rules;
	private int evalIndex;
	private int startIndex;
	private int endIndex;
	private int endBoundary;
	private String startSequence;
	private String endSequence;

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 */
	public RecursiveSingleLineRule(String startSequence, String endSequence, IToken token) {
		this(startSequence, endSequence, token, (char) 0);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 */
	public RecursiveSingleLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter) {
		this(startSequence, endSequence, token, escapeCharacter, false);
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token. Alternatively, the
	 * line can also be ended with the end of the file.
	 * Any character which follows the given escape character
	 * will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 * @param breaksOnEOF indicates whether the end of the file successfully terminates this rule
	 * @since 2.1
	 */
	public RecursiveSingleLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter,
			boolean breaksOnEOF) {
		super(startSequence, endSequence, token, escapeCharacter, breaksOnEOF);
		this.startSequence = startSequence;
		this.endSequence = endSequence;
		rules = new ArrayList<>();
		startIndex = 0;
		endIndex = 0;
	}

	/**
	 * Creates a rule for the given starting and ending sequence
	 * which, if detected, will return the specified token. Alternatively, the
	 * line can also be ended with the end of the file.
	 * Any character which follows the given escape character
	 * will be ignored. In addition, an escape character immediately before an
	 * end of line can be set to continue the line.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence
	 * @param token the token to be returned on success
	 * @param escapeCharacter the escape character
	 * @param breaksOnEOF indicates whether the end of the file successfully terminates this rule
	 * @param escapeContinuesLine indicates whether the specified escape character is used for line
	 *        continuation, so that an end of line immediately after the escape character does not
	 *        terminate the line, even if <code>breakOnEOL</code> is true
	 * @since 3.0
	 */
	public RecursiveSingleLineRule(String startSequence, String endSequence, IToken token, char escapeCharacter,
			boolean breaksOnEOF, boolean escapeContinuesLine) {
		super(startSequence, endSequence, token, escapeCharacter, breaksOnEOF, escapeContinuesLine);
		this.startSequence = startSequence;
		this.endSequence = endSequence;
		rules = new ArrayList<>();
		startIndex = 0;
		endIndex = 0;
	}

	public void addRule(SingleLineRule rule) {
		rules.add(rule);
	}

	@Override
	public IToken getSuccessToken() {
		// We need to be aware of what success token we are referring to.
		// The current internal rule index will help us determine which
		// one.
		if (evalIndex < rules.size()) {
			SingleLineRule x = (SingleLineRule) rules.get(evalIndex);
			return x.getSuccessToken();
		}
		return super.getSuccessToken();
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
			evalIndex = 0;
			// Check if we are within outer rule boundaries.
			if (column >= endIndex || column < startIndex) {
				// If not, then we should evaluate to see if the
				// outer rule is true starting at the current position.
				startIndex = scanner.getColumn();
				if (super.evaluate(scanner, false) != Token.UNDEFINED) {
					// Outer rule is true for a section.  Now we can
					// set the boundaries for the internal rules.
					// End boundary for internal rules is the start of
					// the end sequence.
					endIndex = scanner.getColumn();
					endBoundary = endIndex - endSequence.length();
					// Back up scanner to just after start sequence.
					backupScanner(scanner, startIndex + startSequence.length());
					return super.getSuccessToken();
				} else
					// Outer rule doesn't hold.
					return Token.UNDEFINED;
			}
		}

		// At this point, we want to subdivide up the area covered by the
		// outer rule into success tokens for internal areas separated by
		// areas of the outer rule.

		int start = scanner.getColumn();
		column = start;
		while (column < endBoundary) {
			while (evalIndex < rules.size()) {
				SingleLineRule x = (SingleLineRule) rules.get(evalIndex);
				IToken token = x.evaluate(scanner, false);
				if (!token.isUndefined()) {
					// Found internal token.  If we had to read to get
					// to the start of the internal token, then back up
					// the scanner to the start of the internal token and
					// return the initial read area as part of an outer token.
					// Otherwise, return the internal token.
					if (column == start) {
						evalIndex = 0;
						return token;
					} else {
						backupScanner(scanner, column);
						return super.getSuccessToken();
					}
				}
				++evalIndex;
			}
			evalIndex = 0;
			scanner.read();
			++column;
		}

		// Outside internal area.  Read until end of outer area and return
		// outer token.
		while (column++ < endIndex)
			scanner.read();
		startIndex = 0;
		endIndex = 0;
		return super.getSuccessToken();
	}
}
