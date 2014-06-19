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

import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class DefaultLaunchConfigurationDescriptor implements ILaunchConfigurationDescriptor {

	final ILaunchConfiguration config;
	
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

}
