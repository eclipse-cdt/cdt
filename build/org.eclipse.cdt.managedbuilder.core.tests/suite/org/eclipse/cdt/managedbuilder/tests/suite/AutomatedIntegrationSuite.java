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

import org.eclipse.cdt.build.core.scannerconfig.tests.CfgScannerConfigProfileManagerTests;
import org.eclipse.cdt.build.core.scannerconfig.tests.GCCSpecsConsoleParserTest;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.managedbuilder.core.regressions.RegressionTests;
import org.eclipse.cdt.managedbuilder.core.tests.BuildDescriptionModelTests;
import org.eclipse.cdt.managedbuilder.core.tests.BuildSystem40Tests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTests20;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildCoreTests_SharedToolOptions;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildDependencyCalculatorTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildDependencyLibsTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildEnvironmentTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildMacrosTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedBuildTCSupportedTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedCommandLineGeneratorTest;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProject21MakefileTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProject30MakefileTests;
import org.eclipse.cdt.managedbuilder.core.tests.ManagedProjectUpdateTests;
import org.eclipse.cdt.managedbuilder.core.tests.MultiVersionSupportTests;
import org.eclipse.cdt.managedbuilder.core.tests.OptionCategoryEnablementTests;
import org.eclipse.cdt.managedbuilder.core.tests.OptionEnablementTests;
import org.eclipse.cdt.managedbuilder.core.tests.PathConverterTest;
import org.eclipse.cdt.managedbuilder.core.tests.ResourceBuildCoreTests;
import org.eclipse.cdt.managedbuilder.language.settings.providers.tests.AllLanguageSettingsProvidersMBSTests;
import org.eclipse.cdt.managedbuilder.templateengine.tests.AllTemplateEngineTests;
import org.eclipse.cdt.projectmodel.tests.BackwardCompatiblityTests;
import org.eclipse.cdt.projectmodel.tests.CProjectDescriptionSerializationTests;
import org.eclipse.cdt.projectmodel.tests.OptionStringListValueTests;
import org.eclipse.cdt.projectmodel.tests.ProjectModelTests;

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
		suite.addTest(CfgScannerConfigProfileManagerTests.suite());
		suite.addTestSuite(GCCSpecsConsoleParserTest.class);

		// language settings providers tests
		suite.addTest(AllLanguageSettingsProvidersMBSTests.suite());

		// managedbuilder.core.tests
		suite.addTest(ManagedBuildDependencyLibsTests.suite());
		suite.addTest(ManagedBuildCoreTests20.suite());
		suite.addTest(ManagedBuildCoreTests.suite());
		suite.addTest(ManagedProjectUpdateTests.suite());
		suite.addTest(ManagedCommandLineGeneratorTest.suite());
		suite.addTest(ResourceBuildCoreTests.suite());
		suite.addTest(ManagedProject21MakefileTests.suite());
		suite.addTest(ManagedProject30MakefileTests.suite());
		suite.addTest(BuildSystem40Tests.suite());
		suite.addTest(ManagedBuildCoreTests_SharedToolOptions.suite());
		suite.addTest(ManagedBuildEnvironmentTests.suite());
		suite.addTest(ManagedBuildMacrosTests.suite());
		suite.addTest(ManagedBuildTCSupportedTest.suite());
		suite.addTest(MultiVersionSupportTests.suite());
		suite.addTest(OptionEnablementTests.suite());
		suite.addTest(OptionCategoryEnablementTests.suite());
		suite.addTest(ManagedBuildDependencyCalculatorTests.suite());
		suite.addTest(BuildDescriptionModelTests.suite());
		suite.addTest(PathConverterTest.suite());

		// managedbuilder.templateengine.tests
		suite.addTest(AllTemplateEngineTests.suite());

		// projectmodel.tests
		suite.addTest(BackwardCompatiblityTests.suite());
		suite.addTest(CProjectDescriptionSerializationTests.suite());
		suite.addTest(OptionStringListValueTests.suite());
		suite.addTest(ProjectModelTests.suite());

		// regression tests
		suite.addTest(RegressionTests.suite());

		//$JUnit-END$
		return suite;
	}
}
