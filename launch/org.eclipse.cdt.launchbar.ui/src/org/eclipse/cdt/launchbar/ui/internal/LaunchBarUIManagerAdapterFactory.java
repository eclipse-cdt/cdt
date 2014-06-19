/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.core.runtime.IAdapterFactory;

public class LaunchBarUIManagerAdapterFactory implements IAdapterFactory {

	private static Map<ILaunchBarManager, LaunchBarUIManager> uiProvider = new HashMap<>();
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ILaunchBarManager && adapterType.equals(LaunchBarUIManager.class)) {
			ILaunchBarManager manager = (ILaunchBarManager) adaptableObject;
			LaunchBarUIManager uiManager = uiProvider.get(manager);
			if (uiManager == null) {
				uiManager = new LaunchBarUIManager(manager);
				uiProvider.put(manager, uiManager);
			}
			return uiManager;
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { LaunchBarUIManager.class };
	}

}
