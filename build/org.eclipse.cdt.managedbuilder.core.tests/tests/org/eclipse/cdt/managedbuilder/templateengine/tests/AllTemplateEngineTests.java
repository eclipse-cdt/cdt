/*******************************************************************************
 * Copyright (c) 2007, 2009 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is a TestSuite, the TestCases created to test Template engine are
 * added to test-suite. The test suite will execute all the test cases added
 * to the Suite.
 * 
 * @since 4.0
 */
public class AllTemplateEngineTests extends TestSuite{

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllTemplateEngineTests.suite());
	}

	/**
	 * Since the TemplateEngine consists of UI(Wizard).
	 * A TestWizard is created to which the dynamically generated
	 * UIPages are added.  The Wizard is launched from here.
	 * The TestCases created to test the TemplateEngine is initialized here.
	 * @return
	 * 
	 * @since 4.0
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Template engine tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$

		suite.addTestSuite(TestProcesses.class);

		//$JUnit-END$
		return suite;
	}
}
