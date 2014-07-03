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
package org.eclipse.cdt.launchbar.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;

public class DefaultLaunchConfigurationDescriptor implements ILaunchConfigurationDescriptor {

	private final ILaunchBarManager manager;
	private ILaunchConfiguration config;
	private ILaunchMode[] launchModes;
	
	public DefaultLaunchConfigurationDescriptor(ILaunchBarManager manager, ILaunchConfiguration config) {
		this.manager = manager;
		this.config = config;
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration() throws CoreException {
		return config;
	}

	@Override
	public boolean matches(ILaunchConfiguration launchConfiguration) {
		return config.equals(launchConfiguration);
	}

	@Override
	public ILaunchTarget getLaunchTarget(String id) {
		return LocalTarget.ID.equals(id) ? manager.getLocalLaunchTarget() : null;
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		return new ILaunchTarget[] { manager.getLocalLaunchTarget() };
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		// nothing to do 
	}

	@Override
	public ILaunchMode[] getLaunchModes() throws CoreException {
		if (launchModes == null) {
			List<ILaunchMode> mymodes = new ArrayList<>();
			ILaunchConfigurationType type = config.getType();
			ILaunchMode[] modes = DebugPlugin.getDefault().getLaunchManager().getLaunchModes();
			for (ILaunchMode mode : modes) {
				if (type.supportsMode(mode.getIdentifier())) {
					mymodes.add(mode);
				}
			}
			launchModes = mymodes.toArray(new ILaunchMode[mymodes.size()]);
		}
		return launchModes;
	}

	@Override
	public ILaunchMode getLaunchMode(String id) throws CoreException {
		for (ILaunchMode mode : getLaunchModes()) {
			if (mode.getIdentifier().equals(id))
				return mode;
		}
		return null;
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
		// nothing to do
	}

}
