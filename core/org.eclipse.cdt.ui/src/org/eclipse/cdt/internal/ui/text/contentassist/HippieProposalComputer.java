/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

/**
 * A computer wrapper for the hippie processor.
 *
 * @since 4.0
 */
public final class HippieProposalComputer implements ICompletionProposalComputer {
	/** The wrapped processor. */
	private final HippieProposalProcessor fProcessor = new HippieProposalProcessor();

	/**
	 * Default ctor to make it instantiatable via the extension mechanism.
	 */
	public HippieProposalComputer() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return Arrays.asList(fProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset()));
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#computeContextInformation(org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return Arrays.asList(fProcessor.computeContextInformation(context.getViewer(), context.getInvocationOffset()));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return fProcessor.getErrorMessage();
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionStarted()
	 */
	@Override
	public void sessionStarted() {
	}

	/*
	 * @see org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer#sessionEnded()
	 */
	@Override
	public void sessionEnded() {
	}
}
