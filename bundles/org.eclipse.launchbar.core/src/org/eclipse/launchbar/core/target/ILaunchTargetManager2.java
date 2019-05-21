/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
