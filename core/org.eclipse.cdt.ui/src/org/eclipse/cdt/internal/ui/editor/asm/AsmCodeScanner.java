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
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.IAsmLanguage;
import org.eclipse.cdt.internal.ui.text.CWhitespaceRule;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;

/*
 * An assembly code scanner.
 */
public final class AsmCodeScanner extends AbstractCScanner {

	private static String[] fgTokenProperties = { ICColorConstants.ASM_DIRECTIVE, ICColorConstants.ASM_LABEL,
			ICColorConstants.C_KEYWORD, ICColorConstants.C_SINGLE_LINE_COMMENT, ICColorConstants.C_DEFAULT };

	private IAsmLanguage fAsmLanguage;

	/**
	 * Creates an assembly code scanner.
	 */
	public AsmCodeScanner(ITokenStoreFactory factory, IAsmLanguage asmLanguage) {
		super(factory.createTokenStore(fgTokenProperties));
		fAsmLanguage = asmLanguage;
		setRules(createRules());
	}

	protected List<IRule> createRules() {
		IToken token;
		List<IRule> rules = new ArrayList<>();

		// Add rule(s) for single line comments
		token = getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
		char[] lineCommentChars = fAsmLanguage.getLineCommentCharacters();
		for (int i = 0; i < lineCommentChars.length; i++) {
			rules.add(new EndOfLineRule(new String(new char[] { lineCommentChars[i] }), token));
		}

		final IToken other = getToken(ICColorConstants.C_DEFAULT);

		// Add generic whitespace rule.
		rules.add(new CWhitespaceRule(other));

		// Add rule for labels
		token = getToken(ICColorConstants.ASM_LABEL);
		IRule labelRule = new AsmLabelRule(new AsmWordDetector(false), token, other);
		rules.add(labelRule);

		// Add word rule for keywords
		token = getToken(ICColorConstants.ASM_DIRECTIVE);
		String[] keywords = fAsmLanguage.getDirectiveKeywords();
		WordRule wordRule = new WordRule(new AsmWordDetector('.'), other);
		for (int i = 0; i < keywords.length; i++)
			wordRule.addWord(keywords[i], token);
		rules.add(wordRule);

		// TODO use extra color?
		token = getToken(ICColorConstants.C_KEYWORD);
		WordPatternRule regPattern = new WordPatternRule(new AsmWordDetector('%', (char) 0), "%", null, token); //$NON-NLS-1$
		rules.add(regPattern);

		setDefaultReturnToken(other);
		return rules;
	}
}