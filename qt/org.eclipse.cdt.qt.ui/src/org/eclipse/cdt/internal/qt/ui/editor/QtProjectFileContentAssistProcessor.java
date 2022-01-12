/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class QtProjectFileContentAssistProcessor implements IContentAssistProcessor {
	private final IContextInformation[] NO_CONTEXTS = {};
	private final ICompletionProposal[] NO_COMPLETIONS = {};

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			IDocument document = viewer.getDocument();
			ArrayList<ICompletionProposal> result = new ArrayList<>();

			// Search the list of keywords (case-insensitive)
			String prefix = lastWord(document, offset).toLowerCase(Locale.ROOT);
			for (QtProjectFileKeyword keyword : QtProjectFileKeyword.values()) {
				if (prefix.isEmpty() || keyword.getKeyword().toLowerCase(Locale.ROOT).startsWith(prefix)) {
					result.add(new CompletionProposal(keyword.getKeyword(), offset - prefix.length(), prefix.length(),
							keyword.getKeyword().length()));
				}
			}
			return result.toArray(new ICompletionProposal[result.size()]);
		} catch (Exception e) {
			Activator.log(e);
			return NO_COMPLETIONS;
		}
	}

	/**
	 * Returns the valid Java identifier in a document immediately before the
	 * given offset.
	 *
	 * @param document
	 *            the document
	 * @param offset
	 *            the offset at which to start looking
	 * @return the Java identifier preceding this location or a blank string if
	 *         none
	 */
	private String lastWord(IDocument document, int offset) {
		try {
			for (int n = offset - 1; n >= 0; n--) {
				char c = document.getChar(n);
				if (!Character.isJavaIdentifierPart(c)) {
					return document.get(n + 1, offset - n - 1);
				}
			}
			return document.get(0, offset);
		} catch (BadLocationException e) {
			Activator.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// No context information for now
		return NO_CONTEXTS;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// No context information validator
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// No auto activation
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// No auto activation
		return null;
	}
}
