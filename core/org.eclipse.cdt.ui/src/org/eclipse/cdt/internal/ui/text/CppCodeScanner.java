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
			"and", "and_eq", "asm", "auto", 
			"bitand", "bitor", "break",
			"case", "catch", "class", "compl", "const", "const_cast", "continue",
			"default", "delete", "do", "dynamic_cast",
			"else", "enum", "explicit", "export", "extern",
			"false", "final", "finally", "for",	"friend",
			"goto", 
			"if", "inline",
			"mutable",
			"namespace", "new", "not", "not_eq",
			"operator", "or", "or_eq", 
			"private", "protected", "public", 
			"redeclared", "register", "reinterpret_cast", "return", "restrict",
			"sizeof", "static", "static_cast", "struct", "switch", 
			"template", "this", "throw", "true", "try", "typedef", "typeid", "typename",
			"union", "using",
			"virtual", "volatile", 
			"while",
			"xor", "xor_eq"

	};


	private static String[] fgTypes= { "bool", "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void", "wchar_t", "_Bool", "_Complex", "_Imaginary"};
	private static String[] fgConstants= { "false", "NULL", "true", "__DATE__", "__LINE__", "__TIME__", "__FILE__", "__STDC__"};
	private static String[] fgPreprocessor= { "#define", "#undef", "#include", "#error", "#warning", "#pragma", "#ifdef", "#ifndef", "#line", "#undef", "#if", "#else", "#elif", "#endif"};

	
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