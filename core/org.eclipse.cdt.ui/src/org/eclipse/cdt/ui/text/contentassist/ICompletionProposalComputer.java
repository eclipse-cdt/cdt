/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.cdt.ui.text.contentassist;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

/**
 * Computes completions and context information displayed by the C/C++ editor content assistant.
 * <p>
 * Contributions to the <tt>org.eclipse.cdt.ui.completionProposalComputer</tt> extension point
 * must implement this interface.
 * </p>
 *
 * @since 4.0
 */
public interface ICompletionProposalComputer {
	/**
	 * Informs the computer that a content assist session has started. This call will always be
	 * followed by a {@link #sessionEnded()} call, but not necessarily by calls to
	 * {@linkplain #computeCompletionProposals(ContentAssistInvocationContext, IProgressMonitor) computeCompletionProposals}
	 * or
	 * {@linkplain #computeContextInformation(ContentAssistInvocationContext, IProgressMonitor) computeContextInformation}.
	 */
	void sessionStarted();

	/**
	 * Returns a list of completion proposals valid at the given invocation context.
	 *
	 * @param context the context of the content assist invocation
	 * @param monitor a progress monitor to report progress. The monitor is private to this
	 *        invocation, i.e. there is no need for the receiver to spawn a sub monitor.
	 * @return a list of completion proposals (element type: {@link ICompletionProposal})
	 */
	List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor);

	/**
	 * Returns context information objects valid at the given invocation context.
	 *
	 * @param context the context of the content assist invocation
	 * @param monitor a progress monitor to report progress. The monitor is private to this
	 *        invocation, i.e. there is no need for the receiver to spawn a sub monitor.
	 * @return a list of context information objects (element type: {@link IContextInformation})
	 */
	List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor);

	/**
	 * Returns the reason why this computer was unable to produce any completion proposals or
	 * context information.
	 *
	 * @return an error message or <code>null</code> if no error occurred
	 */
	String getErrorMessage();

	/**
	 * Informs the computer that a content assist session has ended. This call will always be after
	 * any calls to
	 * {@linkplain #computeCompletionProposals(ContentAssistInvocationContext, IProgressMonitor) computeCompletionProposals}
	 * and
	 * {@linkplain #computeContextInformation(ContentAssistInvocationContext, IProgressMonitor) computeContextInformation}.
	 */
	void sessionEnded();
}
