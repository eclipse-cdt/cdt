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

import org.eclipse.cdt.core.language.settings.providers.AllLanguageSettingsProvidersCoreTests;
import org.eclipse.cdt.core.settings.model.AllCProjectDescriptionTests;
import org.eclipse.cdt.core.settings.model.PathSettingsContainerTests;

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
		suite.addTest(AllLanguageInterfaceTests.suite());
		suite.addTest(CModelTests.suite());
		suite.addTest(CModelElementsTests.suite());
		suite.addTest(CModelIdentifierTests.suite());
		suite.addTest(CModelExceptionTest.suite());
		suite.addTest(CModelBuilderInactiveCodeTest.suite());
		suite.addTest(FlagTests.suite());
		suite.addTest(ArchiveTests.suite());
		suite.addTest(BinaryTests.suite());
		suite.addTest(TranslationUnitTests.suite());
		suite.addTest(DeclaratorsTests.suite());
		suite.addTest(MacroTests.suite());
		//		suite.addTest(FailedMacroTests.suite());
		suite.addTest(CPathEntryTest.suite());
		//the CProjectDescriptionTests now groups all New Project Model related tests
		//which includes the CConfigurationDescriptionReferenceTests
		suite.addTest(AllCProjectDescriptionTests.suite());
		suite.addTest(PathSettingsContainerTests.suite());
		suite.addTest(ASTCacheTests.suite());
		suite.addTest(AsmModelBuilderTest.suite());
		suite.addTest(CModelBuilderBugsTest.suite());
		suite.addTest(Bug311189.suite());

		suite.addTest(AllLanguageSettingsProvidersCoreTests.suite());
		return suite;

	}
} // End of AllCoreTests.java
