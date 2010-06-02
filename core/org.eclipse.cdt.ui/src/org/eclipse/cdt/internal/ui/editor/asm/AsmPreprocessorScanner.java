/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.cdt.core.model.IAsmLanguage;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

import org.eclipse.cdt.internal.ui.text.CHeaderRule;
import org.eclipse.cdt.internal.ui.text.CWhitespaceRule;
import org.eclipse.cdt.internal.ui.text.PreprocessorRule;
import org.eclipse.cdt.internal.ui.text.util.CWordDetector;

/**
 * A preprocessor directive scanner for Asm source.
 *
 * @since 4.0
 */
public class AsmPreprocessorScanner extends AbstractCScanner {

	/** Properties for tokens. */
	private static final String[] fgTokenProperties= {
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.PP_DIRECTIVE,
		ICColorConstants.C_STRING,
		ICColorConstants.PP_HEADER,
		ICColorConstants.PP_DEFAULT,
	};

	private IAsmLanguage fAsmLanguage;

	/**
	 * Create a preprocessor directive scanner.
	 * @param factory
	 * @param asmLanguage
	 */
	public AsmPreprocessorScanner(ITokenStoreFactory factory, IAsmLanguage asmLanguage) {
		super(factory.createTokenStore(fgTokenProperties));
		Assert.isNotNull(asmLanguage);
		fAsmLanguage= asmLanguage;
		setRules(createRules());
	}

	/**
	 * Creates rules used in this RulesBasedScanner
	 */
	protected List<IRule> createRules() {
		List<IRule> rules= new ArrayList<IRule>();
		IToken defaultToken= getToken(ICColorConstants.PP_DEFAULT);
		IToken token;

		// Add generic white space rule.
		rules.add(new CWhitespaceRule(defaultToken));

		token= getToken(ICColorConstants.PP_DIRECTIVE);
		PreprocessorRule preprocessorRule= new PreprocessorRule(new CWordDetector(), defaultToken, getToken(ICColorConstants.C_SINGLE_LINE_COMMENT));
		String[] ppKeywords= fAsmLanguage.getPreprocessorKeywords();
		for (int i= 0; i < ppKeywords.length; i++) {
			String ppKeyword= ppKeywords[i];
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
		IRule lineCommentRule= new EndOfLineRule("#", token); //$NON-NLS-1$
		rules.add(lineCommentRule);

		//        token = getToken(ICColorConstants.C_MULTI_LINE_COMMENT);
		//        IRule blockCommentRule = new MultiLineRule("/*", "*/", token, '\\'); //$NON-NLS-1$ //$NON-NLS-2$
		//        rules.add(blockCommentRule);

		setDefaultReturnToken(defaultToken);
		return rules;
	}
}
