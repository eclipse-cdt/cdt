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

import org.eclipse.cdt.make.internal.ui.text.IMakefileColorManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class MakefileCodeScanner extends RuleBasedScanner {

	private final static String[] keywords = { "define", "endef", "ifdef", "ifndef", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"ifeq", "ifneq", "else", "endif", "include", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		"-include", "sinclude", "override", "endef", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"export", "unexport", "vpath" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};

	private final static String[] functions = { "subst", "patsubst", "strip", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"findstring", "filter", "sort", "dir", "notdir", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		"suffix", "basename", "addsuffix", "addprefix", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"join", "word", "words", "wordlist", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"firstword", "wildcard", "error", "warning", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		"shell", "origin", "foreach", "call" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};


	/**
	 * Constructor for MakefileCodeScanner
	 */
	public MakefileCodeScanner(IMakefileColorManager provider) {
		super();

		IToken keyword = new Token(new TextAttribute(provider.getColor(IMakefileColorManager.MAKE_KEYWORD)));
		IToken function = new Token(new TextAttribute(provider.getColor(IMakefileColorManager.MAKE_FUNCTION)));
		IToken comment = new Token(new TextAttribute(provider.getColor(IMakefileColorManager.MAKE_COMMENT)));
		IToken macro = new Token(new TextAttribute(provider.getColor(IMakefileColorManager.MAKE_MACRO_VAR)));
		IToken other = new Token(new TextAttribute(provider.getColor(IMakefileColorManager.MAKE_DEFAULT)));

		List rules = new ArrayList();

		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", comment, '\\')); //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char character) {
				return Character.isWhitespace(character);
			}
		}));

		// Add word rule for keywords, types, and constants.
		// We restring the detection of the keywords to be the first column to be valid.
		WordRule keyWordRule = new WordRule(new MakefileWordDetector(), other);
		for (int i = 0; i < keywords.length; i++) {
			keyWordRule.addWord(keywords[i], keyword);
		}
		keyWordRule.setColumnConstraint(0);
		rules.add(keyWordRule);

		WordRule functionRule = new WordRule(new MakefileWordDetector(), other);
		for (int i = 0; i < functions.length; i++)
			functionRule.addWord(functions[i], function);

		rules.add(functionRule);

		//rules.add(new PatternRule("$(", ")", macro, '\\', true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MakefileSimpleMacroRule(macro)); //$NON-NLS-1$ //$NON-NLS-2$

		rules.add(new MacroRule(macro, other));

		setDefaultReturnToken(other);

		IRule[] result = new IRule[rules.size()];

		rules.toArray(result);

		setRules(result);

	}

	private class MacroRule implements IRule {
		private static final int INIT_STATE = 0;
		private static final int VAR_STATE = 1;
		private static final int END_VAR_STATE = 2;
		private static final int EQUAL_STATE = 3;
		private static final int FINISH_STATE = 4;
		private static final int ERROR_STATE = 5;

		private IToken token;
		private StringBuffer buffer = new StringBuffer();
		protected IToken defaultToken;
		public MacroRule(IToken token, IToken defaultToken) {
			this.token = token;
			this.defaultToken = defaultToken;
		}
		public IToken evaluate(ICharacterScanner scanner) {
			int state = INIT_STATE;
			buffer.setLength(0);
			boolean bTwoCharsAssign = false;

			for (int c = scanner.read(); c != ICharacterScanner.EOF; c = scanner.read()) {
				switch (state) {
					case INIT_STATE :
						if (c != '\n' && Character.isWhitespace((char) c))
							break;
						if (isValidCharacter(c))
							state = VAR_STATE;
						else
							state = ERROR_STATE;
						break;
					case VAR_STATE :
						if (isValidCharacter(c))
							break;
					case END_VAR_STATE :
						if (c != '\n' && Character.isWhitespace((char) c))
							state = END_VAR_STATE;
						else if (c == ':' || c == '+') {
							bTwoCharsAssign = true;
							state = EQUAL_STATE;
						} else if (c == '=')
							state = FINISH_STATE;
						else {
							if (state == END_VAR_STATE)
								scanner.unread(); // Return back to the space
							state = ERROR_STATE;
						}
						break;
					case EQUAL_STATE :
						if (c == '=')
							state = FINISH_STATE;
						else
							state = ERROR_STATE;
						break;
					case FINISH_STATE :
						break;
					default :
						break;
				}
				if (state >= FINISH_STATE)
					break;
				buffer.append((char) c);
			}

			scanner.unread();

			if (state == FINISH_STATE) {
				if (bTwoCharsAssign)
					scanner.unread();
				return token;
			}

			if (defaultToken.isUndefined())
				unreadBuffer(scanner);

			return Token.UNDEFINED;
		}

		/**
		 * Returns the characters in the buffer to the scanner.
		 *
		 * @param scanner the scanner to be used
		 */
		protected void unreadBuffer(ICharacterScanner scanner) {
			for (int i = buffer.length() - 1; i >= 0; i--)
				scanner.unread();
		}

		protected boolean isValidCharacter(int c) {
			char c0 = (char) c;
			return Character.isLetterOrDigit(c0) || (c0 == '_');
		}
	}

}
