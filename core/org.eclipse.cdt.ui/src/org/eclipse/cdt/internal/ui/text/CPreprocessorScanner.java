/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.WordRule;

import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

import org.eclipse.cdt.internal.ui.text.util.CWordDetector;

/**
 * A scanner for preprocessor directives.
 *
 * @since 4.0
 */
public class CPreprocessorScanner extends AbstractCScanner {

    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.PP_DIRECTIVE,
		ICColorConstants.PP_DEFAULT,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
        ICColorConstants.PP_HEADER
	};
	private ICLanguageKeywords fKeywords;
	
	/**
	 * Creates a C/C++ preprocessor scanner.
     * @param keywords  the keywords defined by the language dialect
	 */
	public CPreprocessorScanner(ITokenStoreFactory factory, ICLanguageKeywords keywords) {
		super(factory.createTokenStore(fgTokenProperties));
		fKeywords= keywords;
		setRules(createRules());
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCScanner#createRules()
	 */
	protected List<IRule> createRules() {
		IToken defaultToken= getToken(ICColorConstants.PP_DEFAULT);

		List<IRule> rules= new ArrayList<IRule>();		
		IToken token;
		
		// Add generic white space rule.
		rules.add(new CWhitespaceRule(defaultToken));
		
		token= getToken(ICColorConstants.PP_DIRECTIVE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), defaultToken);
		String[] ppKeywords= fKeywords.getPreprocessorKeywords();
		for (int i = 0; i < ppKeywords.length; i++) {
			preprocessorRule.addWord(ppKeywords[i], token);
		}

		// add ## operator
		preprocessorRule.addWord("##", token); //$NON-NLS-1$
		rules.add(preprocessorRule);

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new CWordDetector(), defaultToken);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		String[] keywords= fKeywords.getKeywords();
		for (int i = 0; i < keywords.length; i++) {
			wordRule.addWord(keywords[i], token);
		}
        token= getToken(ICColorConstants.C_TYPE);
		String[] types= fKeywords.getBuiltinTypes();
		for (int i = 0; i < types.length; i++) {
			wordRule.addWord(types[i], token);
		}
        rules.add(wordRule);
		
        token = getToken(ICColorConstants.PP_HEADER);
        CHeaderRule headerRule = new CHeaderRule(token);
        rules.add(headerRule);

        token = getToken(ICColorConstants.C_STRING);
        IRule stringRule = new PatternRule("\"", "\"", token, '\\', true, true, true); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(stringRule);

        token = getToken(ICColorConstants.C_STRING);
        IRule charRule = new PatternRule("'", "'", token, '\\', true, true, true); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(charRule);

        setDefaultReturnToken(defaultToken);
		return rules;
	}
}
