/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import org.eclipse.cdt.core.parser.tests.ast2.cxx14.GenericLambdaIndexTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.ReturnTypeDeductionIndexTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.StructuredBindingIndexTests;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.TemplateAutoIndexTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the indexer tests
 */
public class IndexTests extends TestSuite {
	public static Test suite() {
		TestSuite suite = new IndexTests();

		suite.addTest(IndexCompositeTests.suite());
		suite.addTest(IndexListenerTest.suite());
		suite.addTest(IndexLocationTest.suite());
		suite.addTest(IndexSearchTest.suite());
		suite.addTest(IndexIncludeTest.suite());
		suite.addTest(IndexUpdateTests.suite());
		suite.addTest(IndexUpdateMultiFileTest.suite());
		suite.addTest(IndexBugsTests.suite());
		suite.addTest(IndexNamesTests.suite());
		suite.addTest(TeamSharedIndexTest.suite());
		suite.addTest(IndexProviderManagerTest.suite());
		suite.addTest(IndexMultiVariantHeaderTest.suite());
		suite.addTest(IndexMultiFileTest.suite());

		// C++14 index test suites
		suite.addTestSuite(ReturnTypeDeductionIndexTests.class);
		suite.addTestSuite(GenericLambdaIndexTests.class);

		// C++17 index test suites
		suite.addTestSuite(TemplateAutoIndexTests.class);
		suite.addTestSuite(StructuredBindingIndexTests.class);

		IndexCPPBindingResolutionBugs.addTests(suite);
		IndexCPPBindingResolutionTest.addTests(suite);
		IndexGPPBindingResolutionTest.addTests(suite);
		IndexCPPTemplateResolutionTest.addTests(suite);
		IndexCBindingResolutionBugs.addTests(suite);
		IndexCBindingResolutionTest.addTests(suite);
		IndexCPPVariableTemplateResolutionTest.addTests(suite);

		return suite;
	}
}
