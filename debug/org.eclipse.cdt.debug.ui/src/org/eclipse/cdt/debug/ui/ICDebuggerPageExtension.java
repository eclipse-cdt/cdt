/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui;

/**
 * This interface extension allows the registration of content listeners.
 * Page implementors can use it to notify parents of changes in
 * the page content which will force the parent tab to recalculate its size.
 *
 * @since 7.0
 */
public interface ICDebuggerPageExtension extends ICDebuggerPage {

	/**
	 * @since 7.0
	 */
	public interface IContentChangeListener {

		void contentChanged();
	}

	/**
	 * Adds a listener to this page. This method has no effect
	 * if the same listener is already registered.
	 */
	void addContentChangeListener(IContentChangeListener listener);

	/**
	 * Removes a listener from this list. Has no effect if
	 * the same listener was not already registered.
	 */
	void removeContentChangeListener(IContentChangeListener listener);
}
