/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 * Johnson Ma (Wind River) - [195402] Add tar.gz archive support
 *******************************************************************************/
package org.eclipse.rse.tests.subsystems.files;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;

public class RSEFileSubsystemTestSuite extends DelegatingTestSuiteHolder {
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
		TestSuite suite = new TestSuite("RSEFileSubsystemTestSuite"); //$NON-NLS-1$
		// add the single test suites to the overall one here.
		//-test-disabled-//suite.addTestSuite(CreateFileTestCase.class);
		//-test-disabled-//suite.addTestSuite(FileOutputStreamTestCase.class);
		suite.addTestSuite(FileServiceArchiveTest.class);
		//-test-disabled-//suite.addTest(FileServiceArchiveTestDStore.suite());
		//-test-disabled-//suite.addTest(FileServiceArchiveTestDStoreWindows.suite());
		suite.addTestSuite(FileServiceTest.class);
		//-test-disabled-//suite.addTestSuite(FileSubsystemConsistencyTestCase.class);

		// Do not include the ftp sub system test case within the automated tests.
		// Most server seems to limit the amount of connections per IP-address, so
		// we run in problems with that. The test needs to be executed manually with
		// the ftp server to use possibly changed to whatever host will do.
		suite.addTestSuite(FTPFileSubsystemTestCase.class);
		suite.addTestSuite(FileServiceTgzArchiveTest.class);
		suite.addTest(RSEFileStoreTest.suite());

		return suite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		return (TestSuite)RSEFileSubsystemTestSuite.suite();
	}

}
