/*******************************************************************************
 * Copyright (c) 2006, 2008 Norbert Ploett and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Norbert Ploett (Siemens) - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.ui.text.contentassist;

import org.eclipse.cdt.ui.text.ICCompletionProposal;

/**
 * Filters completion proposals displayed by the C/C++ editor content assistant.
 * <p>
 * Contributions to the <tt>org.eclipse.cdt.ui.ProposalFilter</tt> extension point
 * must implement this interface.
 * </p>
 */
public interface IProposalFilter {

	/**
	 * Filter a list of ICCompletionProposals <br>
	 * - Change the order of entries <br>
	 * - Remove undesired (duplicate) entries <br>
	 * - Supplement existing entries with additional information
	 * @param proposals The List of proposals
	 * @return The filtered list of proposals as array
	 */
	ICCompletionProposal[] filterProposals(ICCompletionProposal[] proposals);

}
