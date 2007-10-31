/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.model.IAsmLanguage;

import org.eclipse.cdt.internal.ui.text.AbstractCScanner;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.text.util.CWhitespaceDetector;


/**
 * An assembly code scanner.
 */
public final class AsmCodeScanner extends AbstractCScanner {
	
	private static String[] fgTokenProperties= {
		ICColorConstants.ASM_DIRECTIVE,
		ICColorConstants.ASM_LABEL,
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.C_DEFAULT
	};

	private IAsmLanguage fAsmLanguage;
	
	/**
	 * Creates an assembly code scanner.
	 */
	public AsmCodeScanner(IColorManager manager, IPreferenceStore store, IAsmLanguage asmLanguage) {
		super(manager, store);
		fAsmLanguage= asmLanguage;
		initialize();
	}
	
	/*
	 * @see AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see AbstractCScanner#createRules()
	 */
	protected List createRules() {
				
		List rules= new ArrayList();		
		
		Token token;

		// Add rule(s) for single line comments
		token= getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
		char[] lineCommentChars= fAsmLanguage.getLineCommentCharacters();
		for (int i= 0; i < lineCommentChars.length; i++) {
			rules.add(new EndOfLineRule(new String(new char [] {lineCommentChars[i]}), token));
		}

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		final Token other= getToken(ICColorConstants.C_DEFAULT);		

		// Add rule for labels
		token= getToken(ICColorConstants.ASM_LABEL);
		IRule labelRule= new AsmLabelRule(new AsmWordDetector(false), token, other);
		rules.add(labelRule);
		
		// Add word rule for keywords
		token= getToken(ICColorConstants.ASM_DIRECTIVE);
		String[] keywords= fAsmLanguage.getDirectiveKeywords();
		WordRule wordRule= new WordRule(new AsmWordDetector('.'), other);
		for (int i=0; i<keywords.length; i++)
			wordRule.addWord(keywords[i], token);

		// TODO use extra color?
		token= getToken(ICColorConstants.C_KEYWORD);
		WordPatternRule regPattern= new WordPatternRule(new AsmWordDetector('%', (char)0), "%", null, token); //$NON-NLS-1$
		rules.add(regPattern);

		setDefaultReturnToken(other);
		return rules;
	}

	/*
	 * @see AbstractCScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
}