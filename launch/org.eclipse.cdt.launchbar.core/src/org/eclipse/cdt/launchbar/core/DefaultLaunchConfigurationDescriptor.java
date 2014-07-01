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
package org.eclipse.cdt.launchbar.core;

import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.cdt.launchbar.core.internal.LocalTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;

public class DefaultLaunchConfigurationDescriptor implements ILaunchConfigurationDescriptor {

	protected ILaunchConfiguration config;
	
	public DefaultLaunchConfigurationDescriptor(ILaunchConfiguration config) {
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
	public ILaunchConfigurationType getLaunchConfigurationType() throws CoreException{
		return config.getType();
	}

	@Override
	public boolean matches(ILaunchConfiguration launchConfiguration) {
		return config.equals(launchConfiguration);
	}

	@Override
	public ILaunchTarget getLaunchTarget(String id) {
		return LocalTarget.ID.equals(id) ? LaunchBarManager.getLocalLaunchTarget() : null;
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		return new ILaunchTarget[] { LaunchBarManager.getLocalLaunchTarget() };
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		// nothing to do 
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
		// nothing to do
	}

}
