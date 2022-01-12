/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class PDAContentAssistProcessor implements IContentAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		int index = offset - 1;
		StringBuilder prefix = new StringBuilder();
		IDocument document = viewer.getDocument();
		while (index > 0) {
			try {
				char prev = document.getChar(index);
				if (Character.isWhitespace(prev)) {
					break;
				}
				prefix.insert(0, prev);
				index--;
			} catch (BadLocationException e) {
			}
		}

		List<ICompletionProposal> proposals = new ArrayList<>();
		String[] keywords = PDAScanner.fgKeywords;
		if (prefix.length() > 0) {
			String word = prefix.toString();
			for (int i = 0; i < keywords.length; i++) {
				String keyword = keywords[i];
				if (keyword.startsWith(word) && word.length() < keyword.length()) {
					proposals.add(new CompletionProposal(keyword + " ", index + 1, offset - (index + 1),
							keyword.length() + 1));
				}
			}
		} else {
			// propose all keywords
			for (int i = 0; i < keywords.length; i++) {
				String keyword = keywords[i];
				proposals.add(new CompletionProposal(keyword + " ", offset, 0, keyword.length() + 1));
			}
		}
		if (!proposals.isEmpty()) {
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
