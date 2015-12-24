/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WordRule;

import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

import org.eclipse.cdt.internal.ui.text.util.CWordDetector;


/**
 * A C/C++ code scanner.
 */
public final class CCodeScanner extends AbstractCScanner {
	
    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
        ICColorConstants.C_OPERATOR,
        ICColorConstants.C_BRACES,
        ICColorConstants.C_NUMBER,
		ICColorConstants.C_DEFAULT,
	};
	private ICLanguageKeywords fKeywords;

	/**
	 * Creates a C/C++ code scanner.
	 * @param factory 
     * @param keywords  the keywords defined by the language dialect
	 */
	public CCodeScanner(ITokenStoreFactory factory, ICLanguageKeywords keywords) {
		super(factory.createTokenStore(fgTokenProperties));
		fKeywords= keywords;
		setRules(createRules());
	}
	
	/*
	 * @see AbstractCScanner#createRules()
	 */
	protected List<IRule> createRules() {
				
		List<IRule> rules= new ArrayList<IRule>();		
		IToken token;
		
		token= getToken(ICColorConstants.C_DEFAULT);

		// Add generic white space rule.
		rules.add(new CWhitespaceRule(token));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule= new WordRule(new CWordDetector(), token);
		
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

        token = getToken(ICColorConstants.C_NUMBER);
        NumberRule numberRule = new NumberRule(token);
        rules.add(numberRule);
        
        token = getToken(ICColorConstants.C_OPERATOR);
        COperatorRule opRule = new COperatorRule(token);
        rules.add(opRule);

        token = getToken(ICColorConstants.C_BRACES);
        CBraceRule braceRule = new CBraceRule(token);
        rules.add(braceRule);
        
		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}
}