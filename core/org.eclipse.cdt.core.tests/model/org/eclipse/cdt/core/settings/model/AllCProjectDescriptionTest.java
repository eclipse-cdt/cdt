/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCProjectDescriptionTest {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllCProjectDescriptionTest.class.getName());

		// Just add more test cases here as you create them for
		// each class being tested
		suite.addTest(CConfigurationDescriptionReferenceTest.suite());
		suite.addTest(CConfigurationDescriptionExportSettings.suite());
		suite.addTest(ExternalSettingsProviderTest.suite());
		suite.addTest(CfgSettingsTest.suite());
		suite.addTest(CProjectDescriptionDeltaTest.suite());
		suite.addTest(ProjectCreationStateTest.suite());
		suite.addTest(BackwardCompatibilityTest.suite());
		suite.addTest(CProjectDescriptionBasicTest.suite());
		suite.addTest(CProjectDescriptionStorageTest.suite());
		return suite;
	}
}
