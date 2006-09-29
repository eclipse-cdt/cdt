/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor.asm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;

import org.eclipse.cdt.internal.ui.text.AbstractCScanner;
import org.eclipse.cdt.internal.ui.text.CHeaderRule;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.PreprocessorRule;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.cdt.internal.ui.text.util.CWhitespaceDetector;
import org.eclipse.cdt.internal.ui.text.util.CWordDetector;

/**
 * A preprocessor directive scanner for Asm source.
 *
 * @since 4.0
 */
public class AsmPreprocessorScanner extends AbstractCScanner {

    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.C_MULTI_LINE_COMMENT,
		ICColorConstants.PP_DIRECTIVE,
		ICColorConstants.C_STRING,
        ICColorConstants.PP_HEADER,
		ICColorConstants.PP_DEFAULT,
	};

	/**
	 * Create a preprocessor directive scanner.
	 * 
	 * @param colorManager
	 * @param store
	 */
	public AsmPreprocessorScanner(CColorManager colorManager, IPreferenceStore store) {
		super(colorManager, store);
		initialize();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCScanner#createRules()
	 */
	protected List createRules() {

		Token defaultToken= getToken(ICColorConstants.PP_DEFAULT);

		List rules= new ArrayList();		
		Token token;
		
		// Add generic white space rule.
		rules.add(new WhitespaceRule(new CWhitespaceDetector()));
		
		token= getToken(ICColorConstants.PP_DIRECTIVE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), defaultToken, getToken(ICColorConstants.C_SINGLE_LINE_COMMENT));
		Iterator iter;
		iter = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.C ).iterator();
		while( iter.hasNext() ) {
			String ppKeyword= (String) iter.next();
			if (ppKeyword.length() > 1) {
				preprocessorRule.addWord(ppKeyword, token);
			}
		}
		// add ## operator
		preprocessorRule.addWord("##", token); //$NON-NLS-1$
		rules.add(preprocessorRule);

        token = getToken(ICColorConstants.PP_HEADER);
        CHeaderRule headerRule = new CHeaderRule(token);
        rules.add(headerRule);

        token = getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
        IRule lineCommentRule = new EndOfLineRule("//", token, '\\', true); //$NON-NLS-1$
        rules.add(lineCommentRule);
        lineCommentRule = new EndOfLineRule("#", token); //$NON-NLS-1$
        rules.add(lineCommentRule);

        token = getToken(ICColorConstants.C_MULTI_LINE_COMMENT);
        IRule blockCommentRule = new MultiLineRule("/*", "*/", token, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
        rules.add(blockCommentRule);
        
        setDefaultReturnToken(defaultToken);
		return rules;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

}
