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

import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorOldTests;
import org.eclipse.cdt.core.cdescriptor.tests.CDescriptorTests;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManagerTests;
import org.eclipse.cdt.core.internal.efsextension.tests.EFSExtensionTests;
import org.eclipse.cdt.core.internal.errorparsers.tests.ErrorParserTestSuite;
import org.eclipse.cdt.core.internal.tests.PositionTrackerTests;
import org.eclipse.cdt.core.internal.tests.ResourceLookupTests;
import org.eclipse.cdt.core.internal.tests.StringBuilderTest;
import org.eclipse.cdt.core.language.AllLanguageTestSuite;
import org.eclipse.cdt.core.model.tests.AllCoreTestSuite;
import org.eclipse.cdt.core.model.tests.ElementDeltaTests;
import org.eclipse.cdt.core.model.tests.WorkingCopyTests;
import org.eclipse.cdt.core.parser.tests.ParserTestSuite;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr.AllConstexprEvalTestSuite;
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteTestSuite;
import org.eclipse.cdt.core.preferences.tests.TestScopeOfBuildConfigResourceChangesPreference;
import org.eclipse.cdt.core.resources.tests.RefreshScopeTests;
import org.eclipse.cdt.internal.index.tests.IndexTestSuite;
import org.eclipse.cdt.internal.pdom.tests.PDOMTestSuite;
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
			suite.addTest(CDescriptorTests.suite());
		}
		suite.addTest(AllConstexprEvalTestSuite.suite());
		suite.addTest(ParserTestSuite.suite());
		suite.addTest(CDescriptorOldTests.suite());
		suite.addTest(IEnvironmentVariableManagerTests.suite());
		suite.addTest(ErrorParserTestSuite.suite());
		suite.addTest(AllCoreTestSuite.suite());
		suite.addTest(ElementDeltaTests.suite());
		suite.addTest(WorkingCopyTests.suite());
		suite.addTest(PositionTrackerTests.suite());
		suite.addTest(ResourceLookupTests.suite());
		suite.addTest(StringBuilderTest.suite());
		suite.addTest(AllLanguageTestSuite.suite());
		suite.addTest(RewriteTestSuite.suite());
		suite.addTest(CdtVariableResolverTest.suite());
		suite.addTest(StorableCdtVariablesTest.suite());
		suite.addTest(CommandLineUtilTest.suite());
		suite.addTest(WeakHashSetTest.suite());
		suite.addTest(FindProgramLocationTest.suite());
		suite.addTest(EFSExtensionTests.suite());
		suite.addTest(ByteUtilsTest.suite());
		suite.addTest(UNCPathConverterTest.suite());
		suite.addTest(TestScopeOfBuildConfigResourceChangesPreference.suite());
		suite.addTest(ElfParserTest.suite());
		suite.addTest(new JUnit4TestAdapter(ElfTest.class));

		// Add in PDOM tests
		suite.addTest(PDOMTestSuite.suite());
		suite.addTest(IndexTestSuite.suite());

		suite.addTest(RefreshScopeTests.suite());

		return suite;
	}
}
