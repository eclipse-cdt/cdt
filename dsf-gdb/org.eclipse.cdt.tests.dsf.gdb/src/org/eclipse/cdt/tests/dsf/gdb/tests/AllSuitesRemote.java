/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.Suite_Remote_7_1;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_10.Suite_Remote_7_10;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2.Suite_Remote_7_2;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.Suite_Remote_7_3;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.Suite_Remote_7_4;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5.Suite_Remote_7_5;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6.Suite_Remote_7_6;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_7.Suite_Remote_7_7;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_8.Suite_Remote_7_8;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_9.Suite_Remote_7_9;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit suites we
 * want to run.  When creating a new suite class, it should be
 * added to the list below.
 * 
 * This suite runs all the other suites.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Suite_Remote_7_10.class,
	Suite_Remote_7_9.class,
	Suite_Remote_7_8.class,
	Suite_Remote_7_7.class,
	Suite_Remote_7_6.class,
	Suite_Remote_7_5.class,
	Suite_Remote_7_4.class,
	Suite_Remote_7_3.class,
	Suite_Remote_7_2.class,
	Suite_Remote_7_1.class,
// The below test suites have failures
// Don't run them automatically so that we
// can get passing tests in Hudson
//	Suite_Remote_7_0.class,
//	Suite_Remote_6_8.class,
//	Suite_Remote_6_7.class,
//	Suite_Remote_6_6.class,
	/* Add your suite class here */
})

public class AllSuitesRemote {}