/*
 * "The Java Developer's Guide to Eclipse"
 *   by Shavor, D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
 * 
 * (C) Copyright International Business Machines Corporation, 2003. 
 * All Rights Reserved.
 * 
 * Code or samples provided herein are provided without warranty of any kind.
 */
package org.eclipse.cdt.make.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
 
/**
 * Used to scan and detect for SQL keywords  
 */
public class WordPartDetector {
	String wordPart = "";
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
			int bottom = viewer.getBottomIndexEndOffset();
			int top = viewer.getTopIndexStartOffset();
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
	
	/**
	 * Method getString.
	 * @return String
	 */
	public String getString() {
		return wordPart;
	}
	
	public int getOffset() {
		return offset;
	}

	boolean isMakefileLetter(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
}
