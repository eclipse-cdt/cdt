/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework.examples;

import junit.framework.TestSuite;

public class MixedSuite extends TestSuite {

	public MixedSuite() {
		super();
		setName("mixed results");
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01"));
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02"));
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure01"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure02"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure03"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError01"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError02"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError03"));
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess01"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure01"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError01"));
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess02"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure02"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError02"));
		this.addTest(TestSuite.createTest(SuccessTests.class, "testSuccess03"));
		this.addTest(TestSuite.createTest(FailureTests.class, "testFailure03"));
		this.addTest(TestSuite.createTest(ErrorTests.class, "testError03"));
	}
	
}


