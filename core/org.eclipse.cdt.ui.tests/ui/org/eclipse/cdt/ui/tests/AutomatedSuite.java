package org.eclipse.cdt.ui.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.text.PartitionTokenScannerTest;
import org.eclipse.cdt.ui.tests.text.contentassist.*;
import org.eclipse.cdt.ui.tests.text.contentassist.failedtests.*;
import org.eclipse.cdt.ui.tests.textmanipulation.TextBufferTest;



/**
 * Test all areas of the UI.
 */
public class AutomatedSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AutomatedSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public AutomatedSuite() {
		
		// Success Tests
		addTest(PartitionTokenScannerTest.suite());
		addTest(TextBufferTest.suite());
		addTest(CompletionProposalsTest1.suite());
		addTest(CompletionProposalsTest2.suite());
		addTest(CompletionProposalsTest3.suite());
		addTest(CompletionProposalsTest4.suite());
		addTest(CompletionProposalsTest5.suite());
		addTest(CompletionProposalsTest6.suite());
		addTest(CompletionProposalsTest7.suite());
		addTest(CompletionProposalsTest8.suite());
		addTest(CompletionProposalsTest9.suite());
		addTest(CompletionProposalsTest10.suite());
		addTest(CompletionProposalsTest11.suite());
		addTest(CompletionProposalsTest12.suite());
		
		// Failed Tests
		addTest(CompletionProposalsFailedTest1.suite());
		addTest(CompletionProposalsFailedTest2.suite());
		addTest(CompletionProposalsFailedTest3.suite());
		
	}
	
}

