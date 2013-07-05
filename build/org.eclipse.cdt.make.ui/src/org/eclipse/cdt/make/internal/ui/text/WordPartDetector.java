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

import org.eclipse.cdt.make.internal.core.makefile.gnu.GNUMakefileUtil;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class WordPartDetector {
	private IDocument document;
	private int offset;
	private String wordPart = ""; //$NON-NLS-1$
	private WORDPART_TYPE type = WORDPART_TYPE.UNDETERMINED;

	private enum WORDPART_TYPE {
		MACRO,
		INCLUDE,
		UNDETERMINED,
	}

	/**
	 * @param doc - text document.
	 * @param documentOffset - cursor location in the document.
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
			MakeUIPlugin.log(e);
		}
		// Try to figure out if the cursor is on a macro.
		try {
			for (int index = offset - 1; index >= 0; index--) {
				char c;
				c = document.getChar(index);
				if (c == '$') {
					type = WORDPART_TYPE.MACRO;
					return;
				} else if (Character.isWhitespace(c) || c == ')' || c == '}') {
					break;
				}
			}
		} catch (BadLocationException e) {
			MakeUIPlugin.log(e);
		}

		// Try to figure out if the cursor is on an include directive.
		try {
			int lineNumber = document.getLineOfOffset(offset);
			String line = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber));
			if (GNUMakefileUtil.isInclude(line)) {
				type = WORDPART_TYPE.INCLUDE;
				int lineOffset = document.getLineOffset(lineNumber);

				int position = offset - lineOffset;
				String before = line.substring(0, position);
				if (!(before + '.').trim().contains(" ")) { //$NON-NLS-1$
					// the cursor is on the first word which is "include" keyword
					// so find the second word which is the first include file
					String sub = line.substring(line.indexOf(' ', position)).trim();
					wordPart = sub.substring(0, sub.indexOf(' ')).trim();
				} else {
					wordPart = findWord(line, position);
				}
				return;
			}
		} catch (BadLocationException e) {
			MakeUIPlugin.log(e);
		}

	}

	/**
	 * Check if the cursor is in macro.
	 * 
	 * @return {@code true} if the cursor is located in macro, {@code false} otherwise.
	 */
	public boolean isMacro() {
		return type == WORDPART_TYPE.MACRO;
	}

	/**
	 * Check if the cursor sits on an include directive line.
	 * 
	 * @return {@code true} if the cursor is located on include line, {@code false} otherwise.
	 */
	public boolean isIncludeDirective() {
		return type == WORDPART_TYPE.INCLUDE;
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

	@Override
	public String toString() {
		return wordPart;
	}

	public String getName() {
		return wordPart;
	}

	public int getOffset(){
		return offset;
	}

	private boolean isMakefileLetter(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '.';
	}
}
