/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.ui.tests.TestCProjectPlatformPage;
import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;

/**
 *
 */
public class AllManagedBuildUITests {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllManagedBuildUITests.suite());
	}
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.cdt.managedbuilder.ui.tests");
		//$JUnit-BEGIN$
// TODO uncoment this		
		suite.addTest(TestCustomPageManager.suite());
		suite.addTestSuite(TestCProjectPlatformPage.class);
		
		//$JUnit-END$
		return suite;
	}
}
