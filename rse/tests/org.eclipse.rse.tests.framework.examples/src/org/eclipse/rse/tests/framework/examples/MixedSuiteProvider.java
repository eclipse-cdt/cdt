/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is 
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework.examples;

import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.ITestSuiteProvider;

public class MixedSuiteProvider implements ITestSuiteProvider {

	public TestSuite getSuite(String arg) {
		TestSuite suite = new TestSuite("Mixed Suite, arg = " + arg); //$NON-NLS-1$
		if (arg == null || arg.equals("success")) { //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03")); //$NON-NLS-1$
		}
		if (arg == null || arg.equals("failure")) { //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure03")); //$NON-NLS-1$
		}
		if (arg == null || arg.equals("error")) { //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError03")); //$NON-NLS-1$
		}
		if (arg == null) {
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError01")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError02")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(FailureTests.class, "testFailure03")); //$NON-NLS-1$
			suite.addTest(TestSuite.createTest(ErrorTests.class, "testError03")); //$NON-NLS-1$
		}
		return suite;
	}
	
}


