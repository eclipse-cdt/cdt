package org.eclipse.cdt.ui.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.text.PartitionTokenScannerTest;
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
		addTest(PartitionTokenScannerTest.suite());
		addTest(TextBufferTest.suite());

		
	}
	
}

