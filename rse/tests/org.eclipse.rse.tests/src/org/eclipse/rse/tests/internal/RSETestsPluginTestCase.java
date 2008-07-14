/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/

package org.eclipse.rse.tests.internal;

import java.util.ResourceBundle;

import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.RSECoreTestCase;

/**
 * Test cases testing the functionality of the <code>RSETestsPlugin</code> class.
 */
public class RSETestsPluginTestCase extends RSECoreTestCase {

	/**
	 * Test the association of the resource bundle to the plugin and related
	 * resource bundle functionality.
	 */
	public void testPluginResourceBundle() {
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;

		ResourceBundle bundle = RSETestsPlugin.getDefault().getResourceBundle();
		assertNotNull("No resource bundle associated with RSETestsPlugin!", bundle); //$NON-NLS-1$

		// our own test id must be true here, otherwise we wouldn't had
		// reached this point anyway.
		assertTrue("Unexpected return value false!", RSETestsPlugin.isTestCaseEnabled("RSETestsPluginTestCase.testPluginResourceBundle")); //$NON-NLS-1$ //$NON-NLS-2$

		// a test id not listed within the resources file must be always true
		assertTrue("Unexpected return value false!", RSETestsPlugin.isTestCaseEnabled("RSETestsPluginTestCase.testNeverAddThisToTheResourceBundle")); //$NON-NLS-1$ //$NON-NLS-2$

		// this test id should be never enabled
		assertFalse("Unexpected return value true!", RSETestsPlugin.isTestCaseEnabled("RSETestsPluginTestCase.dontRemove.testNeverEnabledThis")); //$NON-NLS-1$ //$NON-NLS-2$

		// Test the different getResourceString methods.
		String expected = "testResolveString"; //$NON-NLS-1$
		assertEquals("Unexpected return value!", expected, RSETestsPlugin.getResourceString("RSETestsPluginTestCase.dontRemove.testResolveString")); //$NON-NLS-1$ //$NON-NLS-2$

		expected = "testResolveString, param=value"; //$NON-NLS-1$
		assertEquals("Unexpected return value!", expected, RSETestsPlugin.getResourceString("RSETestsPluginTestCase.dontRemove.testResolveStringOneParameter", "value")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		expected = "testResolveString, param=value1, param=value2"; //$NON-NLS-1$
		assertEquals("Unexpected return value!", expected, RSETestsPlugin.getResourceString("RSETestsPluginTestCase.dontRemove.testResolveStringMultiParameter", new Object[] { "value1", "value2" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
