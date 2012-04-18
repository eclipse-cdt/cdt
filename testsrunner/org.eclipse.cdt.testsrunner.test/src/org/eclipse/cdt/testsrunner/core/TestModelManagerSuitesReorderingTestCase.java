/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.core;

import org.eclipse.cdt.testsrunner.internal.model.TestModelManager;
import org.eclipse.cdt.testsrunner.internal.model.TestSuite;
import org.eclipse.cdt.testsrunner.model.ITestSuite;


/**
 * Tests on the test suites reordering in TestModelManager.
 */
@SuppressWarnings("nls")
public class TestModelManagerSuitesReorderingTestCase extends TestModelManagerBaseReorderingTestCase {

	@Override
	protected ITestSuite createTestsHierarchy() {
		TestSuite rootTestSuite = new TestSuite(TestModelManager.ROOT_TEST_SUITE_NAME, null);
		rootTestSuite.getChildrenList().add(new TestSuite("item1", rootTestSuite));
		rootTestSuite.getChildrenList().add(new TestSuite("item2", rootTestSuite));
		rootTestSuite.getChildrenList().add(new TestSuite("item3", rootTestSuite));
		return rootTestSuite;
	}

	@Override
	protected void visitTestItem(String name) {
		modelManager.enterTestSuite(name);
		modelManager.exitTestSuite();
	}
	
}
