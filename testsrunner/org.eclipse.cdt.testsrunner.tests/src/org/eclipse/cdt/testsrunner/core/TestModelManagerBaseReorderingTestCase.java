/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov  - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.internal.model.TestModelManager;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

import junit.framework.TestCase;

/**
 * Base test case for test suites & test cases reordering in TestModelManager.
 */
@SuppressWarnings("nls")
public abstract class TestModelManagerBaseReorderingTestCase extends TestCase {

	protected TestModelManager modelManager;
	protected List<String> expectedSuitesOrder = new ArrayList<>();

	protected abstract ITestSuite createTestsHierarchy();

	protected abstract void visitTestItem(String name);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		modelManager = new TestModelManager(createTestsHierarchy(), false);
		modelManager.testingStarted();
	}

	@Override
	protected void tearDown() throws Exception {
		modelManager.testingFinished();
		ITestItem[] rootTestSuiteChildren = modelManager.getRootSuite().getChildren();
		assertEquals("Unexpected children count", expectedSuitesOrder.size(), rootTestSuiteChildren.length);
		for (int i = 0; i < rootTestSuiteChildren.length; i++) {
			assertEquals("Unexpected child name", expectedSuitesOrder.get(i), rootTestSuiteChildren[i].getName());
		}
	}

	private void visitTestItemWithExpectation(String name) {
		visitTestItem(name);
		expectedSuitesOrder.add(name);
	}

	public void testNoReordering() {
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("item2");
		visitTestItemWithExpectation("item3");
	}

	public void testItemAdd() {
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("itemNew");
		visitTestItemWithExpectation("item2");
		visitTestItemWithExpectation("item3");
	}

	public void testItemAddToBeginAndEnd() {
		visitTestItemWithExpectation("itemNew");
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("item2");
		visitTestItemWithExpectation("item3");
		visitTestItemWithExpectation("itemNew2");
	}

	public void testItemRemove() {
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("item3");
	}

	public void testItemRemoveFromBeginAndEnd() {
		visitTestItemWithExpectation("item2");
	}

	public void testItemRemoveAndAdd() {
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("itemNew");
		visitTestItemWithExpectation("item3");
	}

	public void testItemOrderChange() {
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("item3");
		visitTestItemWithExpectation("item2");
	}

	public void testItemReverse() {
		visitTestItemWithExpectation("item3");
		visitTestItemWithExpectation("item2");
		visitTestItemWithExpectation("item1");
	}

	public void testItemVisitTwice() {
		visitTestItem("item1");
		visitTestItemWithExpectation("item2");
		visitTestItemWithExpectation("item1");
		visitTestItem("item3");
		visitTestItemWithExpectation("item3");
	}

	public void testItemVisitTwiceAndReorder() {
		visitTestItem("item3");
		visitTestItemWithExpectation("item1");
		visitTestItemWithExpectation("item3");
		visitTestItemWithExpectation("item2");
	}

}
