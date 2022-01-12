/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.Preferences;

/**
 * Provides access to the properties of a debug target.
 */
public interface ITargetProperties {

	public static final String PREF_INSTRUCTION_STEPPING_MODE = "instruction_stepping_mode"; //$NON-NLS-1$

	/**
	 * Adds a property change listener to this target.
	 * Has no affect if the identical listener is already registered.
	 *
	 * @param listener a property change listener
	 */
	void addPropertyChangeListener(Preferences.IPropertyChangeListener listener);

	/**
	 * Removes the given listener from this target.
	 * Has no affect if the listener is not registered.
	 *
	 * @param listener a property change listener
	 */
	void removePropertyChangeListener(Preferences.IPropertyChangeListener listener);
}
