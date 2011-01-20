/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Tom Hochstein (Freescale)     - [301075] Host copy doesn't copy contained property sets
 *******************************************************************************/

package org.eclipse.rse.tests.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.rse.tests.core.connection.RSEConnectionTestSuite;
import org.eclipse.rse.tests.framework.DelegatingTestSuiteHolder;

/**
 * Suite for RSE Core Model test cases.
 */
public class AllTests extends DelegatingTestSuiteHolder {

	/** Run this test suite stand-alone. Only makes sense if no plugin test */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// add the single test suites to the overall one here.
		suite.addTestSuite(HostMoveTest.class);
		suite.addTestSuite(HostCopyTest.class);
		
		return suite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		return (TestSuite)RSEConnectionTestSuite.suite();
	}
}
