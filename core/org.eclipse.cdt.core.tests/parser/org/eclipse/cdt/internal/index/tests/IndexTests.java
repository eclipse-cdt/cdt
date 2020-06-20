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

import org.eclipse.cdt.core.parser.tests.ast2.cxx14.GenericLambdaIndexTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx14.ReturnTypeDeductionIndexTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.StructuredBindingIndexTest;
import org.eclipse.cdt.core.parser.tests.ast2.cxx17.TemplateAutoIndexTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the indexer tests
 */
public class IndexTests extends TestSuite {
	public static Test suite() {
		TestSuite suite = new IndexTests();

		suite.addTest(IndexCompositeTest.suite());
		suite.addTest(IndexListenerTest.suite());
		suite.addTest(IndexLocationTest.suite());
		suite.addTest(IndexSearchTest.suite());
		suite.addTest(IndexIncludeTest.suite());
		suite.addTest(IndexUpdateTest.suite());
		suite.addTest(IndexUpdateMultiFileTest.suite());
		suite.addTest(IndexBugsTest.suite());
		suite.addTest(IndexNamesTest.suite());
		suite.addTest(TeamSharedIndexTest.suite());
		suite.addTest(IndexProviderManagerTest.suite());
		suite.addTest(IndexMultiVariantHeaderTest.suite());
		suite.addTest(IndexMultiFileTest.suite());

		// C++14 index test suites
		suite.addTestSuite(ReturnTypeDeductionIndexTest.class);
		suite.addTestSuite(GenericLambdaIndexTest.class);

		// C++17 index test suites
		suite.addTestSuite(TemplateAutoIndexTest.class);
		suite.addTestSuite(StructuredBindingIndexTest.class);

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
