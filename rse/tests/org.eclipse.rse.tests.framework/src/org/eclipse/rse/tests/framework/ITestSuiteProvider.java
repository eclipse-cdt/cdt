/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.framework;

import junit.framework.TestSuite;

/**
 * A test suite provider will generate and deliver a test suite when asked to do so.
 */
public interface ITestSuiteProvider {
	
	/**
	 * Generates a test suite.
	 * @param argument a String that can be used to discriminate among test suites generated 
	 * by this provider.
	 * @return the TestSuite provided by this provider.
	 * @see TestSuite
	 */
	public TestSuite getSuite(String argument);
	
}


