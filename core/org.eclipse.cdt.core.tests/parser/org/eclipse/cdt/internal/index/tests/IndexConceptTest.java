/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConceptDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConcept;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;

import junit.framework.TestSuite;

/**
 * AST tests for C++20 concepts via PDOM.
 */
public abstract class IndexConceptTest extends IndexBindingResolutionTestBase {
	public static class SingleProject extends IndexConceptTest {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true /* cpp */));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	public static class SingleProjectReindexed extends IndexConceptTest {
		public SingleProjectReindexed() {
			setStrategy(new SinglePDOMReindexedTestStrategy(true /* cpp */));
		}

		public static TestSuite suite() {
			return suite(SingleProjectReindexed.class);
		}
	}

	public static class ProjectWithDepProj extends IndexConceptTest {
		public ProjectWithDepProj() {
			setStrategy(new ReferencedProject(true /* cpp */));
		}

		public static TestSuite suite() {
			return suite(ProjectWithDepProj.class);
		}
	}

	private static void cxx20SetUp() {
		TestScannerProvider.sDefinedSymbols.put("__cpp_concepts", "201907L");
	}

	@Override
	public void setUp() throws Exception {
		cxx20SetUp();
		super.setUp();
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(SingleProject.suite());
		suite.addTest(SingleProjectReindexed.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}

	//  template<typename T>
	//  concept A = true;

	//  template<typename T>
	//  concept B = A<T>;
	public void testConceptDefinitionFromHeader() throws Exception {
		checkBindings();

		ICPPConcept concept = getBindingFromASTName("B = A<T>", 1);
		ICPPASTConceptDefinition decl = concept.getConceptDefinition();
	}
}
