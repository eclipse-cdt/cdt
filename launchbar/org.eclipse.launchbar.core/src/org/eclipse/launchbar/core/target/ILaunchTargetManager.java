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

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * The manager for the launch targets. It is registered as an OSGi service.
 *
 * @noimplement not to be implemented by clients
 */
public interface ILaunchTargetManager {

	/**
	 * The type id for the local launch target type. It is the default launch
	 * target type. It represents launching on the underlying machine on which
	 * we are running. There is only one launch target of this type which
	 * represents that machine.
	 */
	final String localLaunchTargetTypeId = "org.eclipse.launchbar.core.launchTargetType.local"; //$NON-NLS-1$

	/**
	 * The list of all launch targets.
	 *
	 * @return list of launch targets
	 */
	ILaunchTarget[] getLaunchTargets();

	/**
	 * The list of launch targets with a given type id.
	 *
	 * @param typeId
	 *            the launch target type id
	 * @return list of launch targets
	 */
	ILaunchTarget[] getLaunchTargetsOfType(String typeId);

	/**
	 * Return the launch target with the given typeId and name
	 *
	 * @param typeId
	 *            type of the launch target
	 * @param id
	 *            id of the launch target
	 * @return the launch target
	 */
	ILaunchTarget getLaunchTarget(String typeId, String id);

	/**
	 * Return the status of the launch target.
	 *
	 * @param target
	 *            the launch target
	 * @return status
	 */
	TargetStatus getStatus(ILaunchTarget target);

	/**
	 * Add a launch target with the given typeId, id, and name.
	 *
	 * @param typeId
	 *            type id of the launch target
	 * @param id
	 *            id for the target.
	 * @return the created launch target
	 */
	ILaunchTarget addLaunchTarget(String typeId, String id);

	/**
	 * Removes a launch target.
	 *
	 * @param target
	 *            the launch target to remove
	 */
	void removeLaunchTarget(ILaunchTarget target);

	/**
	 * The status of the launch target has changed.
	 *
	 * @param target
	 */
	void targetStatusChanged(ILaunchTarget target);

	/**
	 * What is the default target to use for this launch configuration.
	 *
	 * @param configuration
	 *            launch configuration or null if not set
	 * @return default target for this launch configuration
	 */
	ILaunchTarget getDefaultLaunchTarget(ILaunchConfiguration configuration);

	/**
	 * Set the default target for the given launch configuration.
	 *
	 * @param configuration
	 *            launch configuration
	 * @param target
	 *            default target for this launch configuration
	 */
	void setDefaultLaunchTarget(ILaunchConfiguration configuration, ILaunchTarget target);

	/**
	 * Add a listener.
	 *
	 * @param listener
	 */
	void addListener(ILaunchTargetListener listener);

	/**
	 * Remove a listener.
	 *
	 * @param listener
	 */
	void removeListener(ILaunchTargetListener listener);

}
