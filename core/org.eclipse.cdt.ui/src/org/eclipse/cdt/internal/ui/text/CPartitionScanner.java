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
import java.util.List;

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

import org.eclipse.cdt.ui.text.ICPartitions;


/**
 * This scanner is not actually use in the code it was relace by
 * FastCPartitionScanner which was faster.  We keep this around
 * as a reference.
 */
public class CPartitionScanner extends RuleBasedPartitionScanner implements ICPartitions {

	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {

		/*
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		/*
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
		 * @param successToken
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
	public CPartitionScanner() {
		super();
		
		IToken comment= new Token(C_MULTI_LINE_COMMENT);
		IToken single_comment= new Token(C_SINGLE_LINE_COMMENT);
		IToken string= new Token(C_STRING);
		IToken character = new Token(C_CHARACTER);
		List rules= new ArrayList();

		// Minimize the number of rules, since we have duplicate rules 
		// in the CCodeScanner...


		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", single_comment, '\\', true)); //$NON-NLS-1$


		// Add rule for string constants.
		rules.add(new SingleLineRule("\"", "\"", string, '\\', false, true)); //$NON-NLS-1$ //$NON-NLS-2$
		// Add rule for character constants
		rules.add(new SingleLineRule("'", "'", character, '\\')); //$NON-NLS-1$ //$NON-NLS-2$

		// Add special case word rule.
		EmptyCommentRule wordRule= new EmptyCommentRule(comment);
		rules.add(wordRule);


		// Add rules for multi-line comments.
		rules.add(new MultiLineRule("/*", "*/", comment)); //$NON-NLS-1$ //$NON-NLS-2$

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
