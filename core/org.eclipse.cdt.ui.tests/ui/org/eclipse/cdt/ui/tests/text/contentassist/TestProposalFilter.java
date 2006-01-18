package org.eclipse.cdt.ui.tests.text.contentassist;

import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.IProposalFilter;

/**
 * Dummy filter implementation for testing purposes
 */
public class TestProposalFilter implements IProposalFilter {

	/**
	 * This dummy filter method will return the original proposals unmodified.
	 */
	public ICCompletionProposal[] filterProposals(
			ICCompletionProposal[] proposals) {
		return proposals ;
	}

}
