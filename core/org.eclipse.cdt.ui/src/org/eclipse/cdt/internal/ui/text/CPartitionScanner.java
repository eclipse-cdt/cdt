package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;


import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;


/**
 * This scanner recognizes comments
 */
public class CPartitionScanner extends BufferedRuleBasedScanner {


	private final static String SKIP= "__skip";


	public final static String C_MULTILINE_COMMENT= "c_multi_line_comment";
	public final static String C_SINGLE_LINE_COMMENT= "c_single_line_comment";
	public final static String C_STRING= "c_string";


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
	public CPartitionScanner() {
		// Set buffer size to 1k
		super(1000);
		
		IToken comment= new Token(C_MULTILINE_COMMENT);
		IToken single_comment= new Token(C_SINGLE_LINE_COMMENT);
		IToken string= new Token(C_STRING);
		IToken skip= new Token(SKIP);



		List rules= new ArrayList();


		// Minimize the number of rules, since we have duplicate rules 
		// in the CCodeScanner...


		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", single_comment));


		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		rules.add(new SingleLineRule("'", "'", skip, '\\'));


		// Add special case word rule.
		//WordRule wordRule= new WordRule(new EmptyCommentDetector());
		//wordRule.addWord("/**/", comment);
		//rules.add(wordRule);


		// Add rules for multi-line comments.
		//rules.add(new MultiLineRule("/*", "*/", comment));
		rules.add(new CMultilineCommentScanner(comment, (char)0, false));

		IRule[] result= new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
