/*******************************************************************************
 * Copyright (c) 2007, 2017 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *     Jonah Graham (Kichwa Coders) - converted to new style suite (Bug 515178)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is a TestSuite, the TestCases created to test Template engine are
 * added to testsuite.
 * The test suite will execute all the Testcases added to the Suite.
 * 
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestTemplateEngine.class,
	TestTemplateCore.class,
	TestValueStore.class,
	TestSharedDefaults.class,
	TestProcesses.class,
	TestTemplateEngineBugs.class,

})
public class AllTemplateEngineTests {
}
