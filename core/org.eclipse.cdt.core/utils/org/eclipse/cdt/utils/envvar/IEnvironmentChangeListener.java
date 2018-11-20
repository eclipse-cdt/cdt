/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

/**
 * Interface for listeners to changes in environment variables defined by user
 * on CDT Environment page in Preferences.
 *
 * @since 5.5
 */
public interface IEnvironmentChangeListener {
	/**
	 * Indicates that environment variables have been changed.
	 *
	 * @param event - details of the event.
	 */
	public void handleEvent(IEnvironmentChangeEvent event);
}
