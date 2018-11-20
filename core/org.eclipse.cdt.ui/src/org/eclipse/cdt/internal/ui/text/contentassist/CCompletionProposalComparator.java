/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Comparator;

import org.eclipse.cdt.ui.text.ICCompletionProposal;

public class CCompletionProposalComparator implements Comparator<ICCompletionProposal> {

	private boolean fOrderAlphabetically;

	/**
	 * Constructor for CompletionProposalComparator.
	 */
	public CCompletionProposalComparator() {
		fOrderAlphabetically = false;
	}

	public void setOrderAlphabetically(boolean orderAlphabetically) {
		fOrderAlphabetically = orderAlphabetically;
	}

	/*
	 * @see Comparator#compare(Object, Object)
	 */
	@Override
	public int compare(ICCompletionProposal c1, ICCompletionProposal c2) {
		if (!fOrderAlphabetically) {
			int relevanceDif = c2.getRelevance() - c1.getRelevance();
			if (relevanceDif != 0) {
				return relevanceDif;
			}
		}

		String id1 = c1.getIdString();
		String id2 = c2.getIdString();

		return id1.compareTo(id2);
	}

}
