/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;


/**
 * This is a helper class for the text editor to be able to determine, given a
 * particular offset in a document, various candidates segments for things like
 * context help, proposals and hovering.
 */
public class CWordFinder {
	
	private static final char CBRACE_L = '{';
	private static final char CBRACE_R = '}';
	private static final char  BRACE_R = ')';
	
	/**
	 * This method determines for a given offset into a given document what the
	 * region is which defines the current word. A word is defined as the set of
	 * non "C" identifiers. So assuming that ! indicated the current cursor
	 * postion: !afunction(int a, int b) --> word = length 0 afunc!tion(int a,
	 * int b) --> word = afunction afunction!(int a, int b) --> word = afunction
	 * afunction(!int a, int b) --> word = length 0 afunction(int a,! int b) -->
	 * word = length 0 afunction(!) --> word = length 0
	 * 
	 * @param document
	 *            The document to be examined
	 * @param offset
	 *            The offset into the document where a word should be
	 *            idendified.
	 * @return The region defining the current word, which may be a region of
	 *         length 0 if the offset is not in a word, or null if there is an
	 *         error accessing the docment data.
	 */
	public static IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;

		try {
			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	/**
	 * This method will determine the region for the name of the function within
	 * which the current offset is contained.
	 * 
	 * @param document
	 *            The document to be examined
	 * @param offset
	 *            The offset into the document where a word should be
	 *            idendified.
	 * @return The region defining the current word, which may be a region of
	 *         length 0 if the offset is not in a function, or null if there is
	 *         an error accessing the docment data.
	 */
	public static IRegion findFunction(IDocument document, int offset) {
		int leftbracket = -1;
		int leftbracketcount = 0;
		int rightbracket = -1;
		int rightbracketcount = 0;
		int functionstart = -1;
		int functionend = -1;

		try {
			int length = document.getLength();
			int pos;
			char c;

			//Find most relevant right bracket from our position
			pos = offset;
			rightbracketcount = leftbracketcount = 0;
			while (pos < length) {
				c = document.getChar(pos);

				if (c == ')') {
					rightbracketcount++;
					if (rightbracketcount >= leftbracketcount) {
						rightbracket = pos;
						break;
					}
				}

				if (c == '(') {
					leftbracketcount++;
				}

				if (c == ';') {
					break;
				}

				pos++;
			}

			if (rightbracket == -1) {
				return new Region(offset, 0);
			}

			//Now backtrack our way from the rightbracket to the left
			pos = rightbracket;
			rightbracketcount = leftbracketcount = 0;
			while (pos >= 0) {
				c = document.getChar(pos);

				if (c == ')') {
					rightbracketcount++;
				}

				if (c == '(') {
					leftbracketcount++;
					if (leftbracketcount >= rightbracketcount) {
						leftbracket = pos;
						break;
					}
				}

				if (c == ';') {
					break;
				}

				pos--;
			}

			if (leftbracket == -1) {
				return new Region(offset, 0);
			}

			//Now work our way to the function name
			pos = leftbracket - 1;
			while (pos >= 0) {
				c = document.getChar(pos);
				if (functionend == -1 && c == ' ') {
					pos--;
					continue;
				}

				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}

				functionstart = pos;
				if (functionend == -1) {
					functionend = pos;
				}

				pos--;
			}
		} catch (BadLocationException x) {
			/* Ignore */
		}

		if (functionstart > -1 && functionend > -1) {
			return new Region(functionstart, functionend - functionstart + 1);
		}

		return null;
	}

	/**
	 * This method will determine whether current offset is contained 
	 * in any function's body or it's outside it. 
	 * 
	 * @param document
	 *            The document to be examined
	 * @param offset
	 *            The offset into the document
	 * @return 
	 *      <code>true</code> if there is no function body around offset 
	 *      <code>false</code> otherwise 
	 */
	public static boolean isGlobal(IDocument document, int offset) {
		try {
			int pos = offset;
			int bracketcount = 0;
			char c;

			//Find left curled bracket from our position
			while (pos > 0) {
				c = document.getChar(pos--);

				if (c == CBRACE_R) {
					bracketcount++; // take into account nested blocks
				} else if (c == CBRACE_L) {
					if (bracketcount-- == 0) {
						do {
							c = document.getChar(pos--);
							if (c == BRACE_R) return false;
						} while (Character.isWhitespace(c));
						// container block seems to be not a function or statement body
						pos++;             // step back one symbol
						bracketcount = 0;  // let's search for upper block
					}
				}
			}
			
		} catch (BadLocationException x) { /* Ignore */	}
		// return true in case of unknown result or exception 
		return true;
	}
	
	/**
	 * Searches for line feed symbols in string.
	 * First met '\r' or '\n' is treated as LF symbol
	 * 
	 * @param s
	 * 			string to search in.
	 * @return  number of LFs met.
	 */
	public static int countLFs (String s) {
		int counter = 0;
		char lf = 0;
		char c;
		for (int i=0; i<s.length(); i++) {
			c = s.charAt(i);
			if (lf == 0) {
				if (c == '\n' || c == '\r') {
					lf = c;
					counter++;
				}
			} else if (lf == c) counter++;
		}
		return counter;
	}

	/**
	 * Checks whether the string contains any C-block delimiters ( { } ) 
	 * 
	 * @param s 
	 * 			text to check
	 * @return  true if curled brace found.
	 */
	public static boolean hasCBraces (String s) {
		if (s.indexOf(CBRACE_L) > -1 || s.indexOf(CBRACE_R) > -1) return true;
		else return false;
	}
}

