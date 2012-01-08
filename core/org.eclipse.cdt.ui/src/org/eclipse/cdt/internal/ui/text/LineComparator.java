/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.cdt.ui.CUIPlugin;


/**
 * This implementation of <code>IRangeComparator</code> compares lines of a document.
 * The lines are compared using a DJB hash function.
 * 
 * @since 5.1
 */
public class LineComparator implements IRangeComparator {
	private static final long UNKNOWN_HASH = Long.MIN_VALUE;
	private final IDocument fDocument;
	private final long[] fHashes;

    /**
	 * Create a line comparator for the given document.
	 * 
	 * @param document
	 */
	public LineComparator(IDocument document) {
		fDocument= document;

		fHashes= new long[fDocument.getNumberOfLines()];
		for (int i = 0; i < fHashes.length; i++) {
			fHashes[i] = UNKNOWN_HASH;
		}
    }

	/*
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#getRangeCount()
     */
    @Override
	public int getRangeCount() {
        return fDocument.getNumberOfLines();
    }

    /*
     * @see org.eclipse.compare.rangedifferencer.IRangeComparator#rangesEqual(int, org.eclipse.compare.rangedifferencer.IRangeComparator, int)
     */
    @Override
	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
		try {
			return getHash(thisIndex) == ((LineComparator) other).getHash(otherIndex);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
			return false;
		}
    }

	/*
	 * @see org.eclipse.compare.rangedifferencer.IRangeComparator#skipRangeComparison(int, int, org.eclipse.compare.rangedifferencer.IRangeComparator)
	 */
	@Override
	public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
		return false;
	}

	/**
	 * @param line the number of the line in the document to get the hash for
	 * @return the hash of the line
	 * @throws BadLocationException if the line number is invalid
	 */
	private int getHash(int line) throws BadLocationException {
		long hash= fHashes[line];
		if (hash == UNKNOWN_HASH) {
			IRegion lineRegion= fDocument.getLineInformation(line);
			String lineContents= fDocument.get(lineRegion.getOffset(), lineRegion.getLength());
			hash= computeDJBHash(lineContents);
			fHashes[line] = hash;
		}

		return (int) hash;
	}

	/**
	 * Compute a hash using the DJB hash algorithm
	 * 
	 * @param string the string for which to compute a hash
	 * @return the DJB hash value of the string
	 */
	private int computeDJBHash(String string) {
		int hash= 5381;
		int len= string.length();
		for (int i= 0; i < len; i++) {
			char ch= string.charAt(i);
			hash= (hash << 5) + hash + ch;
		}

		return hash;
    }
}
