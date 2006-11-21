/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.systems.connection;

import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.ITestSuiteProvider;

public class ConnectionTestSuite implements ITestSuiteProvider {

	public TestSuite getSuite(String arg) {
		TestSuite suite = new TestSuite("Connection Test");
		suite.addTest(new ConnectionTest("testConnect"));
		suite.addTest(new ConnectionTest("testResolveFilterString"));
		return suite;
	}

}
