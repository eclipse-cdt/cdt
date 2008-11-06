/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/
package org.eclipse.rse.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;

/**
 * Main class bundling all single specialized test suites into a
 * overall complete one.
 */
public class RSECombinedTestSuite extends DelegatingTestSuiteHolder {

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
		TestSuite suite = new TestSuite("RSE Combined Test Suite"); //$NON-NLS-1$

		// add the single test suites to the overall one here.
		suite.addTest(org.eclipse.rse.tests.core.AllTests.suite());
		suite.addTest(org.eclipse.rse.tests.core.connection.RSEConnectionTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.core.registries.RSERegistriesTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.internal.RSEInternalFrameworkTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.persistence.PersistenceTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.preferences.RSEPreferencesTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.subsystems.files.RSEFileSubsystemTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.subsystems.shells.RSEShellSubsystemTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.subsystems.testsubsystem.RSETestSubsystemTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.ui.mnemonics.MnemonicsTestSuite.suite());
		suite.addTest(org.eclipse.rse.tests.ui.preferences.PreferencesTestSuite.suite());
		// //Manual test below -- cannot be run in unattended builds
		// suite.addTest(org.eclipse.rse.tests.ui.connectionwizard.RSENewConnectionWizardTestSuite.suite());
		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		return (TestSuite)RSECombinedTestSuite.suite();
	}
}
