package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.ICPartitions;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;


/**
 * This scanner recognizes comments
 */
public class AsmPartitionScanner extends RuleBasedPartitionScanner {


	//private final static String SKIP= "__skip";


	public final static String ASM_MULTILINE_COMMENT= ICColorConstants.C_MULTI_LINE_COMMENT;
	public final static String ASM_SINGLE_LINE_COMMENT= ICColorConstants.C_SINGLE_LINE_COMMENT;
	public final static String ASM_STRING= ICColorConstants.C_STRING;


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
	}
	
	/**
	 * Word rule for empty comments.
	 */
	static class EmptyCommentRule extends WordRule implements IPredicateRule {
		
		private IToken fSuccessToken;
		/**
		 * Constructor for EmptyCommentRule.
		 * @param defaultToken
		 */
		public EmptyCommentRule(IToken successToken) {
			super(new EmptyCommentDetector());
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}
		
		/*
		 * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
		 */
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return evaluate(scanner);
		}

		/*
		 * @see IPredicateRule#getSuccessToken()
		 */
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}

	/**
	 * Creates the partitioner and sets up the appropriate rules.
	 */
	public AsmPartitionScanner() {
		super();
		
		IToken comment= new Token(ICPartitions.C_MULTILINE_COMMENT);
		IToken single_comment= new Token(ICPartitions.C_SINGLE_LINE_COMMENT);
		IToken string= new Token(ICPartitions.C_STRING);
		// IToken skip= new Token(SKIP);



		List rules= new ArrayList();


		// Minimize the number of rules, since we have duplicate rules 
		// in the CCodeScanner...


		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", single_comment)); //$NON-NLS-1$
		rules.add(new EndOfLineRule("#", single_comment)); //$NON-NLS-1$

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		
		EmptyCommentRule wordRule= new EmptyCommentRule(comment);
		rules.add(wordRule);


		// Add rules for multi-line comments.
		rules.add(new MultiLineRule("/*", "*/", comment)); //$NON-NLS-1$ //$NON-NLS-2$


		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
