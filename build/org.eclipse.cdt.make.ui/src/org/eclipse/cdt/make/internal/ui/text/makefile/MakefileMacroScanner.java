/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class MakefileMacroScanner extends RuleBasedScanner {
	private String buffer;
	private final static String[] DELIMITERS = { "\r", "\n", "\r\n" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public final static String tokenText = "text"; //$NON-NLS-1$
	public final static String tokenMacro = "macro"; //$NON-NLS-1$
	public final static String tokenOther = "other"; //$NON-NLS-1$

	/**
	 * Constructor for MakefileMacroScanner
	 */
	public MakefileMacroScanner(String buffer) {
		super();
		this.buffer = buffer;
		fOffset = 0;

		IToken tText = new Token(tokenText);
		IToken tMacro = new Token(tokenMacro);
		IToken tOther = new Token(tokenOther);

		List rules = new ArrayList();

		rules.add(new PatternRule("\"", "\"", tText, '\\', true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new PatternRule("\'", "\'", tText, '\\', true)); //$NON-NLS-1$ //$NON-NLS-2$

		rules.add(new MakefileSimpleMacroRule(tMacro));

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char character) {
				return Character.isWhitespace(character);
			}
		}));

		WordRule wRule = new WordRule(new IWordDetector() {
			public boolean isWordPart(char c) {
				return isWordStart(c);
			}
			public boolean isWordStart(char c) {
				return !(((short) c == EOF) || Character.isSpaceChar(c));
			}
		}, tOther);

		rules.add(wRule);

		IRule[] result = new IRule[rules.size()];

		rules.toArray(result);

		setRules(result);

		setRange(null, 0, buffer.length());
	}

	/**
	 * @see RuleBasedScanner#getColumn()
	 */
	public int getColumn() {
		return fOffset;
	}

	/**
	 * @see RuleBasedScanner#read()
	 */
	public int read() {
		int c;
		if (fOffset == buffer.length())
			c = EOF;
		else
			c = buffer.charAt(fOffset);
		++fOffset;
		return c;
	}

	/**
	 * @see RuleBasedScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		fDocument = document;
		fOffset = offset;
		fRangeEnd = offset + length;

		fDelimiters = new char[DELIMITERS.length][];
		for (int i = 0; i < DELIMITERS.length; i++)
			fDelimiters[i] = DELIMITERS[i].toCharArray();
	}

}
