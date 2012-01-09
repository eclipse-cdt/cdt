/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

import com.ibm.icu.text.BreakIterator;
import java.text.CharacterIterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

/**
 * A double-click strategy for words starting with specified tag markers.
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
/*
 * Cloned and tweaked from JDT's javadoc double click strategy
 */
public class GenericTagDoubleClickStrategy implements ITextDoubleClickStrategy {
	protected char[] fTagMarkers;
	
	/**
	 * A double-click strategy that additionally understands tag markers form part of a
	 * selectable word.
	 * @param tagMarkers the set of characters to additionally consider part of a word
	 */
	public GenericTagDoubleClickStrategy(char[] tagMarkers) {
		fTagMarkers= new char[tagMarkers.length];
		System.arraycopy(tagMarkers, 0, fTagMarkers, 0, tagMarkers.length);
	}
	
	/**
	 * Implements a character iterator that works directly on
	 * instances of <code>IDocument</code>. Used to collaborate with
	 * the break iterator.
	 *
	 * @see IDocument
	 * @since 2.0
	 */
	static class DocumentCharacterIterator implements CharacterIterator {

		/** Document to iterate over. */
		private IDocument fDocument;
		/** Start offset of iteration. */
		private int fOffset= -1;
		/** End offset of iteration. */
		private int fEndOffset= -1;
		/** Current offset of iteration. */
		private int fIndex= -1;

		/** Creates a new document iterator. */
		public DocumentCharacterIterator() {
		}

		/**
		 * Configures this document iterator with the document section to be visited.
		 *
		 * @param document the document to be iterated
		 * @param iteratorRange the range in the document to be iterated
		 */
		public void setDocument(IDocument document, IRegion iteratorRange) {
			fDocument= document;
			fOffset= iteratorRange.getOffset();
			fEndOffset= fOffset + iteratorRange.getLength();
		}

		/*
		 * @see CharacterIterator#first()
		 */
		@Override
		public char first() {
			fIndex= fOffset;
			return current();
		}

		/*
		 * @see CharacterIterator#last()
		 */
		@Override
		public char last() {
			fIndex= fOffset < fEndOffset ? fEndOffset -1 : fEndOffset;
			return current();
		}

		/*
		 * @see CharacterIterator#current()
		 */
		@Override
		public char current() {
			if (fOffset <= fIndex && fIndex < fEndOffset) {
				try {
					return fDocument.getChar(fIndex);
				} catch (BadLocationException x) {
				}
			}
			return DONE;
		}

		/*
		 * @see CharacterIterator#next()
		 */
		@Override
		public char next() {
			++fIndex;
			int end= getEndIndex();
			if (fIndex >= end) {
				fIndex= end;
				return DONE;
			}
			return current();
		}

		/*
		 * @see CharacterIterator#previous()
		 */
		@Override
		public char previous() {
			if (fIndex == fOffset)
				return DONE;

			if (fIndex > fOffset)
				-- fIndex;

			return current();
		}

		/*
		 * @see CharacterIterator#setIndex(int)
		 */
		@Override
		public char setIndex(int index) {
			fIndex= index;
			return current();
		}

		/*
		 * @see CharacterIterator#getBeginIndex()
		 */
		@Override
		public int getBeginIndex() {
			return fOffset;
		}

		/*
		 * @see CharacterIterator#getEndIndex()
		 */
		@Override
		public int getEndIndex() {
			return fEndOffset;
		}

		/*
		 * @see CharacterIterator#getIndex()
		 */
		@Override
		public int getIndex() {
			return fIndex;
		}

		/*
		 * @see CharacterIterator#clone()
		 */
		@Override
		public Object clone() {
			DocumentCharacterIterator i= new DocumentCharacterIterator();
			i.fDocument= fDocument;
			i.fIndex= fIndex;
			i.fOffset= fOffset;
			i.fEndOffset= fEndOffset;
			return i;
		}
	}


	/**
	 * The document character iterator used by this strategy.
	 * @since 2.0
	 */
	private DocumentCharacterIterator fDocIter= new DocumentCharacterIterator();

	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(org.eclipse.jface.text.ITextViewer)
	 */
	@Override
	public void doubleClicked(ITextViewer text) {

		int position= text.getSelectedRange().x;

		if (position < 0)
			return;

		IRegion word= getWordRegion(text.getDocument(), position);

		if (word != null)
			text.setSelectedRange(word.getOffset(), word.getLength());
	}

	/**
	 * Returns a region describing the word around <code>position</code>.
	 *
	 * @param document the document
	 * @param position the offset around which to return the word
	 * @return the word's region, or <code>null</code> for no selection
	 */
	private IRegion getWordRegion(IDocument document, int position) {
		try {

			IRegion line= document.getLineInformationOfOffset(position);
			if (position == line.getOffset() + line.getLength())
				return null;

			fDocIter.setDocument(document, line);

			BreakIterator breakIter= BreakIterator.getWordInstance();
			breakIter.setText(fDocIter);

			int start= breakIter.preceding(position);
			if (start == BreakIterator.DONE)
				start= line.getOffset();

			int end= breakIter.following(position);
			if (end == BreakIterator.DONE)
				end= line.getOffset() + line.getLength();

			if (breakIter.isBoundary(position)) {
				if (end - position > position- start)
					start= position;
				else
					end= position;
			}

			if (start > 0 && isTagMarker(document.getChar(start - 1)) && Character.isJavaIdentifierPart(document.getChar(start))
					&& (start == 1 || Character.isWhitespace(document.getChar(start - 2)) || document.getChar(start - 2) == '{')) {
				// double click after @ident
				start--;
			} else if (end == position && end == start + 1 && end < line.getOffset() + line.getLength() && document.getChar(end) == '@') {
				// double click before " @ident"
				return getWordRegion(document, position + 1);
			}

			if (start == end)
				return null;
			return new Region(start, end - start);

		} catch (BadLocationException x) {
			return null;
		}
	}


	protected boolean isTagMarker(char c) {
		for(int i=0; i<fTagMarkers.length; i++) {
			if(c == fTagMarkers[i])
				return true;
		}
		return false;
	}
}
