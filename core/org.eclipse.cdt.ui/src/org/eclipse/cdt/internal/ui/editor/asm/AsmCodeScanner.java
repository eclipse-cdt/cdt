package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.internal.ui.text.AbstractCScanner;
import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.internal.ui.text.eclipse2.Token;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WordPatternRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A C code scanner.
 */
public final class AsmCodeScanner extends AbstractCScanner {
	
	private static class VersionedWordRule extends WordRule {

		private final String fVersion;
		private final boolean fEnable;
		
		private String fCurrentVersion;

		public VersionedWordRule(IWordDetector detector, String version, boolean enable, String currentVersion) {
			super(detector);

			fVersion= version;
			fEnable= enable;
			fCurrentVersion= currentVersion;
		}
		
		public void setCurrentVersion(String version) {
			fCurrentVersion= version;
		}
	
		/*
		 * @see IRule#evaluate
		 */
		public IToken evaluate(ICharacterScanner scanner) {
			IToken token= super.evaluate(scanner);

			if (fEnable) {
				if (fCurrentVersion.equals(fVersion))
					return token;
					
				return Token.UNDEFINED;

			} else {
				if (fCurrentVersion.equals(fVersion))
					return Token.UNDEFINED;
					
				return token;
			}
		}
	}

	private static String[] fgKeywords= { 
			".set", ".section",  
			".global", 
			".extern", ".macro", ".endm", 
			".if", ".ifdef", ".ifndef", ".else", ".endif",
			".include", ".globl", 
			".text",".data", ".rodata", ".common", ".debug", ".ctor", ".dtor", 
			".asciz", ".byte", ".long", ".size", ".align", ".type"
	};


	private static String[] fgTypes= { "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void"};
	
	private static String[] fgTokenProperties= {
		ICColorConstants.C_KEYWORD,
		ICColorConstants.C_TYPE,
		ICColorConstants.C_STRING,
		ICColorConstants.C_SINGLE_LINE_COMMENT,
		ICColorConstants.C_DEFAULT
	};
	
	private VersionedWordRule fVersionedWordRule;

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
		
		// Add rule for strings
		Token token= getToken(ICColorConstants.C_SINGLE_LINE_COMMENT);
		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", token));
		
		// Add rule for single line comments.
		rules.add(new EndOfLineRule("#", token));
		
		token= getToken(ICColorConstants.C_STRING);
		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("'", "'", token, '\\'));
		rules.add(new SingleLineRule("\"", "\"", token, '\\'));
				
		Token other= getToken(ICColorConstants.C_DEFAULT);		
		
		// Add generic whitespace rule.
		//rules.add(new WhitespaceRule(new CWhitespaceDetector()));

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
						} else {
							fBuffer.append((char) c);
							IToken token= (IToken) fWords.get(":");
							if (token != null)
								return token;
						}
							
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
		labelRule.addWord(":", token);
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
		WordPatternRule regPattern = new WordPatternRule(new AsmWordDetector('%', (char)0), "%", null, token);
		rules.add(regPattern);

		setDefaultReturnToken(getToken(ICColorConstants.C_DEFAULT));
		return rules;
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	public void setRules(IRule[] rules) {
		int i;
		for (i= 0; i < rules.length; i++)
			if (rules[i].equals(fVersionedWordRule))
				break;

		// not found - invalidate fVersionedWordRule
		if (i == rules.length)
			fVersionedWordRule= null;
		
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