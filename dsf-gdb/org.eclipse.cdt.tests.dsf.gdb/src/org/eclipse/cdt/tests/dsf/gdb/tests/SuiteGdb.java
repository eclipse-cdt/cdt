/*******************************************************************************
 * Copyright (c) 2016 QNX Software System and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty. It enables us to define the annotations
 * which list all the different JUnit class we want to run. When creating a new
 * test class, it should be added to the list below.
 *
 * This suite is for tests to be run with GDB.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	MIBreakpointsTest.class,
		/* Add your test class here */
})
public class SuiteGdb {
	@BeforeClass
	public static void before() {
		// If we running this suite we have to clean up global options since
		// each
		// test will set local version of these properly.
		// If our tests are running from other suites they
		// may have globals that will override local values.
		BaseParametrizedTestCase.resetGlobalState();
	}
}