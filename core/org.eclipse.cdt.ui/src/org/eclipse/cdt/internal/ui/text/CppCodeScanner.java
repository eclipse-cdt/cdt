package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.text.util.CWordDetector;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A C code scanner.
 */
public final class CppCodeScanner extends AbstractCScanner {

	private static String[] fgKeywords= { 
			"and", "and_eq", "asm", "auto",  					//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"bitand", "bitor", "break", 						//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"case", "catch", "class", "compl", "const", "const_cast", "continue", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"default", "delete", "do", "dynamic_cast", 			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"else", "enum", "explicit", "export", "extern", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"false", "final", "finally", "for",	"friend", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"goto",  											//$NON-NLS-1$
			"if", "inline", 									//$NON-NLS-1$ //$NON-NLS-2$
			"mutable", 											//$NON-NLS-1$
			"namespace", "new", "not", "not_eq", 				//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"operator", "or", "or_eq",  						//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"private", "protected", "public",  					//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"redeclared", "register", "reinterpret_cast", "return", "restrict", 		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"sizeof", "static", "static_cast", "struct", "switch", 						 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"template", "this", "throw", "true", "try", "typedef", "typeid", "typename", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"union", "using", 									//$NON-NLS-1$ //$NON-NLS-2$
			"virtual", "volatile",  							//$NON-NLS-1$ //$NON-NLS-2$
			"while", 											//$NON-NLS-1$
			"xor", "xor_eq" 									//$NON-NLS-1$ //$NON-NLS-2$

	};


	private static String[] fgTypes= { "bool", "char", "double",		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
			                           "float", "int", "long", 			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
									   "short", "signed", "unsigned", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
									   "void", "wchar_t", "_Bool", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
									   "_Complex", "_Imaginary"}; 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private static String[] fgConstants= { "false", "NULL", "true", 			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			                               "__DATE__", "__LINE__", "__TIME__", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
										   "__FILE__", "__STDC__"};				//$NON-NLS-1$ //$NON-NLS-2$ 
	
	private static String[] fgPreprocessor= { "#define", "#undef", "#include",	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
			                                  "#error", "#warning", "#pragma", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#ifdef", "#ifndef", "#line", 	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#undef", "#if", "#else", 		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
											  "#elif", "#endif"}; 				//$NON-NLS-1$ //$NON-NLS-2$ 

	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
		ICColorConstants.C_DEFAULT
	};
	

	/**
	 * Creates a C++ code scanner
	 */
	public CppCodeScanner(IColorManager manager, IPreferenceStore store) {
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