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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;

import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;

/**
 * Proposal to ignore the word during the current editing session.
 */
public class WordIgnoreProposal implements ICCompletionProposal {
	/** The invocation context */
	private IInvocationContext fContext;

	/** The word to ignore */
	private String fWord;

	/**
	 * Creates a new spell ignore proposal.
	 *
	 * @param word       The word to ignore
	 * @param context    The invocation context
	 */
	public WordIgnoreProposal(final String word, final IInvocationContext context) {
		fWord= word;
		fContext= context;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final void apply(final IDocument document) {
        final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
        final ISpellChecker checker= engine.getSpellChecker();
        if (checker != null) {
            checker.ignoreWord(fWord);
            if (fContext instanceof IQuickAssistInvocationContext) {
                ISourceViewer sourceViewer= ((IQuickAssistInvocationContext) fContext).getSourceViewer();
                if (sourceViewer != null) {
                    SpellingProblem.removeAll(sourceViewer, fWord);
                }
            }
        }
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return NLS.bind(Messages.Spelling_ignore_info, WordCorrectionProposal.getHtmlRepresentation(fWord));
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
		return NLS.bind(Messages.Spelling_ignore_label, fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_NLS_NEVER_TRANSLATE);
	}
	/*
	 * @see org.eclipse.cdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	@Override
	public final int getRelevance() {
		return Integer.MIN_VALUE + 1;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getSelectionOffset(), fContext.getSelectionLength());
	}

	@Override
	public String getIdString() {
		return fWord;
	}
}
