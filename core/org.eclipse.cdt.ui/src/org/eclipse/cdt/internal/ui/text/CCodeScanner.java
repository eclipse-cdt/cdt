/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.ui.text.util.CWordDetector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A C code scanner.
 */
public final class CCodeScanner extends AbstractCScanner {
	
	/** Constants which are additionally colored. */
    private static String[] fgConstants= { "NULL", "__DATE__", "__LINE__",  	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
			                           "__TIME__", "__FILE__", "__STDC__",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                       "bool", "TRUE", "FALSE", 	            //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                       "EXT_TEXT"};                             //$NON-NLS-1$
    /** Properties for tokens. */
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
        ICColorConstants.C_OPERATOR,
        ICColorConstants.C_BRACES,
        ICColorConstants.C_NUMBER,
		ICColorConstants.C_DEFAULT,
	};
	

	/**
	 * Creates a C code scanner.
     * @param manager Color manager.
     * @param store Preference store.
	 */
	public CCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}
	
	/**
	 * @see AbstractCScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/**
	 * @see AbstractCScanner#createRules()
	 */
	protected List createRules() {
				
		List rules= new ArrayList();		
		
		// Add rule for strings
		Token token= getToken(ICColorConstants.C_STRING);
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
				
		
		// Add generic white space rule.
		//rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		// Add word rule for keywords, types, and constants.
		token= getToken(ICColorConstants.C_DEFAULT);
		WordRule wordRule= new WordRule(new CWordDetector(), token);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		Iterator i = ParserFactory.getKeywordSet( KeywordSetKey.KEYWORDS, ParserLanguage.C ).iterator();
		while( i.hasNext() )
			wordRule.addWord((String) i.next(), token);
        token= getToken(ICColorConstants.C_TYPE);
		i = ParserFactory.getKeywordSet( KeywordSetKey.TYPES, ParserLanguage.C ).iterator();
		while( i.hasNext() )
			wordRule.addWord((String) i.next(), token);
		
		for (int j=0; j<fgConstants.length; j++)
			wordRule.addWord(fgConstants[j], token);

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
        
        token = getToken(ICColorConstants.C_TYPE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), token);
		
		i = ParserFactory.getKeywordSet( KeywordSetKey.PP_DIRECTIVE, ParserLanguage.C ).iterator();
		while( i.hasNext() )
			preprocessorRule.addWord((String) i.next(), token);
		
		rules.add(preprocessorRule);
		
		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}

	/**
     * @see org.eclipse.jface.text.rules.RuleBasedScanner#setRules(org.eclipse.jface.text.rules.IRule[])
	 */
    public void setRules(IRule[] rules) {
		super.setRules(rules);	
	}

	/**
	 * @see AbstractCScanner#affectsBehavior(PropertyChangeEvent)
	 */	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return super.affectsBehavior(event);
	}

	/**
	 * @see AbstractCScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
			
		if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
}