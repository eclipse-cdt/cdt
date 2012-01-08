/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;

import org.eclipse.cdt.internal.ui.text.IHtmlTagConstants;

/**
 * Proposal to correct the incorrectly spelled word.
 */
public class WordCorrectionProposal implements ICCompletionProposal {
	/** The invocation context */
	private final IInvocationContext fContext;

	/** The length in the document */
	private final int fLength;

	/** The line where to apply the correction */
	private final String fLine;

	/** The offset in the document */
	private final int fOffset;

	/** The relevance of this proposal */
	private final int fRelevance;

	/** The word to complete */
	private final String fWord;

	/**
	 * Creates a new word correction proposal.
	 *
	 * @param word the corrected word
	 * @param arguments the problem arguments associated with the spelling problem
	 * @param offset the offset in the document where to apply the proposal
	 * @param length the length in the document to apply the proposal
	 * @param context the invocation context for this proposal
	 * @param relevance the relevance of this proposal
	 */
	public WordCorrectionProposal(final String word, final String[] arguments, final int offset,
			final int length, final IInvocationContext context, final int relevance) {
		fWord= Character.isUpperCase(arguments[0].charAt(0)) ?
				Character.toUpperCase(word.charAt(0)) + word.substring(1) : word;

		fOffset= offset;
		fLength= length;
		fContext= context;
		fRelevance= relevance;

		final StringBuffer buffer= new StringBuffer(80);

		buffer.append("...<br>"); //$NON-NLS-1$
		buffer.append(getHtmlRepresentation(arguments[1]));
		buffer.append("<b>"); //$NON-NLS-1$
		buffer.append(getHtmlRepresentation(fWord));
		buffer.append("</b>"); //$NON-NLS-1$
		buffer.append(getHtmlRepresentation(arguments[2]));
		buffer.append("<br>..."); //$NON-NLS-1$

		fLine= buffer.toString();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final void apply(final IDocument document) {
		try {
			document.replace(fOffset, fLength, fWord);
		} catch (BadLocationException exception) {
			// Do nothing
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return fLine;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	@Override
	public final IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		return NLS.bind(Messages.Spelling_correct_label, fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_RENAME);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	@Override
	public final int getRelevance() {
		return fRelevance;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final Point getSelection(final IDocument document) {
		int offset= fContext.getSelectionOffset();
		int length= fContext.getSelectionLength();

		final int delta= fWord.length() - fLength;
		if (offset <= fOffset && offset + length >= fOffset) {
			length += delta;
		} else if (offset > fOffset && offset + length > fOffset + fLength) {
			offset += delta;
			length -= delta;
		} else {
			length += delta;
		}

		return new Point(offset, length);
	}
	
	/**
	 * Returns the html representation of the specified string.
	 *
	 * @param string The string to return the html representation for
	 * @return The html representation for the string
	 */
	public static String getHtmlRepresentation(final String string) {
		final int length= string.length();
		final StringBuffer buffer= new StringBuffer(string);

		for (int offset= length - 1; offset >= 0; offset--) {
			for (int index= 0; index < IHtmlTagConstants.HTML_ENTITY_CHARACTERS.length; index++) {
				if (string.charAt(offset) == IHtmlTagConstants.HTML_ENTITY_CHARACTERS[index]) {
					buffer.replace(offset, offset + 1, String.valueOf(IHtmlTagConstants.HTML_ENTITY_CODES[index]));
					break;
				}
			}
		}
		return buffer.toString();
	}

	@Override
	public String getIdString() {
		return fWord;
	}
}
