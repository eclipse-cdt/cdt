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
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestSuite;

/**
 * Represents the test suite of the tests hierarchy.
 */
public class TestSuite extends TestItem implements ITestSuite {

	/**
	 * Stores child test suites and test cases.
	 *
	 * @note Children order is important.
	 */
	private List<TestItem> children = new ArrayList<>();

	public TestSuite(String name, TestSuite parent) {
		super(name, parent);
	}

	@Override
	public Status getStatus() {
		Status result = Status.NotRun;
		for (TestItem testItem : children) {
			Status childStatus = testItem.getStatus();
			if (result.compareTo(childStatus) < 0) {
				result = childStatus;
			}
		}
		return result;
	}

	@Override
	public int getTestingTime() {
		int result = 0;
		for (TestItem testItem : children) {
			result += testItem.getTestingTime();
		}
		return result;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public TestItem[] getChildren() {
		return children.toArray(new TestItem[children.size()]);
	}

	@Override
	public void visit(IModelVisitor visitor) {
		visitor.visit(this);
		for (TestItem testItem : children) {
			testItem.visit(visitor);
		}
		visitor.leave(this);
	}

	/**
	 * Returns list of children for the test suite.
	 *
	 * @return children list
	 */
	public List<TestItem> getChildrenList() {
		return children;
	}

}
