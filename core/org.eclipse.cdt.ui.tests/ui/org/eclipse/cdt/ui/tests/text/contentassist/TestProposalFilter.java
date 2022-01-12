/*******************************************************************************
 * Copyright (c) 2006, 2014 Norbert Ploett and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Norbert Ploett (Seimens) - Initial Contribution
 *******************************************************************************/
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
	@Override
	public ICCompletionProposal[] filterProposals(ICCompletionProposal[] proposals) {
		return proposals;
	}
}
