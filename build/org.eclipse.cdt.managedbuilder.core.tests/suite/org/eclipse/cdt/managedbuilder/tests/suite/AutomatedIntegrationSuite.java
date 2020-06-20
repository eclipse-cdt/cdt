/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.tests.suite;

import org.eclipse.cdt.build.core.scannerconfig.tests.CfgScannerConfigProfileManagerTest;
import org.eclipse.cdt.build.core.scannerconfig.tests.GCCSpecsConsoleParserTest;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.managedbuilder.core.regressions.RegressionTest;
import org.eclipse.cdt.managedbuilder.core.tests.BuildDescriptionModelTest;
import org.eclipse.cdt.managedbuilder.core.tests.BuildSystem40Test;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTests20;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTests_SharedToolOptions;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildDependencyCalculatorTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildDependencyLibsTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildEnvironmentTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildMacrosTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildTCSupportedTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedCommandLineGeneratorTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProject21MakefileTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProject30MakefileTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProjectUpdateTest;
import org.eclipse.cdt.managedbuilder.core.tests.MultiVersionSupportTest;
import org.eclipse.cdt.managedbuilder.core.tests.OptionCategoryEnablementTest;
import org.eclipse.cdt.managedbuilder.core.tests.OptionEnablementTest;
import org.eclipse.cdt.managedbuilder.core.tests.PathConverterTest;
import org.eclipse.cdt.managedbuilder.core.tests.ResourceBuildCoreTest;
import org.eclipse.cdt.managedbuilder.language.settings.providers.tests.AllLanguageSettingsProvidersMBSTests;
import org.eclipse.cdt.managedbuilder.templateengine.tests.AllTemplateEngineTests;
import org.eclipse.cdt.projectmodel.tests.BackwardCompatiblityTest;
import org.eclipse.cdt.projectmodel.tests.CProjectDescriptionSerializationTest;
import org.eclipse.cdt.projectmodel.tests.OptionStringListValueTest;
import org.eclipse.cdt.projectmodel.tests.ProjectModelTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Main TestSuite for all the managed build tests
 */
public class AutomatedIntegrationSuite {
	public static void main(String[] args) {
		junit.textui.TestRunner.run(AutomatedIntegrationSuite.suite());
	}

	public static Test suite() {
		CCorePlugin.getDefault().getPluginPreferences().setValue(CCorePlugin.PREF_INDEXER,
				IPDOMManager.ID_FAST_INDEXER);

		TestSuite suite = new TestSuite("Test for org.eclipse.cdt.managedbuild.core.tests");
		//$JUnit-BEGIN$
		// Preconditions
		suite.addTestSuite(Preconditions.class);

		// build.core.scannerconfig.tests
		suite.addTest(CfgScannerConfigProfileManagerTest.suite());
		suite.addTestSuite(GCCSpecsConsoleParserTest.class);

		// language settings providers tests
		suite.addTest(AllLanguageSettingsProvidersMBSTests.suite());

		// managedbuilder.core.tests
		suite.addTest(ManagedBuildDependencyLibsTest.suite());
		suite.addTest(ManagedBuildCoreTests20.suite());
		suite.addTest(ManagedBuildCoreTest.suite());
		suite.addTest(ManagedProjectUpdateTest.suite());
		suite.addTest(ManagedCommandLineGeneratorTest.suite());
		suite.addTest(ResourceBuildCoreTest.suite());
		suite.addTest(ManagedProject21MakefileTest.suite());
		suite.addTest(ManagedProject30MakefileTest.suite());
		suite.addTest(BuildSystem40Test.suite());
		suite.addTest(ManagedBuildCoreTests_SharedToolOptions.suite());
		suite.addTest(ManagedBuildEnvironmentTest.suite());
		suite.addTest(ManagedBuildMacrosTest.suite());
		suite.addTest(ManagedBuildTCSupportedTest.suite());
		suite.addTest(MultiVersionSupportTest.suite());
		suite.addTest(OptionEnablementTest.suite());
		suite.addTest(OptionCategoryEnablementTest.suite());
		suite.addTest(ManagedBuildDependencyCalculatorTest.suite());
		suite.addTest(BuildDescriptionModelTest.suite());
		suite.addTest(PathConverterTest.suite());

		// managedbuilder.templateengine.tests
		suite.addTest(AllTemplateEngineTests.suite());

		// projectmodel.tests
		suite.addTest(BackwardCompatiblityTest.suite());
		suite.addTest(CProjectDescriptionSerializationTest.suite());
		suite.addTest(OptionStringListValueTest.suite());
		suite.addTest(ProjectModelTest.suite());

		// regression tests
		suite.addTest(RegressionTest.suite());

		//$JUnit-END$
		return suite;
	}
}
