/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.cdt.launchbar.core.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class ConfigBasedLaunchDescriptor extends AbstractLaunchDescriptor implements ILaunchDescriptorConfigBased {
	private final ILaunchDescriptorType type;
	private ILaunchConfiguration config;

	public ConfigBasedLaunchDescriptor(ILaunchDescriptorType type, ILaunchConfiguration config) {
		if (type == null)
			throw new NullPointerException();
		this.type = type;
		this.config = config;
	}

	@Override
	public String getName() {
		if (config == null)
			return "?";
		return config.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return config;
	}

	public ILaunchConfigurationType getLaunchConfigurationType() {
		if (config != null)
			try {
				return config.getType();
			} catch (CoreException e) {
				Activator.log(e); // can happened when config is deleted XXX hide in this case
			}
		if (type instanceof ConfigBasedLaunchDescriptorType) {
			return ((ConfigBasedLaunchDescriptorType) type).getLaunchConfigurationType();
		}
		throw new IllegalStateException("Cannot determine configuration type for " + this);
	}

	@Override
	public String toString() {
		return "LC/" + getName();
	}

	public void setLaunchConfiguration(ILaunchConfiguration config) {
		this.config = config;
	}
}
