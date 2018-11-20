/*******************************************************************************
 * Copyright (c) 2008, 2011 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.util.EventListener;
import java.util.List;

/**
 * Listener interface for finding out when the list of Executable objects in the
 * workspace changes or when the objects themselves change.
 *
 * <p>
 * Executable objects are ephemeral representations of Eclipse workspace model
 * elements. A particular executable in the workspace is typically represented
 * by many Executable objects. For example, an executable in the workspace that
 * changes twice can cause the listener's {@link #executablesChanged(List)} to
 * be called with a different Executable instance each of the two times it's invoked.
 *
 */
public interface IExecutablesChangeListener extends EventListener {

	/**
	 * Called whenever the list of executables in the workspace changes. Many
	 * types of operations cause the list to change, for example:
	 * <ul>
	 * <li>project is built for the first time
	 * <li>project with executables already in place is open, closed, removed or
	 * cleaned
	 * <li>user deletes one or more executables
	 * </ul>
	 *
	 * Clients can get the list by calling {@link ExecutablesManager#getExecutables()}
	 *
	 * @since 7.0
	 */
	public void executablesListChanged();

	/**
	 * Called whenever one or more executables have changed, e.g. when a project
	 * is rebuilt. This is sometimes also called if the executable has not
	 * changed (i.e., the file on disk) but the information the Executable
	 * object provides has changed. One such case is when there's a change in
	 * the source locators, as such locators guide the Executable in finding the
	 * local path for the compile path.
	 *
	 * <p>
	 * The Executable instances in the given list have had their caches flushed
	 * by ExecutableManager. Clients that keep references to Executable objects
	 * must keep in mind that those particular instances may no longer be
	 * managed by ExecutableManager and as such it is the client's
	 * responsibility to tell those instances to flush when this listener method
	 * is called. E.g.,
	 *
	 * <p><pre>
	 * public void executablesChanged(List<Executable> executables) {
	 *    for (Executable e : executables) {
	 *       if (e.equals(fExecutable) {
	 *          fExecutable.setRefreshSourceFiles(true);
	 *       }
	 *    }
	 * }
	 * </pre>
	 *
	 * <p>
	 * This is not called when an executable is added or removed
	 *
	 * @param executables
	 * @since 7.0
	 */
	public void executablesChanged(List<Executable> executables);
}