/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;

public class WordPartDetector {
	IDocument document;
	int offset;
	String wordPart = ""; //$NON-NLS-1$

	/**
	 * WordPartDetector.
	 * @param viewer is a text viewer
	 * @param documentOffset
	 */
	public WordPartDetector(ITextViewer viewer, int documentOffset) {
		this(viewer.getDocument(), documentOffset);
	}

	/**
	 * 
	 * @param doc
	 * @param documentOffset
	 */
	public WordPartDetector(IDocument doc, int documentOffset) {
		document = doc;
		offset = documentOffset - 1;
		int endOffset = documentOffset;
		try {
			IRegion region = doc.getLineInformationOfOffset(documentOffset);
			int top = region.getOffset();
			int bottom = region.getLength() + top;
			while (offset >= top && isMakefileLetter(doc.getChar(offset))) {
				offset--;
			}
			while (endOffset < bottom && isMakefileLetter(doc.getChar(endOffset))) {
				endOffset++;
			}
			//we've been one step too far : increase the offset
			offset++;
			wordPart = doc.get(offset, endOffset - offset);
		} catch (BadLocationException e) {
			// do nothing
		}
	}

	public static boolean inMacro(ITextViewer viewer, int offset) {
		return inMacro(viewer.getDocument(), offset);
	}

	public static boolean inMacro(IDocument document, int offset) {
		boolean isMacro = false;
		// Try to figure out if we are in a Macro.
		try {
			for (int index = offset - 1; index >= 0; index--) {
				char c;
				c = document.getChar(index);
				if (c == '$') {
					isMacro = true;
					break;
				} else if (Character.isWhitespace(c) || c == ')' || c == '}') {
					break;
				}
			}
		} catch (BadLocationException e) {
		}
		return isMacro;
	}

	/**
	 * Quick check if the cursor sits on an include directive line.
	 * 
	 * @return {@code true} if the cursor is located on include line, {@code false} otherwise.
	 */
	public boolean isIncludeDirective() {
		boolean isIncludeDirective = false;
		try {
			int lineNumber = document.getLineOfOffset(offset);
			String line = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber));
			String firstWord = findWord(line.trim(), 1);
			isIncludeDirective = isIncludeKeyword(firstWord);
		} catch (BadLocationException e) {
			MakeUIPlugin.log(e);
		}
		return isIncludeDirective;
	}

	/**
	 * Gets include file name under the cursor.
	 * 
	 * @return include file name to which the cursor location corresponds.
	 */
	public String getIncludedFile() {
		String inc = null;
		try {
			int lineNumber = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(lineNumber);
			String line = document.get(lineOffset, document.getLineLength(lineNumber));

			int position = offset -lineOffset;
			inc = findWord(line, position);
			if (isIncludeKeyword(inc)) {
				inc = findWord(line, line.indexOf(inc) + inc.length() + 1);
			}
		} catch (BadLocationException e) {
			MakeUIPlugin.log(e);
		}
		return inc;
	}

	/**
	 * Find word located in the given position. A word is defined here as a sequence of non-space characters.
	 */
	private static String findWord(String line, int position) {
		String firstHalf;
		try {
			firstHalf = line.substring(0, position);
		} catch (IndexOutOfBoundsException e) {
			firstHalf = line;
		}
		String secondHalf;
		try {
			secondHalf = line.substring(position);
		} catch (IndexOutOfBoundsException e) {
			secondHalf = ""; //$NON-NLS-1$
		}
		int startIndex = firstHalf.lastIndexOf(' ') + 1;
		int firstHalfLen = firstHalf.length();
		int endIndex = firstHalfLen + secondHalf.indexOf(' ');
		if (endIndex < firstHalfLen) {
			endIndex = line.length();
		}
		// trim() gets rid of trailing end of line if captured
		String word = line.substring(startIndex, endIndex).trim();
		return word;
	}

	/**
	 * Check if the string is include keyword.
	 */
	private static boolean isIncludeKeyword(String inc) {
		@SuppressWarnings("nls")
		boolean isKeyword = "include".equals(inc) || "-include".equals(inc) || "sinclude".equals(inc);
		return isKeyword;
	}

	@Override
	public String toString() {
		return wordPart;
	}

	public int getOffset() {
		return offset;
	}

	boolean isMakefileLetter(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '.';
	}
}
