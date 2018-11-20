/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class MakefileCodeScanner extends AbstractMakefileCodeScanner {
	@SuppressWarnings("nls")
	private final static String[] keywords = { "define", "endef", "ifdef", "ifndef", "ifeq", "ifneq", "else", "endif",
			"include", "-include", "sinclude", "override", "export", "unexport", "vpath" };

	public static final String[] fTokenProperties = new String[] { ColorManager.MAKE_KEYWORD_COLOR,
			ColorManager.MAKE_FUNCTION_COLOR, ColorManager.MAKE_MACRO_REF_COLOR, ColorManager.MAKE_MACRO_DEF_COLOR,
			ColorManager.MAKE_DEFAULT_COLOR };

	private final class KeywordDetector implements IWordDetector {
		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c);
		}

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetterOrDigit(c) || c == '-';
		}
	}

	private class KeywordRule extends WordRule {
		private KeywordRule() {
			super(new KeywordDetector());
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int offset = fOffset;
			IToken token = super.evaluate(scanner);
			if (token != fDefaultToken) {
				// check if the keyword starts from beginning of line possibly indented
				try {
					int line = fDocument.getLineOfOffset(offset);
					int start = fDocument.getLineOffset(line);
					String ident = fDocument.get(start, offset - start);
					if (ident.trim().length() > 0) {
						token = fDefaultToken;
					}
				} catch (BadLocationException ex) {
				}
			}
			return token;
		}
	}

	/**
	 * Constructor for MakefileCodeScanner
	 */
	public MakefileCodeScanner() {
		super();
		initialize();
	}

	@Override
	protected List<IRule> createRules() {
		IToken keywordToken = getToken(ColorManager.MAKE_KEYWORD_COLOR);
		IToken functionToken = getToken(ColorManager.MAKE_FUNCTION_COLOR);
		IToken macroRefToken = getToken(ColorManager.MAKE_MACRO_REF_COLOR);
		IToken macroDefToken = getToken(ColorManager.MAKE_MACRO_DEF_COLOR);
		IToken defaultToken = getToken(ColorManager.MAKE_DEFAULT_COLOR);

		List<IRule> rules = new ArrayList<>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			@Override
			public boolean isWhitespace(char character) {
				return Character.isWhitespace(character);
			}
		}, defaultToken));

		rules.add(new MacroDefinitionRule(macroDefToken, Token.UNDEFINED));

		// Add word rule for keywords, types, and constants.
		WordRule keyWordRule = new KeywordRule();
		for (String keyword : keywords) {
			keyWordRule.addWord(keyword, keywordToken);
		}
		rules.add(keyWordRule);

		rules.add(new FunctionReferenceRule(functionToken, "$(", ")")); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new FunctionReferenceRule(functionToken, "${", "}")); //$NON-NLS-1$ //$NON-NLS-2$

		rules.add(new AutomaticVariableReferenceRule(macroRefToken));
		rules.add(new MacroReferenceRule(macroRefToken, "$(", ")")); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MacroReferenceRule(macroRefToken, "$$(", ")")); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MacroReferenceRule(macroRefToken, "${", "}")); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new MacroReferenceRule(macroRefToken, "$${", "}")); //$NON-NLS-1$ //$NON-NLS-2$

		setDefaultReturnToken(defaultToken);

		return rules;
	}

	@Override
	protected String[] getTokenProperties() {
		return fTokenProperties;
	}

}
