/*******************************************************************************
 * Copyright (c) 2007, 2017 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
@Suite.SuiteClasses({ TestTemplateEngine.class, TestTemplateCore.class, TestValueStore.class, TestSharedDefaults.class,
		TestProcesses.class, TestTemplateEngineBugs.class,

})
public class AllTemplateEngineTests {
}
