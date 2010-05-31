/* *******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * Martin Oberhuber (Wind River) - [247908] extract testBug255023
 * *******************************************************************************/
package org.eclipse.rse.tests.core.connection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;

/**
 * Main class bundling all RSE connection test cases.
 */
public class RSEConnectionTestSuite extends DelegatingTestSuiteHolder {

	/**
	 * Standard Java application main method. Allows to launch the test
	 * suite from outside as part of nightly runs, headless runs or other.
	 * <p><b>Note:</b> Use only <code>junit.textui.TestRunner</code> here as
	 * it is explicitly supposed to output the test output to the shell the
	 * test suite has been launched from.
	 * <p>
	 * @param args The standard Java application command line parameters passed in.
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Combine all test into a suite and returns the test suite instance.
	 * <p>
	 * <b>Note: This method must be always called <i><code>suite</code></i> ! Otherwise
	 * the JUnit plug-in test launcher will fail to detect this class!</b>
	 * <p>
	 * @return The test suite instance.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("RSE Connection Test Suite"); //$NON-NLS-1$
		// add the single test suites to the overall one here.
		suite.addTestSuite(RSEConnectionTestCase.class);
		// TODO currently fails - see bug 255023
		//suite.addTestSuite(TestBug255023.class);
		
		return suite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		return (TestSuite)RSEConnectionTestSuite.suite();
	}
}
