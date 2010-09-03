/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6.Suite_6_6;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_7.Suite_6_7;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_8.Suite_6_8;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_0.Suite_7_0;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_1.Suite_7_1;
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
	Suite_7_1.class,
	Suite_7_0.class,
	Suite_6_8.class,
	Suite_6_7.class,
	Suite_6_6.class,
	/* Add your suite class here */
})

public class AllSuites {}