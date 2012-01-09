/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.RankedWordProposal;

/**
 * Content assist processor to complete words.
 * <strong>Note:</strong> This is currently not supported because the spelling engine
 * cannot return word proposals but only correction proposals.
 */
public final class WordCompletionProposalComputer implements ICompletionProposalComputer {
	/** The prefix rank shift */
	private static final int PREFIX_RANK_SHIFT= 500;

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.jface.text.contentassist.TextContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (contributes()) {
			try {
				IDocument document= context.getDocument();
				final int offset= context.getInvocationOffset();
			
				final IRegion region= document.getLineInformationOfOffset(offset);
				final String content= document.get(region.getOffset(), region.getLength());
			
				int index= offset - region.getOffset() - 1;
				while (index >= 0 && Character.isLetter(content.charAt(index)))
					index--;
			
				final int start= region.getOffset() + index + 1;
				final String candidate= content.substring(index + 1, offset - region.getOffset());
			
				if (candidate.length() > 0) {
					final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
					final ISpellChecker checker= engine.getSpellChecker();
			
					if (checker != null) {
						final List<RankedWordProposal> proposals= new ArrayList<RankedWordProposal>(checker.getProposals(candidate, Character.isUpperCase(candidate.charAt(0))));
						final List<ICompletionProposal> result= new ArrayList<ICompletionProposal>(proposals.size());
			
						for (Object element : proposals) {
							RankedWordProposal word= (RankedWordProposal) element;
							String text= word.getText();
							if (text.startsWith(candidate))
								word.setRank(word.getRank() + PREFIX_RANK_SHIFT);
							
							result.add(new CCompletionProposal(text, start, candidate.length(),
									CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_RENAME), text, word.getRank()) {
								/*
								 * @see org.eclipse.cdt.internal.ui.text.java.JavaCompletionProposal#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
								 */
								@Override
								public boolean validate(IDocument doc, int validate_offset, DocumentEvent event) {
									return offset == validate_offset;
								}
							});
						}
						
						return result;
					}
				}
			} catch (BadLocationException exception) {
				// log & ignore
				CUIPlugin.log(exception);
			}
		}
		return Collections.emptyList();
	}

	private boolean contributes() {
		return SpellingPreferences.isEnabledSpellingContentAssist();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#computeContextInformation(org.eclipse.jface.text.contentassist.TextContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return null; // no error message available
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
	 */
	@Override
	public void sessionStarted() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
	 */
	@Override
	public void sessionEnded() {
	}
}
