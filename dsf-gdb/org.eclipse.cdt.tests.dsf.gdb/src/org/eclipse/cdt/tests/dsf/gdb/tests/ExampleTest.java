/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * This is an example of how to write new JUnit test cases
 * for services of DSF.
 * 
 * Each test class should extend BaseTestCase
 * so as to automatically launch the application before
 * each testcase and tear it down after.
 *
 * Also, each new test class must be added to the list within AllTest.
 * 
 * Finally, each testcase should be @RunWith(BackgroundRunner.class)
 * so as to release the UI thread and allow things such as
 * timeouts to work in JUnit
 */

// Each test must run with the BackgroundRunner so as
// to release the UI thread
@RunWith(BackgroundRunner.class)

public class ExampleTest extends BaseTestCase {

	@BeforeClass
	public static void beforeClassMethod() {
		// Things to run once specifically for this class,
		// before starting this set of tests.
		// Any method name can be used
		
		// To choose your own test application, use the following form
		// You must make sure the compiled binary is available in the
		// specified location.
		// If this method call is not made, the default GDBMIGenericTestApp 
		// will be used
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           "data/launch/bin/SpecialTestApp.exe");
		
		// Other attributes can be changed here
	}
	
	@AfterClass
	public static void afterClassMethod() {
		// Things to run once specifically for this class,
		// after the launch has been performed
		// Any method name can be used
	}
	
	@Before
	public void beforeMethod() {
		// Things to run specifically for this class,
		// before each test but after the launch has been performed
		// The Launched used is for the default test application
		// Any method name can be used
	}
	
	@After
	public void afterMethod() {
		// Things to run specifically for this class
		// after each test but before the launch has been torn down
		// Any method name can be used
	}
	
//	@Override
//	public void baseBeforeMethod() {
//		// Can be used to override and prevent the baseSetup from being run
//	    // The name baseBeforeMethod must be used
//	}
	
//	@Override
//	public void baseAfterMethod() {
//		// Can be used to override and prevent the baseTeardown from being run
//      // The name baseAfterMethod must be used
//	}
	
	@Test
	public void basicTest() {
		// First test to run
		assertTrue("", true);
	}
	
	@Test(timeout=5000)
	public void timeoutTest() {
		// Second test to run, which will timeout if not finished on time
		assertTrue("", true);
	}

	@Test(expected=FileNotFoundException.class)
	public void exceptionTest() throws FileNotFoundException {
		// Third test to run which expects an exception
		throw new FileNotFoundException("Just testing");
	}

}
