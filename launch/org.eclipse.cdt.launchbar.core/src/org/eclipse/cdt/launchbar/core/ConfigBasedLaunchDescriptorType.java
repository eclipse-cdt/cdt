/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Elena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ConfigBasedLaunchDescriptorType extends AbstarctLaunchDescriptorType implements ILaunchDescriptorType {
	private String id;
	private String typeId;

	public ConfigBasedLaunchDescriptorType(String descTypeId, String launchConfigurationTypeId) {
		if (launchConfigurationTypeId == null)
			throw new NullPointerException();
		this.typeId = launchConfigurationTypeId;
		this.id = descTypeId != null ? descTypeId : launchConfigurationTypeId;
	}

	public ConfigBasedLaunchDescriptorType(String launchConfigurationTypeId) {
		this(null, launchConfigurationTypeId);
	}

	public boolean ownsConfiguration(ILaunchConfiguration element) {
		try {
			return element.getType().getIdentifier().equals(typeId);
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		return new ConfigBasedLaunchDescriptor(this, (ILaunchConfiguration) element);
	}

	@Override
	public boolean ownsLaunchObject(Object element) {
		return element instanceof ILaunchConfiguration
		        && ownsConfiguration((ILaunchConfiguration) element);
	}

	@Override
	public String getId() {
		return id;
	}
}
