/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.ID;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.ui.tests.chelp.CHelpTestInfoProvider;

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

	// Note: The help proposal completions for C library functions that are proposed
	//       in this test are defined in the CHelpProposalTester.CHelpBook constructor.
	//       When writing a new test case, add any necessary new functions there.

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

	//	struct Waldo {
	//		Waldo(int, int);
	//	};
	//	int main() {
	//		Waldo w(/*cursor*/
	//	}
	public void testHelpProposalInInappropriateContext_509186() throws Exception {
		String[] expected = new String[] { "Waldo(const Waldo &)", "Waldo(int, int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	void foo() {
	//	#ifdef MYMACRO
	//		setv/*cursor*/
	//	#endif
	//	}
	public void testInactiveCodeBlock_72809() throws Exception {
		assertCompletionResults(new String[] { "setvbuf()" });
	}
}
