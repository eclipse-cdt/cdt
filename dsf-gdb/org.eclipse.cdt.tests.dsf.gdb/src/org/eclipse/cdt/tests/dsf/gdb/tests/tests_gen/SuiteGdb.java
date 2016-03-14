/*******************************************************************************
 * Copyright (c) Mar 14, 2016 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development SuiteGdb License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_gen;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit class we
 * want to run.  When creating a new test class, it should be
 * added to the list below.
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
	public static void beforeClassMethod() {
		//BaseTestCase.setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_11);
		BaseTestCase.ignoreIfGDBMissing();
	}
}