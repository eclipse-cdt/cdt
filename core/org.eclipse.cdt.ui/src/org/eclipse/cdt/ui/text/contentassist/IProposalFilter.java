package org.eclipse.cdt.ui.text.contentassist;

import org.eclipse.cdt.ui.text.ICCompletionProposal;


public interface IProposalFilter {
	
		/**
		 * Filter a list of ICCompletionProposals <br>
		 * - Change the order of entries <br>
		 * - Remove undesired (duplicate) entries <br>
		 * - Supplement existing entries with additional information
		 * @param proposals The List of proposals
		 * @return The filtered list of proposals as array
		 */
		ICCompletionProposal[] filterProposals(ICCompletionProposal[] proposals) ;

}
