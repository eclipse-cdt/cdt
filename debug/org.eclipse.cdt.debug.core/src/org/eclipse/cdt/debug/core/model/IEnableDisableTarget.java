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

import org.eclipse.debug.core.DebugException;

/**
 * Provides support for enable/disable actions.
 */
public interface IEnableDisableTarget {

	/**
	 * Returns whether this object supports enable/disable operations.
	 *
	 * @return whether this object supports enable/disable operations
	 */
	boolean canEnableDisable();

	/**
	 * Returns whether this object is enabled.
	 *
	 * @return <code>true</code> if this obvject is enabled,
	 * 		   or <code>false</code> otherwise.
	 */
	boolean isEnabled();

	/**
	 * Enables/disables this object
	 *
	 * @param enabled enablement flag value
	 * @throws DebugException
	 */
	void setEnabled(boolean enabled) throws DebugException;
}
