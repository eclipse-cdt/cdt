/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson)	- Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5.Suite_7_5;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This suite runs all suites that are part of the tests
 * automatically run with each CDT build.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
	Suite_7_5.class,
	// Can't run the Remote test just yet because they
	// have the same names on the local tests, which is
	// not handled by JUnit (https://bugs.eclipse.org/172256)
})

public class AutomatedSuite {}