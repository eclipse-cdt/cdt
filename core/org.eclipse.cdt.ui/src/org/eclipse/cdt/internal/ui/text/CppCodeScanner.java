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
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A C code scanner.
 */
public final class CppCodeScanner extends AbstractCScanner {
	
	private class CWordRule implements IRule {
		
		protected static final int UNDEFINED= -1;
		
		/** The word detector used by this rule */
		protected IWordDetector fDetector;
		/** The default token to be returned on success and if nothing else has been specified. */
		protected IToken fDefaultToken;
		/** The column constraint */
		protected int fColumn= UNDEFINED;
		/** The table of predefined words and token for this rule */
		protected Map fWords= new HashMap();
		
		private StringBuffer fBuffer= new StringBuffer();
	
		/**
		 * Creates a rule which, with the help of an word detector, will return the token
		 * associated with the detected word. If no token has been associated, the scanner 
		 * will be rolled back and an undefined token will be returned in order to allow 
		 * any subsequent rules to analyze the characters.
		 *
		 * @param detector the word detector to be used by this rule, may not be <code>null</code>
		 *
		 * @see #addWord
		 */
		public CWordRule(IWordDetector detector) {
			this(detector, Token.UNDEFINED);
		}
		/**
		 * Creates a rule which, with the help of an word detector, will return the token
		 * associated with the detected word. If no token has been associated, the
		 * specified default token will be returned.
		 *
		 * @param detector the word detector to be used by this rule, may not be <code>null</code>
		 * @param defaultToken the default token to be returned on success 
		 *		if nothing else is specified, may not be <code>null</code>
		 *
		 * @see #addWord
		 */
		public CWordRule(IWordDetector detector, IToken defaultToken) {
			
			Assert.isNotNull(detector);
			Assert.isNotNull(defaultToken);
			
			fDetector= detector;
			fDefaultToken= defaultToken;
		}
		/**
		 * Adds a word and the token to be returned if it is detected.
		 *
		 * @param word the word this rule will search for, may not be <code>null</code>
		 * @param token the token to be returned if the word has been found, may not be <code>null</code>
		 */
		public void addWord(String word, IToken token) {
			Assert.isNotNull(word);
			Assert.isNotNull(token);		
		
			fWords.put(word, token);
		}
		/*
		 * @see IRule#evaluate
		 */
		public IToken evaluate(ICharacterScanner scanner) {
			
			int c= scanner.read();
			if (Character.isJavaIdentifierStart((char) c) || (c == '#' && scanner.getColumn() == 1)) {	
				fBuffer.setLength(0);
				do {
					fBuffer.append((char) c);
					c= scanner.read();
				} while (Character.isJavaIdentifierPart((char) c));
				scanner.unread();
				
				IToken token= (IToken) fWords.get(fBuffer.toString());
				if (token != null)
					return token;
					
				//if (fDefaultToken.isUndefined())
				//	unreadBuffer(scanner);
					
				return fDefaultToken;
			}
			
			scanner.unread();
			return Token.UNDEFINED; 
		}
		/**
		 * Sets a column constraint for this rule. If set, the rule's token
		 * will only be returned if the pattern is detected starting at the 
		 * specified column. If the column is smaller then 0, the column
		 * constraint is considered removed.
		 *
		 * @param column the column in which the pattern starts
		 */
		public void setColumnConstraint(int column) {
			if (column < 0)
				column= UNDEFINED;
			fColumn= column;
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
	private static String[] fgPreprocessor= { "#define", "#include", "#error", "#pragma", "#ifdef", "#ifndef", "#line", "#undef", "#if", "#else", "#elif", "#endif"};

	
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
		CWordRule wordRule= new CWordRule(new CWordDetector(), token);
		
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