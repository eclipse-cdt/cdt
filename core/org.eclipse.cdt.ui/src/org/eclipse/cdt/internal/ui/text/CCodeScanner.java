package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.List;

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
	

	private static String[] fgKeywords= { 
			"asm", "auto", 							//$NON-NLS-1$ //$NON-NLS-2$
			"break",  								//$NON-NLS-1$
			"case",  								//$NON-NLS-1$
			"const", "continue",					//$NON-NLS-1$ //$NON-NLS-2$
			"default", "do",						//$NON-NLS-1$ //$NON-NLS-2$
			"else",	"enum",	"extern",  				//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"for", 									//$NON-NLS-1$
			"goto",  								//$NON-NLS-1$
			"if", "inline",  						//$NON-NLS-1$ //$NON-NLS-2$
			"register", "return", "restrict", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"sizeof", "static", "struct", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"typedef",  							//$NON-NLS-1$
			"union", 								//$NON-NLS-1$
			"volatile",  							//$NON-NLS-1$
			"while", "_Pragma" 						//$NON-NLS-1$ //$NON-NLS-2$
	};


	private static String[] fgTypes= { "char", "double", "float",				//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			                           "int", "long", "short", 					//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
									   "signed", "unsigned", "void", 			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
									   "_Bool", "_Complex", "_Imaginary"};  	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	private static String[] fgConstants= { "NULL", "__DATE__", "__LINE__",  	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
			                           "__TIME__", "__FILE__", "__STDC__"}; 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	private static String[] fgPreprocessor= { "#define", "#undef", "#include",	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
			                                  "#error", "#warning", "#pragma", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#ifdef", "#ifndef", "#if", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#else", "#elif", "#endif", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#line"}; 						//$NON-NLS-1$ 

	
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
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
				
		
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
		rules.add(wordRule);

		token = getToken(ICColorConstants.C_TYPE);
		PreprocessorRule preprocessorRule = new PreprocessorRule(new CWordDetector(), token);
					
		for (int i=0; i<fgPreprocessor.length; i++) {
			preprocessorRule.addWord(fgPreprocessor[i], token);
		}
		rules.add(preprocessorRule);
		
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