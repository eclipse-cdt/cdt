/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.parser.AbstractPersistableProblem;

/**
 * Spelling problem to be accepted by problem requesters.
 */
public class CoreSpellingProblem extends AbstractPersistableProblem {
	// spelling 'marker type' name. Only virtual as spelling problems are never persisted in markers. 
	// marker type is used in the quickFixProcessor extension point
	public static final String MARKER_TYPE= "org.eclipse.cdt.internal.spelling"; //$NON-NLS-1$
	
	/** The end offset of the problem */
	private int fSourceEnd= 0;

	/** The line number of the problem */
	private int fLineNumber= 1;

	/** The start offset of the problem */
	private int fSourceStart= 0;

	/** The description of the problem */
	private String fMessage;

	/** The misspelled word */
	private String fWord;

	/** Was the word found in the dictionary? */
	private boolean fMatch;

	/** Does the word start a new sentence? */
	private boolean fSentence;

	/** The associated document */
	private IDocument fDocument;

	/** The originating file name */
	private String fOrigin;

	/**
	 * Initialize with the given parameters.
	 *
	 * @param start the start offset
	 * @param end the end offset
	 * @param line the line
	 * @param message the message
	 * @param word the word
	 * @param match <code>true</code> iff the word was found in the dictionary
	 * @param sentence <code>true</code> iff the word starts a sentence
	 * @param document the document
	 * @param origin the originating file name
	 */
	public CoreSpellingProblem(int start, int end, int line, String message, String word, boolean match, boolean sentence, IDocument document, String origin) {
		super();
		fSourceStart= start;
		fSourceEnd= end;
		fLineNumber= line;
		fMessage= message;
		fWord= word;
		fMatch= match;
		fSentence= sentence;
		fDocument= document;
		fOrigin= origin;
	}
	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getArguments()
	 */
	@Override
	public String[] getArguments() {
		String prefix= ""; //$NON-NLS-1$
		String postfix= ""; //$NON-NLS-1$

		try {
			IRegion line= fDocument.getLineInformationOfOffset(fSourceStart);

			prefix= fDocument.get(line.getOffset(), fSourceStart - line.getOffset());
			postfix= fDocument.get(fSourceEnd + 1, line.getOffset() + line.getLength() - fSourceEnd);
		} catch (BadLocationException exception) {
			// Do nothing
		}
		return new String[] { fWord, prefix, postfix, fSentence ? Boolean.toString(true) : Boolean.toString(false), fMatch ? Boolean.toString(true) : Boolean.toString(false) };
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getID()
	 */
	@Override
	public int getID() {
		return CSpellingReconcileStrategy.SPELLING_PROBLEM_ID;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getMessage()
	 */
	@Override
	public String getMessage() {
		return fMessage;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IProblem#getMessageWithLocation()
	 */
	@Override
	public String getMessageWithLocation() {
		return NLS.bind(Messages.Spelling_msgWithLocation, new Object[] {fMessage, fOrigin, fLineNumber});
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getOriginatingFileName()
	 */
	@Override
	public char[] getOriginatingFileName() {
		return fOrigin.toCharArray();
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceEnd()
	 */
	@Override
	public int getSourceEnd() {
		return fSourceEnd;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceLineNumber()
	 */
	@Override
	public int getSourceLineNumber() {
		return fLineNumber;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#getSourceStart()
	 */
	@Override
	public int getSourceStart() {
		return fSourceStart;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#isError()
	 */
	@Override
	public boolean isError() {
		return false;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#isWarning()
	 */
	@Override
	public boolean isWarning() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#setSourceStart(int)
	 */
	public void setSourceStart(int sourceStart) {
		fSourceStart= sourceStart;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#setSourceEnd(int)
	 */
	public void setSourceEnd(int sourceEnd) {
		fSourceEnd= sourceEnd;
	}

	/*
	 * @see org.eclipse.cdt.core.parser.IProblem#setSourceLineNumber(int)
	 */
	public void setSourceLineNumber(int lineNumber) {
		fLineNumber= lineNumber;
	}
	
	/*
	 * @see org.eclipse.cdt.core.parser.CategorizedProblem#getMarkerType()
	 */
	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}
	
	@Override
	public boolean checkCategory(int bitmask) {
		return (getID() & bitmask) != 0;
	}
}
