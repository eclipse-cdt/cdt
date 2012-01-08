/*******************************************************************************
 * Copyright (c) 2006, 2007 Norbert Ploett and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett (Seimens) - Initial Contribution
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
	public ICCompletionProposal[] filterProposals(
			ICCompletionProposal[] proposals) {
		return proposals ;
	}

}
