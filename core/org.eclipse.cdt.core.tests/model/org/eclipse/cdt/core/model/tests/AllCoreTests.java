/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.language.settings.providers.AllLanguageSettingsProvidersCoreTest;
import org.eclipse.cdt.core.settings.model.AllCProjectDescriptionTest;
import org.eclipse.cdt.core.settings.model.PathSettingsContainerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * AllTests.java
 * This is the main entry point for running this suite of JUnit tests
 * for all tests within the package "org.eclipse.cdt.core.model"
 *
 * @author Judy N. Green
 * @since Jul 19, 2002
 */
public class AllCoreTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllCoreTests.class.getName());

		// Just add more test cases here as you create them for
		// each class being tested
		suite.addTest(AllLanguageInterfaceTest.suite());
		suite.addTest(CModelTest.suite());
		suite.addTest(CModelElementsTest.suite());
		suite.addTest(CModelIdentifierTest.suite());
		suite.addTest(CModelExceptionTest.suite());
		suite.addTest(CModelBuilderInactiveCodeTest.suite());
		suite.addTest(FlagTest.suite());
		suite.addTest(ArchiveTest.suite());
		suite.addTest(BinaryTest.suite());
		suite.addTest(TranslationUnitTest.suite());
		suite.addTest(DeclaratorsTest.suite());
		suite.addTest(MacroTest.suite());
		//		suite.addTest(FailedMacroTest.suite());
		suite.addTest(CPathEntryTest.suite());
		//the CProjectDescriptionTests now groups all New Project Model related tests
		//which includes the CConfigurationDescriptionReferenceTests
		suite.addTest(AllCProjectDescriptionTest.suite());
		suite.addTest(PathSettingsContainerTest.suite());
		suite.addTest(ASTCacheTest.suite());
		suite.addTest(AsmModelBuilderTest.suite());
		suite.addTest(CModelBuilderBugsTest.suite());
		suite.addTest(Bug311189.suite());

		suite.addTest(AllLanguageSettingsProvidersCoreTest.suite());
		return suite;

	}
} // End of AllCoreTests.java
