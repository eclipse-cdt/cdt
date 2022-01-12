/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.editor;

import org.eclipse.cdt.examples.dsf.pda.ui.PDAUIPlugin;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * PDA editor keyword scanner.
 */
public class PDAScanner extends BufferedRuleBasedScanner {

	/**
	 * PDA keywods
	 */
	public static final String[] fgKeywords = new String[] { "add", "branch_not_zero", "call", "dec", "dup", "halt",
			"output", "pop", "push", "return", "var" };

	/**
	 * Detects potential keywords
	 */
	class PDAWordDetector implements IWordDetector {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
		 */
		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
		 */
		@Override
		public boolean isWordPart(char c) {
			return Character.isLetter(c) || c == '_';
		}
	}

	/**
	 * Detects PDA branch labels
	 */
	class PDALabelDetector extends PDAWordDetector {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
		 */
		@Override
		public boolean isWordStart(char c) {
			return c == ':';
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
		 */
		@Override
		public boolean isWordPart(char c) {
			return super.isWordPart(c) || Character.isDigit(c);
		}
	}

	/**
	 * Constructs a scanner that identifies PDA keywords.
	 */
	public PDAScanner() {
		// keywords
		Token token = new Token(new TextAttribute(PDAUIPlugin.getDefault().getColor(PDAUIPlugin.KEYWORD)));
		WordRule keywords = new WordRule(new PDAWordDetector());
		for (int i = 0; i < fgKeywords.length; i++) {
			String keyword = fgKeywords[i];
			keywords.addWord(keyword, token);
		}
		// labels
		token = new Token(new TextAttribute(PDAUIPlugin.getDefault().getColor(PDAUIPlugin.LABEL)));
		WordRule labels = new WordRule(new PDALabelDetector(), token);
		setRules(new IRule[] { keywords, labels });
	}
}
