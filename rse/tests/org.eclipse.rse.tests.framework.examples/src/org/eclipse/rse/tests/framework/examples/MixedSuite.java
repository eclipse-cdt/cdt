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

public class MixedSuite extends TestSuite {

	public MixedSuite() {
		super();
		setName("mixed results"); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure03")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError03")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError01")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError02")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure03")); //$NON-NLS-1$
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError03")); //$NON-NLS-1$
	}
	
}


