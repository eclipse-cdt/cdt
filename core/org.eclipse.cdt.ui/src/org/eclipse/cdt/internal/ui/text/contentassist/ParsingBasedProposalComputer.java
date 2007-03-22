/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

/**
 * The base class for any proposal computers that require a completion node in
 * order to determine its completion proposals.
 * 
 * @author Bryan Wilkinson
 */
public abstract class ParsingBasedProposalComputer implements ICompletionProposalComputer {

	private String fErrorMessage = null;
	
	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		try {
			if (context instanceof CContentAssistInvocationContext) {
				CContentAssistInvocationContext cContext = (CContentAssistInvocationContext) context;
				
				IASTCompletionNode completionNode = cContext.getCompletionNode();
				if (completionNode == null) return Collections.EMPTY_LIST;
				String prefix = completionNode.getPrefix();
				if (prefix == null) {
					prefix = cContext.computeIdentifierPrefix().toString();
				}

				return computeCompletionProposals(cContext, completionNode, prefix);
			}
		} catch (Throwable e) {
			fErrorMessage = e.toString();
		}

		return Collections.EMPTY_LIST;
	}
	
	protected abstract List computeCompletionProposals(
			CContentAssistInvocationContext context,
			IASTCompletionNode completionNode,
			String prefix) throws CoreException;
	
	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		List proposals= computeCompletionProposals(context, monitor);
		// remove duplicates
		proposals= new ArrayList(new LinkedHashSet(proposals));
		List result= new ArrayList();

		for (Iterator it= proposals.iterator(); it.hasNext();) {
			ICompletionProposal proposal= (ICompletionProposal) it.next();
			IContextInformation contextInformation= proposal.getContextInformation();
			if (contextInformation != null) {
				result.add(contextInformation);
			}
		}
		
		return result;
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	public void sessionEnded() {
	}

	public void sessionStarted() {
		fErrorMessage = null;
	}
}
