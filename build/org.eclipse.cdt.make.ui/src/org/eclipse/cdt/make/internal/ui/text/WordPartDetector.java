/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.make.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
 
/**
 * Used to scan and detect for SQL keywords  
 */
public class WordPartDetector {
	String wordPart = ""; //$NON-NLS-1$
	int offset;

	/**
	 * Method WordPartDetector.
	 * @param viewer is a text viewer 
	 * @param documentOffset into the SQL document
	 */
	public WordPartDetector(ITextViewer viewer, int documentOffset) {
		offset = documentOffset - 1;
		int endOffset = documentOffset;		
		try {
			IDocument doc = viewer.getDocument();
			//int bottom = viewer.getBottomIndexEndOffset();
			//int top = viewer.getTopIndexStartOffset();
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
			wordPart = viewer.getDocument().get(offset, endOffset - offset);
		} catch (BadLocationException e) {
			// do nothing
		}
	}

	public static boolean inMacro(ITextViewer viewer, int offset) {
		boolean isMacro = false;
		IDocument document = viewer.getDocument();
		// Try to figure out if we are in a Macro.
		try {
			for (int index = offset - 1; index >= 0; index--) {
				char c;
				c = document.getChar(index);
				if (c == '$') {
					isMacro = true;
					break;
				} else if (Character.isWhitespace(c)) {
					break;
				}
			}
		} catch (BadLocationException e) {
		}
		return isMacro;
	}


	/**
	 * @return String
	 */
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
