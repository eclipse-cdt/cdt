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

import org.eclipse.cdt.testsrunner.model.ITestItem;

/**
 * Common implementation for the structural item of test hierarchy (test suite
 * or test case).
 */
public abstract class TestItem implements ITestItem {

	/** Test item has no children by default. */
	private static final ITestItem[] NO_CHILDREN = new ITestItem[0];

	/** Test item name. */
	private final String name;

	/** Item parent test suite. May be <code>null</code> for root test suite. */
	private TestSuite parent;

	public TestItem(String name, TestSuite parent) {
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TestSuite getParent() {
		return parent;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public ITestItem[] getChildren() {
		return NO_CHILDREN;
	}

}
