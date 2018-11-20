/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.test;

import org.eclipse.cdt.testsrunner.core.TestModelManagerCasesReorderingTestCase;
import org.eclipse.cdt.testsrunner.core.TestModelManagerSuitesReorderingTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.BoostTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.GoogleTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.QtTestCase;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite with all the tests on Tests Runner.
 */
public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();
		// Core
		suite.addTestSuite(TestModelManagerSuitesReorderingTestCase.class);
		suite.addTestSuite(TestModelManagerCasesReorderingTestCase.class);
		// Tests Runners Plug-ins
		suite.addTestSuite(BoostTestCase.class);
		suite.addTestSuite(GoogleTestCase.class);
		suite.addTestSuite(QtTestCase.class);
		return suite;
	}
}
