/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

}

