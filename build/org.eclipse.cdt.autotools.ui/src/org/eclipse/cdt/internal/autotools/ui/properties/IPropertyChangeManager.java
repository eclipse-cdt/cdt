/*******************************************************************************
 * Copyright (c) 2007, 2012 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.core.resources.IProject;

public interface IPropertyChangeManager {

	/**
	 * Add a project property listener for given project.
	 *
	 * @param project the project to which the listener is interested
	 * @param listener the listener to notify
	 */
	void addProjectPropertyListener(IProject project, IProjectPropertyListener listener);

	/**
	 * Remove a project property listener.
	 *
	 * @param listener the listener to remove
	 */
	void removeProjectPropertyListener(IProject project, IProjectPropertyListener listener);

	/**
	 * Notify all listeners of project that a property has changed.
	 *
	 * @param project the project for which the property has changed
	 * @param property the property that has changed
	 */
	void notifyPropertyListeners(IProject project, String property);

}
