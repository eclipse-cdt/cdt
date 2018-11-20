/*******************************************************************************
 * Copyright (c) 2007, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class KeywordCompletionProposalComputer extends ParsingBasedProposalComputer {

	private static final int MIN_KEYWORD_LENGTH = 5;

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) throws CoreException {

		if (prefix.length() == 0) {
			try {
				prefix = context.computeIdentifierPrefix().toString();
			} catch (BadLocationException exc) {
				CUIPlugin.log(exc);
			}
		}
		final int prefixLength = prefix.length();
		// No prefix, no completions
		if (prefixLength == 0 || context.isContextInformationStyle())
			return Collections.emptyList();

		// keywords are matched case-sensitive
		final int relevance = RelevanceConstants.CASE_MATCH_RELEVANCE + RelevanceConstants.KEYWORD_TYPE_RELEVANCE;

		List<ICompletionProposal> proposals = new ArrayList<>();

		ICLanguageKeywords languageKeywords = null;
		ITranslationUnit tu = context.getTranslationUnit();
		if (tu != null) {
			ILanguage language = tu.getLanguage();
			if (language != null)
				languageKeywords = language.getAdapter(ICLanguageKeywords.class);
		}

		if (languageKeywords == null)
			return Collections.emptyList();

		if (inPreprocessorDirective(context)) {
			// TODO split this into a separate proposal computer?
			boolean needDirectiveKeyword = inPreprocessorKeyword(context);

			// add matching preprocessor keyword proposals
			ImageDescriptor imagedesc = CElementImageProvider.getKeywordImageDescriptor();
			Image image = imagedesc != null ? CUIPlugin.getImageDescriptorRegistry().get(imagedesc) : null;

			for (String keyword : languageKeywords.getPreprocessorKeywords()) {
				if (keyword.startsWith(prefix) && keyword.length() > prefixLength) {
					String repString = keyword + ' ';
					int repLength = prefixLength;
					int repOffset = context.getInvocationOffset() - repLength;
					if (prefix.charAt(0) == '#') {
						// strip leading '#' from replacement
						repLength--;
						repOffset++;
						repString = repString.substring(1);
					} else if (needDirectiveKeyword) {
						continue;
					}
					proposals.add(new CCompletionProposal(repString, repOffset, repLength, image, keyword, relevance,
							context.getViewer()));
				}
			}
		} else {
			if (!isValidContext(completionNode))
				return Collections.emptyList();

			// add matching keyword proposals
			ImageDescriptor imagedesc = CElementImageProvider.getKeywordImageDescriptor();
			Image image = imagedesc != null ? CUIPlugin.getImageDescriptorRegistry().get(imagedesc) : null;

			for (String keyword : languageKeywords.getKeywords()) {
				if (keyword.startsWith(prefix) && keyword.length() > prefixLength
						&& keyword.length() >= MIN_KEYWORD_LENGTH) {
					int repLength = prefixLength;
					int repOffset = context.getInvocationOffset() - repLength;
					proposals.add(new CCompletionProposal(keyword, repOffset, repLength, image, keyword, relevance,
							context.getViewer()));
				}
			}
		}

		return proposals;
	}

	/**
	 * Checks whether the given invocation context looks valid for template completion.
	 *
	 * @param context  the content assist invocation context
	 * @return <code>false</code> if the given invocation context looks like a field reference
	 */
	private boolean isValidContext(IASTCompletionNode completionNode) {
		IASTName[] names = completionNode.getNames();
		for (int i = 0; i < names.length; ++i) {
			IASTName name = names[i];

			// ignore if not connected
			if (name.getTranslationUnit() == null)
				continue;

			// ignore if this is a member access
			if (name.getParent() instanceof IASTFieldReference)
				continue;

			return true;
		}

		return false;
	}

	/**
	 * Test whether the invocation offset is inside or before the preprocessor directive keyword.
	 *
	 * @param context  the invocation context
	 * @return <code>true</code> if the invocation offset is inside or before the directive keyword
	 */
	private boolean inPreprocessorKeyword(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();

		try {
			final ITypedRegion partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				String ppPrefix = doc.get(partition.getOffset(), offset - partition.getOffset());
				if (ppPrefix.matches("\\s*#\\s*\\w*")) { //$NON-NLS-1$
					// we are inside the directive keyword
					return true;
				}
			}

		} catch (BadLocationException exc) {
		}
		return false;
	}

	/**
	 * Check if the invocation offset is inside a preprocessor directive.
	 *
	 * @param context  the content asist invocation context
	 * @return <code>true</code> if invocation offset is inside a preprocessor directive
	 */
	private boolean inPreprocessorDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();

		try {
			final ITypedRegion partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				return true;
			}

		} catch (BadLocationException exc) {
		}
		return false;
	}
}
