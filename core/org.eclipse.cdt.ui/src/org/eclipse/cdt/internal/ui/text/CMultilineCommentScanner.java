package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.cdt.internal.ui.text.eclipse2.Token;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.util.Assert;


/**
 * Implementation of <code>IRule</code> for C code scanning.
 * Optimized to partition C documents
 * Is is capable of detecting a pattern which begins with a given starting
 * sequence and ends with a given ending sequence. If the ending sequence is
 * not specified, it can be either end of line, end or file, or both. Additionally,
 * the pattern can be constrained to begin in a certain column.
 */
public class CMultilineCommentScanner implements IRule {
	
	protected static final int UNDEFINED= -1;

	/** The token to be returned on success */
	protected IToken fToken;

	/** The pattern's escape character */
	protected char fEscapeCharacter;
	/** Indicates whether end of line termines the pattern */
	protected boolean fBreaksOnEOL;

	/**
	 * Creates a rule for the given starting and ending sequence.
	 * When these sequences are detected the rule will return the specified token.
	 * Alternatively, the sequence can also be ended by the end of the line.
	 * Any character which follows the given escapeCharacter will be ignored.
	 *
	 * @param startSequence the pattern's start sequence
	 * @param endSequence the pattern's end sequence, <code>null</code> is a legal value
	 * @param token the token which will be returned on success
	 * @param escapeCharacter any character following this one will be ignored
	 * @param indicates whether the end of the line also termines the pattern
	 */
	public CMultilineCommentScanner(IToken token, char escapeCharacter, boolean breaksOnEOL) {
		Assert.isNotNull(token);
		
		fToken= token;
		fEscapeCharacter= escapeCharacter;
		fBreaksOnEOL= breaksOnEOL;
	}

	/**
	 * Returns whether the end sequence was detected. As the pattern can be considered 
	 * ended by a line delimiter, the result of this method is <code>true</code> if the 
	 * rule breaks on the end  of the line, or if the EOF character is read.
	 *
	 * @param scanner the character scanner to be used
	 * @return <code>true</code> if the end sequence has been detected
	 */
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		int c;
		//char[][] delimiters= scanner.getLegalLineDelimiters();
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			if (c == fEscapeCharacter) {
				// Skip the escaped character.
				scanner.read();
			} else if (c == '*') {
				c = scanner.read();
				if(c == '/') {
					return true;
				}
				scanner.unread();
				// Check if the specified end sequence has been found.
			//} else if (fBreaksOnEOL) {
				// Check for end of line since it can be used to terminate the pattern.
				//for (int i= 0; i < delimiters.length; i++) {
				//	if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], false))
				//		return true;
				//}
			}
		}
		scanner.unread();
		return true;
	}
	/*
	 * @see IRule#evaluate
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		
		int c= scanner.read();
		
		if (c == '/') {
			if((c= scanner.read()) == '*') {
				endSequenceDetected(scanner);
				return fToken;
			}
			scanner.unread();
		}
		
		scanner.unread();
		return Token.UNDEFINED;
	}
	/**
	 * Returns whether the next characters to be read by the character scanner
	 * are an exact match with the given sequence. No escape characters are allowed 
	 * within the sequence. If specified the sequence is considered to be found
	 * when reading the EOF character.
	 *
	 * @param scanner the character scanner to be used
	 * @param sequence the sequence to be detected
	 * @param eofAllowed indicated whether EOF terminates the pattern
	 * @return <code>true</code> if the given sequence has been detected
	 */
	protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
		for (int i= 1; i < sequence.length; i++) {
			int c= scanner.read();
			if (c == ICharacterScanner.EOF && eofAllowed) {
				return true;
			} else if (c != sequence[i]) {
				// Non-matching character detected, rewind the scanner back to the start.
				scanner.unread();
				for (int j= i-1; j > 0; j--)
					scanner.unread();
				return false;
			}
		}
		
		return true;
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
		//fColumn= column;
	}
}


