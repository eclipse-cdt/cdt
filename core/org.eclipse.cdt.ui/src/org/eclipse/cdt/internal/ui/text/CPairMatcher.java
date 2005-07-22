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

import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

/**
 * Helper class for match pairs of characters.
 */
public class CPairMatcher implements ICharacterPairMatcher {

	protected char[] fPairs;
	protected IDocument fDocument;
	protected int fOffset;

	protected int fStartPos;
	protected int fEndPos;
	protected int fAnchor;

	protected CCodeReader fReader = new CCodeReader();

	public CPairMatcher(char[] pairs) {
		fPairs = pairs;
	}

	public IRegion match(IDocument document, int offset) {

		fOffset = offset;

		if (fOffset < 0)
			return null;

		fDocument = document;

		if (matchPairsAt() && fStartPos != fEndPos)
			return new Region(fStartPos, fEndPos - fStartPos + 1);

		return null;
	}

	public int getAnchor() {
		return fAnchor;
	}

	/*
		 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
		 */
	public void clear() {
		if (fReader != null) {
			try {
				fReader.close();
			} catch (IOException x) {
				// ignore
			}
		}
	}

	public void dispose() {
		clear();
		fDocument = null;
		fReader = null;
	}
	protected boolean matchPairsAt() {

		int i;
		int pairIndex1= fPairs.length;
		int pairIndex2= fPairs.length;

		fStartPos= -1;
		fEndPos= -1;

		// get the chars preceding and following the start position
		try {

			char prevChar= fDocument.getChar(Math.max(fOffset - 1, 0));
// modified behavior for http://dev.eclipse.org/bugs/show_bug.cgi?id=16879			
//			char nextChar= fDocument.getChar(fOffset);

			// search for opening peer character next to the activation point
			for (i= 0; i < fPairs.length; i= i + 2) {
//				if (nextChar == fPairs[i]) {
//					fStartPos= fOffset;
//					pairIndex1= i;
//				} else 
				if (prevChar == fPairs[i]) {
					fStartPos= fOffset - 1;
					pairIndex1= i;
				}
			}
			
			// search for closing peer character next to the activation point
			for (i= 1; i < fPairs.length; i= i + 2) {
				if (prevChar == fPairs[i]) {
					fEndPos= fOffset - 1;
					pairIndex2= i;
				} 
//				else if (nextChar == fPairs[i]) {
//					fEndPos= fOffset;
//					pairIndex2= i;
//				}
			}

			if (fEndPos > -1) {
				fAnchor= RIGHT;
				fStartPos= searchForOpeningPeer(fEndPos, fPairs[pairIndex2 - 1], fPairs[pairIndex2], fDocument);
				if (fStartPos > -1)
					return true;
				fEndPos= -1;
			}	else if (fStartPos > -1) {
				fAnchor= LEFT;
				fEndPos= searchForClosingPeer(fStartPos, fPairs[pairIndex1], fPairs[pairIndex1 + 1], fDocument);
				if (fEndPos > -1)
					return true;
				fStartPos= -1;
			}

		} catch (BadLocationException x) {
		} catch (IOException x) {
		}

		return false;
	}
	


//	protected boolean matchPairsAt() {
//
//		int i;
//		int pairIndex1 = fPairs.length;
//		int pairIndex2 = fPairs.length;
//
//		fStartPos = -1;
//		fEndPos = -1;
//
//		// get the chars preceding and following the start position
//		try {
//
//			/*
//			 A quick hack to get around the fact that we can't bracket
//			 match on the very first element of a document.  We make the
//			 character to match a null character which is unlikely to match.
//			*/
//			char prevChar = (fOffset > 0) ? fDocument.getChar(fOffset - 1) : '\0';
//			char nextChar = fDocument.getChar(fOffset);
//
//			// search for opening peer character next to the activation point
//			for (i = 0; i < fPairs.length; i = i + 2) {
//				if (nextChar == fPairs[i]) {
//					fStartPos = fOffset;
//					pairIndex1 = i;
//				} else if (prevChar == fPairs[i]) {
//					fStartPos = fOffset - 1;
//					pairIndex1 = i;
//				}
//			}
//
//			// search for closing peer character next to the activation point
//			for (i = 1; i < fPairs.length; i = i + 2) {
//				if (prevChar == fPairs[i]) {
//					fEndPos = fOffset - 1;
//					pairIndex2 = i;
//				} else if (nextChar == fPairs[i]) {
//					fEndPos = fOffset;
//					pairIndex2 = i;
//				}
//			}
//
//			if (fEndPos > -1) {
//				fAnchor = RIGHT;
//				fStartPos = searchForOpeningPeer(fEndPos, fPairs[pairIndex2 - 1], fPairs[pairIndex2], fDocument);
//				if (fStartPos > -1)
//					return true;
//				else
//					fEndPos = -1;
//			} else if (fStartPos > -1) {
//				fAnchor = LEFT;
//				fEndPos = searchForClosingPeer(fStartPos, fPairs[pairIndex1], fPairs[pairIndex1 + 1], fDocument);
//				if (fEndPos > -1)
//					return true;
//				else
//					fStartPos = -1;
//			}
//
//		} catch (BadLocationException x) {
//		} catch (IOException x) {
//		}
//
//		return false;
//	}

	protected int searchForClosingPeer(int offset, int openingPeer, int closingPeer, IDocument document) throws IOException {

		fReader.configureForwardReader(document, offset + 1, document.getLength(), true, true);

		int stack = 1;
		int c = fReader.read();
		while (c != CCodeReader.EOF) {
			if (c == openingPeer && c != closingPeer)
				stack++;
			else if (c == closingPeer)
				stack--;

			if (stack == 0)
				return fReader.getOffset();

			c = fReader.read();
		}

		return -1;
	}

	protected int searchForOpeningPeer(int offset, int openingPeer, int closingPeer, IDocument document) throws IOException {

		fReader.configureBackwardReader(document, offset, true, true);

		int stack = 1;
		int c = fReader.read();
		while (c != CCodeReader.EOF) {
			if (c == closingPeer && c != openingPeer)
				stack++;
			else if (c == openingPeer)
				stack--;

			if (stack == 0)
				return fReader.getOffset();

			c = fReader.read();
		}

		return -1;
	}
}