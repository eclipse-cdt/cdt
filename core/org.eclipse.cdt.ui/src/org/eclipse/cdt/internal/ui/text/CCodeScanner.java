package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.internal.ui.text.util.CWordDetector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A C code scanner.
 */
public final class CCodeScanner extends AbstractCScanner {
	

	private static String[] fgKeywords= { 
			"asm", "auto",
			"break", 
			"case", 
			"const", "continue",
			"default", "do",
			"else",	"enum",	"extern", 
			"for",
			"goto", 
			"if", "inline", 
			"register", "return", "restrict",
			"sizeof", "static", "struct", "switch", 
			"typedef", 
			"union",
			"volatile", 
			"while", "_Pragma"
	};


	private static String[] fgTypes= { "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void", "_Bool", "_Complex", "_Imaginary"};
	private static String[] fgConstants= { "NULL", "__DATE__", "__LINE__", "__TIME__", "__FILE__", "__STDC__"};
	private static String[] fgPreprocessor= { "#define", "#undef", "#include", "#error", "#warning", "#pragma", "#ifdef", "#ifndef", "#if", "#else", "#elif", "#endif", "#line"};

	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
		ICColorConstants.C_DEFAULT
	};
	

	/**
	 * Creates a C code scanner
	 */
	public CCodeScanner(IColorManager manager, IPreferenceStore store) {
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
		
		// Add rule for strings
		Token token= getToken(ICColorConstants.C_STRING);
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("'", "'", token, '\\'));
				
		
		// Add generic whitespace rule.
		//rules.add(new WhitespaceRule(new CWhitespaceDetector()));

		
		// Add word rule for keywords, types, and constants.
		token= getToken(ICColorConstants.C_DEFAULT);
		WordRule wordRule= new WordRule(new CWordDetector(), token);
		
		token= getToken(ICColorConstants.C_KEYWORD);
		for (int i=0; i<fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);
		token= getToken(ICColorConstants.C_TYPE);
		for (int i=0; i<fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], token);
		for (int i=0; i<fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], token);
		for (int i=0; i<fgPreprocessor.length; i++)
			wordRule.addWord(fgPreprocessor[i], token);
		rules.add(wordRule);
					
		PatternRule patternRule;
		for (int i=0; i<fgPreprocessor.length; i++) {
			patternRule = new PatternRule(fgPreprocessor[i], " ", getToken(ICColorConstants.C_TYPE), (char)0, true);
			patternRule.setColumnConstraint(0); // For now, until we have a better rule
			//rules.add(patternRule);
		}
		
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