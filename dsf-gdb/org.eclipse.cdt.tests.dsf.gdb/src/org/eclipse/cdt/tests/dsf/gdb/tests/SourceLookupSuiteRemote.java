/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.framework.BaseRemoteSuite;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6.SourceLookupTest_6_6;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7.SourceLookupTest_6_7;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8.SourceLookupTest_6_8;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0.SourceLookupTest_7_0;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.SourceLookupTest_7_1;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2.SourceLookupTest_7_2;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3.SourceLookupTest_7_3;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.SourceLookupTest_7_4;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5.SourceLookupTest_7_5;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6.SourceLookupTest_7_6;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_7.SourceLookupTest_7_7;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_8.SourceLookupTest_7_8;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_9.SourceLookupTest_7_9;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit class we
 * want to run.  When creating a new test class, it should be
 * added to the list below.
 * 
 *  This suite is for tests to be run with GDB 6.6
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// We need specific name for the tests of this suite, because of bug https://bugs.eclipse.org/172256
	SourceLookupTest_6_6.class,
	SourceLookupTest_6_7.class,
	SourceLookupTest_6_8.class,
	SourceLookupTest_7_0.class,
	SourceLookupTest_7_1.class,
	SourceLookupTest_7_2.class,
	SourceLookupTest_7_3.class,
	SourceLookupTest_7_4.class,
	SourceLookupTest_7_5.class,
	SourceLookupTest_7_6.class,
	SourceLookupTest_7_7.class,
	SourceLookupTest_7_8.class,
	SourceLookupTest_7_9.class,
	/* Add your test class here */
})

public class SourceLookupSuiteRemote extends BaseRemoteSuite {
}
