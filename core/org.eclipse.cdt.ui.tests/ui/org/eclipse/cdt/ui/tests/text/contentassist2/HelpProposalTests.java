/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.ui.tests.chelp.CHelpTestInfoProvider;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;

import junit.framework.Test;

/**
 * Tests for {@link org.eclipse.cdt.internal.ui.text.contentassist.HelpCompletionProposalComputer}. 
 */
public class HelpProposalTests extends CompletionTestBase {
	private boolean fOldTestInfoProviderEnablement;
	
	public HelpProposalTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return BaseTestCase.suite(HelpProposalTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fOldTestInfoProviderEnablement = CHelpTestInfoProvider.fgEnabled;
		CHelpTestInfoProvider.fgEnabled = true;
	}
	
	@Override
	protected void tearDown() throws Exception {
		CHelpTestInfoProvider.fgEnabled = fOldTestInfoProviderEnablement;
		super.tearDown();
	}
	
	//	int main() {
	//	    setvbuf(file, NULL, _IOLBF, /*cursor*/);
	//	}
	public void testHelpProposalClobberingTokens_391439() throws Exception {
		Object[] results = invokeContentAssist(fCursorOffset, 0, true, false, true).results;
		assertEquals(1, results.length);
		assertInstance(results[0], CCompletionProposal.class);
		CCompletionProposal proposal = ((CCompletionProposal) results[0]);
		assertEquals(0, proposal.getReplacementLength());
		assertEquals("", proposal.getReplacementString());
		assertNotNull(proposal.getContextInformation());
	}
}
