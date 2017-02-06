/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.testsrunner.core.TestModelManagerCasesReorderingTestCase;
import org.eclipse.cdt.testsrunner.core.TestModelManagerSuitesReorderingTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.BoostTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.GoogleTestCase;
import org.eclipse.cdt.testsrunner.testsrunners.QtTestCase;


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
