/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.internal.ui.text.AbstractCScanner;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.text.util.CWhitespaceDetector;


/**
 * A C code scanner.
 */
public final class AsmCodeScanner extends AbstractCScanner {
	
	private static String[] fgKeywords= { 
			".set", ".section",   							//$NON-NLS-1$ //$NON-NLS-2$
			".global",".file",  							//$NON-NLS-1$ //$NON-NLS-2$
			".extern", ".macro", ".endm",  					//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			".if", ".ifdef", ".ifndef", ".else", ".endif", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			".include", ".globl",  							//$NON-NLS-1$ //$NON-NLS-2$
			".text",".data", ".rodata", ".common", ".debug", ".ctor", ".dtor",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			".ascii", ".asciz", ".byte", ".long", ".size", ".align", ".type" 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	};


	private static String[] fgTypes= { "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.C_DEFAULT
	};
	
	/**
	 * Creates a C code scanner
	 */
	public AsmCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
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
		
		Token token= getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
		
		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", token)); //$NON-NLS-1$
		
		Token other= getToken(ICColorConstants.C_DEFAULT);		
		
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		// Add word rule for labels
		WordRule labelRule = new WordRule(new AsmWordDetector(false), other) {
			private StringBuffer fBuffer= new StringBuffer();
			/*
			 * @see IRule#evaluate
			 */
			public IToken evaluate(ICharacterScanner scanner) {
				int c= scanner.read();
				if (fDetector.isWordStart((char) c)) {
					if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
						
						fBuffer.setLength(0);
						do {
							fBuffer.append((char) c);
							c= scanner.read();
						} while (fDetector.isWordPart((char) c));
						if(c != ':') {
							unreadBuffer(scanner);
							return fDefaultToken;
						}
						fBuffer.append((char) c);
						IToken token= (IToken) fWords.get(":"); //$NON-NLS-1$
						if (token != null)
							return token;
							
						return fDefaultToken;
					}
				}
				
				scanner.unread();
				return Token.UNDEFINED;
			}
			/**
			 * Adds a word and the token to be returned if it is detected.
			 *
			 * @param word the word this rule will search for, may not be <code>null</code>
			 * @param token the token to be returned if the word has been found, may not be <code>null</code>
			 */
			public void addWord(String word, IToken token) {
				
				fWords.put(word, token);
			}
			/**
			 * Returns the characters in the buffer to the scanner.
			 *
			 * @param scanner the scanner to be used
			 */
			protected void unreadBuffer(ICharacterScanner scanner) {
				for (int i= fBuffer.length() - 1; i >= 0; i--)
					scanner.unread();
			}
		};
		
		token= getToken(ICColorConstants.C_TYPE);
		labelRule.addWord(":", token); //$NON-NLS-1$
		//wordRule.setColumnConstraint(0);
		rules.add(labelRule);
		
		// Add word rule for keywords and types
		WordRule wordRule= new WordRule(new AsmWordDetector('.'), other);
		for (int i=0; i<fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);
		for (int i=0; i<fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], token);
		rules.add(wordRule);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		WordPatternRule regPattern = new WordPatternRule(new AsmWordDetector('%', (char)0), "%", null, token); //$NON-NLS-1$
		rules.add(regPattern);

		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	public void setRules(IRule[] rules) {
		
		super.setRules(rules);	
	}

	/*
	 * @see AbstractCScanner#affectsBehavior(PropertyChangeEvent)
	 */	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return super.affectsBehavior(event);
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