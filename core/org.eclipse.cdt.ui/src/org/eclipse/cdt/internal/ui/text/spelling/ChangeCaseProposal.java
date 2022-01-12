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
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import java.util.Locale;

import org.eclipse.cdt.ui.text.IInvocationContext;

/**
 * Proposal to change the letter case of a word.
 */
public class ChangeCaseProposal extends WordCorrectionProposal {

	/**
	 * Creates a new change case proposal.
	 *
	 * @param arguments The problem arguments associated with the spelling problem
	 * @param offset The offset in the document where to apply the proposal
	 * @param length The length in the document to apply the proposal
	 * @param context The invocation context for this proposal
	 * @param locale The locale to use for the case change
	 */
	public ChangeCaseProposal(final String[] arguments, final int offset, final int length,
			final IInvocationContext context, final Locale locale) {
		super(Character.isLowerCase(arguments[0].charAt(0))
				? Character.toUpperCase(arguments[0].charAt(0)) + arguments[0].substring(1)
				: arguments[0], arguments, offset, length, context, Integer.MAX_VALUE);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		return Messages.Spelling_case_label;
	}
}
