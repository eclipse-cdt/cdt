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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of C identifier syntax rules.
 */
public class CDoubleClickSelector implements ITextDoubleClickStrategy {

	protected ITextViewer fText;
	protected int fPos;
	protected int fStartPos;
	protected int fEndPos;


	protected static char[] fgBrackets= {'{', '}', '(', ')', '[', ']', '"', '"'};

	public CDoubleClickSelector() {
		super();
	}


	/**
	 * @see ITextDoubleClickStrategy#doubleClicked
	 */
	public void doubleClicked(ITextViewer text) {
		fPos= text.getSelectedRange().x;


		if (fPos < 0)
			return;


		fText= text;


		if (!selectBracketBlock())
			selectWord();
	}


	protected boolean matchBracketsAt() {
		char prevChar, nextChar;


		int i;
		int bracketIndex1= fgBrackets.length;
		int bracketIndex2= fgBrackets.length;


		fStartPos= -1;
		fEndPos= -1;

		// get the chars preceding and following the start position
		try {
			IDocument doc= fText.getDocument();


			prevChar= doc.getChar(fPos - 1);
			nextChar= doc.getChar(fPos);


			// is the char either an open or close bracket?
			for (i= 0; i < fgBrackets.length; i= i + 2) {
				if (prevChar == fgBrackets[i]) {
					fStartPos= fPos - 1;
					bracketIndex1= i;
				}
			}
			for (i= 1; i < fgBrackets.length; i= i + 2) {
				if (nextChar == fgBrackets[i]) {
					fEndPos= fPos;
					bracketIndex2= i;
				}
			}


			if (fStartPos > -1 && bracketIndex1 < bracketIndex2) {
				fEndPos= searchForClosingBracket(fStartPos, prevChar, fgBrackets[bracketIndex1 + 1], doc);
				if (fEndPos > -1)
					return true;
				fStartPos= -1;
			} else if (fEndPos > -1) {
				fStartPos= searchForOpenBracket(fEndPos, fgBrackets[bracketIndex2 - 1], nextChar, doc);
				if (fStartPos > -1)
					return true;
				fEndPos= -1;
			}
		} catch (BadLocationException x) {
		}


		return false;
	}


	protected boolean matchWord() {
		IDocument doc= fText.getDocument();
		try {
			int pos= fPos;
			char c;


			while (pos >= 0) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				--pos;
			}
			fStartPos= pos;


			pos= fPos;
			int length= doc.getLength();


			while (pos < length) {
				c= doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			fEndPos= pos;


			return true;


		} catch (BadLocationException x) {
		}
		return false;
	}


	protected int searchForClosingBracket(int startPos, char openBracket, char closeBracket, IDocument doc) throws BadLocationException {
		int stack= 1;
		int closePos= startPos + 1;
		int length= doc.getLength();
		char nextChar;


		while (closePos < length && stack > 0) {
			nextChar= doc.getChar(closePos);
			if (nextChar == openBracket && nextChar != closeBracket)
				stack++;
			else if (nextChar == closeBracket)
				stack--;
			closePos++;
		}


		if (stack == 0)
			return closePos - 1;
		return -1;
	}


	protected int searchForOpenBracket(int startPos, char openBracket, char closeBracket, IDocument doc) throws BadLocationException {
		int stack= 1;
		int openPos= startPos - 1;
		char nextChar;

		while (openPos >= 0 && stack > 0) {
			nextChar= doc.getChar(openPos);
			if (nextChar == closeBracket && nextChar != openBracket)
				stack++;
			else if (nextChar == openBracket)
				stack--;
			openPos--;
		}


		if (stack == 0)
			return openPos + 1;
		return -1;
	}


	protected boolean selectBracketBlock() {
		if (matchBracketsAt()) {
			if (fStartPos == fEndPos)
				fText.setSelectedRange(fStartPos, 0);
			else
				fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);


			return true;
		}
		return false;
	}


	protected void selectWord() {
		if (matchWord()) {
			if (fStartPos == fEndPos)
				fText.setSelectedRange(fStartPos, 0);
			else
				fText.setSelectedRange(fStartPos + 1, fEndPos - fStartPos - 1);
		}
	}
}
