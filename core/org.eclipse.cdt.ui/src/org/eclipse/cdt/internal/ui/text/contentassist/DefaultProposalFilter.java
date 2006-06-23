/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Arrays;

import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.IProposalFilter;

/**
 * The default code completion filter: Remove duplicate entries on the basis of
 * their id string. Use CCompletionProposalComparator for sorting.
 */
public class DefaultProposalFilter implements IProposalFilter {

	public ICCompletionProposal[] filterProposals(
			ICCompletionProposal[] proposals) {

		CCompletionProposalComparator propsComp = new CCompletionProposalComparator();
		propsComp.setOrderAlphabetically(true);
		Arrays.sort(proposals, propsComp);

		// remove duplicates but leave the ones with return types

		int last = 0;
		int removed = 0;
		for (int i = 1; i < proposals.length; ++i) {
			if (propsComp.compare(proposals[last], proposals[i]) == 0) {
				// We want to leave the one that has the return string if any
				boolean lastReturn = proposals[last].getIdString() != proposals[last]
						.getDisplayString();
				boolean iReturn = proposals[i].getIdString() != proposals[i]
						.getDisplayString();

				if (!lastReturn && iReturn)
					// flip i down to last
					proposals[last] = proposals[i];

				// Remove the duplicate
				proposals[i] = null;
				++removed;
			} else
				// update last
				last = i;
		}
		if (removed > 0) {
			// Strip out the null entries
			ICCompletionProposal[] newArray = new ICCompletionProposal[proposals.length
					- removed];
			int j = 0;
			for (int i = 0; i < proposals.length; ++i)
				if (proposals[i] != null)
					newArray[j++] = proposals[i];
			proposals = newArray;
		}

		return proposals;
	}

}
