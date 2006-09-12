/*******************************************************************************
 * Copyright (c) 2000 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import org.eclipse.cdt.ui.text.ICPartitions;

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

	public CPairMatcher(char[] pairs) {
		fPairs = pairs;
	}

	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#match(org.eclipse.jface.text.IDocument, int)
	 */
	public IRegion match(IDocument document, int offset) {

		fOffset = offset;

		if (fOffset < 0)
			return null;

		fDocument = document;

		if (matchPairsAt() && fStartPos != fEndPos)
			return new Region(fStartPos, fEndPos - fStartPos + 1);

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#getAnchor()
	 */
	public int getAnchor() {
		return fAnchor;
	}

	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
	 */
	public void clear() {
	}

	/*
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcher#dispose()
	 */
	public void dispose() {
		clear();
		fDocument = null;
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

			// search for opening peer character next to the activation point
			for (i= 0; i < fPairs.length; i= i + 2) {
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
		}

		return false;
	}
	

	protected int searchForClosingPeer(int offset, char openingPeer, char closingPeer, IDocument document) throws BadLocationException {
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false));
		return scanner.findClosingPeer(offset + 1, openingPeer, closingPeer);
	}

	protected int searchForOpeningPeer(int offset, char openingPeer, char closingPeer, IDocument document) throws BadLocationException {
		CHeuristicScanner scanner= new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, TextUtilities.getContentType(document, ICPartitions.C_PARTITIONING, offset, false));
		int peer= scanner.findOpeningPeer(offset - 1, openingPeer, closingPeer);
		if (peer == CHeuristicScanner.NOT_FOUND)
			return -1;
		return peer;
	}
}
