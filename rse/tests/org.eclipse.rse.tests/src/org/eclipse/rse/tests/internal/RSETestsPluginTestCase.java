/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial contribution.
 * *******************************************************************************/
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
		if (!RSETestsPlugin.isTestCaseEnabled("RSETestsPluginTestCase.testPluginResourceBundle")) return; //$NON-NLS-1$
		
		ResourceBundle bundle = RSETestsPlugin.getDefault().getResourceBundle();
		assertNotNull("No resource bundle associated with RSETestsPlugin!", bundle); //$NON-NLS-1$
	}
}
