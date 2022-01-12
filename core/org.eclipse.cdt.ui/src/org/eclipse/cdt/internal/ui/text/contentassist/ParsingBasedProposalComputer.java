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
 *     QNX - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

/**
 * The base class for any proposal computers that require a completion node in
 * order to determine its completion proposals.
 *
 * @author Bryan Wilkinson
 */
public abstract class ParsingBasedProposalComputer implements ICompletionProposalComputer {
	private String fErrorMessage;

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		try {
			if (context instanceof CContentAssistInvocationContext) {
				CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;

				IASTCompletionNode completionNode = cContext.getCompletionNode();
				if (completionNode == null)
					return Collections.emptyList();
				String prefix = completionNode.getPrefix();
				if (prefix == null) {
					prefix = cContext.computeIdentifierPrefix().toString();
				}

				return computeCompletionProposals(cContext, completionNode, prefix);
			}
		} catch (Exception e) {
			fErrorMessage = e.toString();
			CUIPlugin.log(e);
		}

		return Collections.emptyList();
	}

	protected abstract List<ICompletionProposal> computeCompletionProposals(CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) throws CoreException;

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		Collection<ICompletionProposal> proposals = computeCompletionProposals(context, monitor);
		// Remove duplicates
		proposals = (new LinkedHashSet<>(proposals));
		List<IContextInformation> result = new ArrayList<>();
		for (ICompletionProposal proposal : proposals) {
			IContextInformation contextInformation = proposal.getContextInformation();
			if (contextInformation != null) {
				result.add(contextInformation);
			}
		}

		return result;
	}

	@Override
	public String getErrorMessage() {
		return fErrorMessage;
	}

	@Override
	public void sessionEnded() {
	}

	@Override
	public void sessionStarted() {
		fErrorMessage = null;
	}

	/**
	 * Computes base relevance depending on quality of name / prefix match.
	 *
	 * @param prefix  the completion prefix
	 * @param match  the matching identifier
	 * @return a relevance value indicating the quality of the name match
	 */
	protected int computeBaseRelevance(String prefix, String match) {
		boolean caseMatch = prefix.length() > 0 && match.startsWith(prefix);
		if (caseMatch) {
			return RelevanceConstants.CASE_MATCH_RELEVANCE;
		}
		boolean exactNameMatch = match.equalsIgnoreCase(prefix);
		if (exactNameMatch) {
			return RelevanceConstants.EXACT_NAME_MATCH_RELEVANCE;
		}
		return 0;
	}
}
