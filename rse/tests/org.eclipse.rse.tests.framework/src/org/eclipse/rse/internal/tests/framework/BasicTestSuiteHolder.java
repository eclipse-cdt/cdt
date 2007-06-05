/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework;

import org.eclipse.rse.tests.framework.AbstractTestSuiteHolder;

import junit.framework.TestSuite;

/**
 * A BasicHolder provides a simple wrapper for a test suite. Use this if you just want to contribute a JUnit
 * test suite that you already have without defining it in an extension point.
 */
public class BasicTestSuiteHolder extends AbstractTestSuiteHolder {
	
	private TestSuite testSuite;
	
	public BasicTestSuiteHolder(TestSuite testSuite) {
		this.testSuite = testSuite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getName()
	 */
	public String getName() {
		return testSuite.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.framework.AbstractTestSuiteHolder#getTestSuite()
	 */
	public TestSuite getTestSuite() {
		return testSuite;
	}

}
