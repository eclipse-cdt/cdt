/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class MakefileCodeScanner extends AbstractMakefileCodeScanner {
	@SuppressWarnings("nls")
	private final static String[] keywords = {
		"define", "endef", "ifdef", "ifndef",
		"ifeq", "ifneq", "else", "endif", "include",
		"-include", "sinclude", "override",
		"export", "unexport", "vpath"
	};

	public static final String[] fTokenProperties = new String[] {
		ColorManager.MAKE_KEYWORD_COLOR,
		ColorManager.MAKE_FUNCTION_COLOR,
		ColorManager.MAKE_MACRO_REF_COLOR,
		ColorManager.MAKE_MACRO_DEF_COLOR,
		ColorManager.MAKE_DEFAULT_COLOR
	};

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

		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			@Override
			public boolean isWhitespace(char character) {
				return Character.isWhitespace(character);
			}
		}, defaultToken));

		rules.add(new MacroDefinitionRule(macroDefToken, Token.UNDEFINED));

		// Add word rule for keywords, types, and constants.
		// We restrict the detection of the keywords to be the first column to be valid.
		WordRule keyWordRule = new WordRule(new IWordDetector() {
			@Override
			public boolean isWordPart(char c) {
				return Character.isLetterOrDigit(c) || c == '_';
			}
			@Override
			public boolean isWordStart(char c) {
				return Character.isLetterOrDigit(c) || c == '_' || c == '-';
			}
		});
		for (String keyword : keywords) {
			keyWordRule.addWord(keyword, keywordToken);
		}
		keyWordRule.setColumnConstraint(0);
		rules.add(keyWordRule);

		rules.add(new FunctionReferenceRule(functionToken));

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
