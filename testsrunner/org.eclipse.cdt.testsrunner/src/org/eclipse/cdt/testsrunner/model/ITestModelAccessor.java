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
package org.eclipse.cdt.testsrunner.model;

/**
 * Provides an access to the tests model.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestModelAccessor {

	/**
	 * Checks whether the specified testing item (case or suite) is running now.
	 *
	 * @param item the interested item
	 * @return whether the item is running now
	 */
	public boolean isCurrentlyRunning(ITestItem item);

	/**
	 * Provides access to the root test suite.
	 *
	 * @return root test suite
	 */
	public ITestSuite getRootSuite();

	/**
	 * Adds the given listener to this registered listeners collection.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addChangesListener(ITestingSessionListener listener);

	/**
	 * Removes the given listener from registered listeners collection.
	 * Has no effect if the listener is not already registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeChangesListener(ITestingSessionListener listener);

}
