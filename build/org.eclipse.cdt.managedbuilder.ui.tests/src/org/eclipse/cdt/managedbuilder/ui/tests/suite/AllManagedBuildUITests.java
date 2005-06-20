/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.suite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllManagedBuildUITests {
	public static void main(String[] args) {
	    CCorePlugin.getDefault().getCoreModel().getIndexManager().reset();
		junit.textui.TestRunner.run(AllManagedBuildUITests.suite());
	}
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.cdt.managedbuilder.ui.tests");
		//$JUnit-BEGIN$
// TODO uncoment this		
		suite.addTest(TestCustomPageManager.suite());
		
		//$JUnit-END$
		return suite;
	}
}
