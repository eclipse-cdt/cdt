/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.suite;

import org.eclipse.cdt.managedbuilder.ui.tests.TestCProjectPlatformPage;
import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.tests.properties.ToolListContentProviderTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AutomatedIntegrationSuite {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AutomatedIntegrationSuite.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.eclipse.cdt.managedbuilder.ui.tests");
		//$JUnit-BEGIN$
		// TODO uncoment this
		suite.addTest(TestCustomPageManager.suite());
		suite.addTestSuite(TestCProjectPlatformPage.class);
		suite.addTest(ToolListContentProviderTests.suite());

		//$JUnit-END$
		return suite;
	}
}
