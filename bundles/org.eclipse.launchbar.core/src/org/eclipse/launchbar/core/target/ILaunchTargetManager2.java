/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
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
 * Launch target manager extensions.
 * 
 * @noimplement not to be implemented by clients
 */
public interface ILaunchTargetManager2 {
	
	/**
	 * Add a launch target with the given typeId, id, and name but no notification.
	 * 
	 * @param typeId
	 *            type id of the launch target
	 * @param id
	 *            id for the target.
	 * @return the created launch target
	 */
	ILaunchTarget addLaunchTargetNoNotify(String typeId, String id);


}
