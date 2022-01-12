/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.target;

/**
 * A listener to changes in the list and status of launch targets.
 */
public interface ILaunchTargetListener {

	/**
	 * A launch target was added.
	 *
	 * @param target
	 *            the new launch target
	 */
	default void launchTargetAdded(ILaunchTarget target) {
	}

	/**
	 * A launch target was removed.
	 *
	 * @param target
	 *            the target about to be removed.
	 */
	default void launchTargetRemoved(ILaunchTarget target) {
	}

	/**
	 * The status of a target has changed. Query the target to find out what the
	 * new status is.
	 *
	 * @param target
	 *            the target whose status has changed
	 */
	default void launchTargetStatusChanged(ILaunchTarget target) {
	}

}
