package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;


/**
 * This scanner recognizes comments
 */
public class AsmPartitionScanner extends RuleBasedScanner {


	private final static String SKIP= "__skip";


	public final static String C_MULTILINE_COMMENT= "__c_multiline_comment";


	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {


		/**
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return (c == '/');
		}


		/**
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/');
		}
	};


	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public AsmPartitionScanner() {
		super();
		
		IToken comment= new Token(C_MULTILINE_COMMENT);


		List rules= new ArrayList();


		// Minimize the number of rules, since we have duplicate rules 
		// in the CCodeScanner...


		// Add rule for single line comments.
		//rules.add(new EndOfLineRule("//", Token.UNDEFINED));


		// Add rule for strings and character constants.
		//rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
		//rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\'));


		// Add special case word rule.
		WordRule wordRule= new WordRule(new EmptyCommentDetector());
		wordRule.addWord("/**/", comment);
		rules.add(wordRule);


		// Add rules for multi-line comments.
		rules.add(new MultiLineRule("/*", "*/", comment));


		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
