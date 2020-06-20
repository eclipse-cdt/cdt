/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Norbert Ploett (Siemens AG)
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> Bug 559193
 *******************************************************************************/
package org.eclipse.cdt.core.suite;

import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorOldTest;
import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorTest;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManagerTest;
import org.eclipse.cdt.core.internal.efsextension.tests.EFSExtensionTest;
import org.eclipse.cdt.core.internal.errorparsers.tests.ErrorParserTests;
import org.eclipse.cdt.core.internal.tests.BaseOptionTest;
import org.eclipse.cdt.core.internal.tests.OsgiPreferenceStorageTest;
import org.eclipse.cdt.core.internal.tests.PositionTrackerTest;
import org.eclipse.cdt.core.internal.tests.ResourceLookupTest;
import org.eclipse.cdt.core.internal.tests.StringBuilderTest;
import org.eclipse.cdt.core.language.AllLanguageTests;
import org.eclipse.cdt.core.model.tests.AllCoreTests;
import org.eclipse.cdt.core.model.tests.ElementDeltaTest;
import org.eclipse.cdt.core.model.tests.WorkingCopyTest;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr.AllConstexprEvalTest;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteTests;
import org.eclipse.cdt.core.preferences.tests.TestScopeOfBuildConfigResourceChangesPreference;
import org.eclipse.cdt.core.resources.tests.RefreshScopeTest;
import org.eclipse.cdt.internal.index.tests.IndexTests;
import org.eclipse.cdt.internal.pdom.tests.PDOMTests;
import org.eclipse.cdt.utils.ByteUtilsTest;
import org.eclipse.cdt.utils.CdtVariableResolverTest;
import org.eclipse.cdt.utils.CommandLineUtilTest;
import org.eclipse.cdt.utils.FindProgramLocationTest;
import org.eclipse.cdt.utils.StorableCdtVariablesTest;
import org.eclipse.cdt.utils.UNCPathConverterTest;
import org.eclipse.cdt.utils.WeakHashSetTest;
import org.eclipse.cdt.utils.elf.ElfParserTest;
import org.eclipse.cdt.utils.elf.ElfTest;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author vhirsl
 */
public class AutomatedIntegrationSuite extends TestSuite {

	public AutomatedIntegrationSuite() {
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass, String name) {
		super(theClass, name);
	}

	public AutomatedIntegrationSuite(Class<? extends TestCase> theClass) {
		super(theClass);
	}

	public AutomatedIntegrationSuite(String name) {
		super(name);
	}

	public static Test suite() throws Exception {
		final AutomatedIntegrationSuite suite = new AutomatedIntegrationSuite();

		// Has intermittent failures
		if (System.getProperty("cdt.skip.known.test.failures") == null) {
			suite.addTest(CDescriptorTest.suite());
		}
		suite.addTest(AllConstexprEvalTest.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(CDescriptorOldTest.suite());
		suite.addTest(IEnvironmentVariableManagerTest.suite());
		suite.addTest(ErrorParserTests.suite());
		suite.addTest(AllCoreTests.suite());
		suite.addTest(ElementDeltaTest.suite());
		suite.addTest(WorkingCopyTest.suite());
		suite.addTest(new JUnit4TestAdapter(BaseOptionTest.class));
		suite.addTest(new JUnit4TestAdapter(OsgiPreferenceStorageTest.class));
		suite.addTest(PositionTrackerTest.suite());
		suite.addTest(ResourceLookupTest.suite());
		suite.addTest(StringBuilderTest.suite());
		suite.addTest(AllLanguageTests.suite());
		suite.addTest(RewriteTests.suite());
		suite.addTest(CdtVariableResolverTest.suite());
		suite.addTest(StorableCdtVariablesTest.suite());
		suite.addTest(CommandLineUtilTest.suite());
		suite.addTest(WeakHashSetTest.suite());
		suite.addTest(FindProgramLocationTest.suite());
		suite.addTest(EFSExtensionTest.suite());
		suite.addTest(ByteUtilsTest.suite());
		suite.addTest(UNCPathConverterTest.suite());
		suite.addTest(TestScopeOfBuildConfigResourceChangesPreference.suite());
		suite.addTest(ElfParserTest.suite());
		suite.addTest(new JUnit4TestAdapter(ElfTest.class));

		// Add in PDOM tests
		suite.addTest(PDOMTests.suite());
		suite.addTest(IndexTests.suite());

		suite.addTest(RefreshScopeTest.suite());

		return suite;
	}
}
