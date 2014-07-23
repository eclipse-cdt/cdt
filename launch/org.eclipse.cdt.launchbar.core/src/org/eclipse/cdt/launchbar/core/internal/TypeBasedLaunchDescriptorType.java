/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems (Elena Laskavaia) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.DefaultLaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptorType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class TypeBasedLaunchDescriptorType implements ILaunchDescriptorType {
	protected ILaunchBarManager manager;
	private String typeId;
	private String id;

	public TypeBasedLaunchDescriptorType(String descId, String launchConfigurationTypeId) {
		if (launchConfigurationTypeId == null)
			throw new NullPointerException();
		this.typeId = launchConfigurationTypeId;
		this.id = descId != null ? descId : typeId + ".desc";
	}

	@Override
	public String getId() {
		return id;
	}

	public boolean ownsConfiguration(ILaunchConfiguration element) {
		try {
			return element.getType().getIdentifier().equals(typeId);
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public void init(ILaunchBarManager manager) {
		this.manager = manager;
	}

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		return new DefaultLaunchDescriptor(this, (ILaunchConfiguration) element);
	}

	@Override
	public ILaunchBarManager getManager() {
		return manager;
	}

	@Override
	public boolean ownsLaunchObject(Object element) {
		return element instanceof ILaunchConfiguration
		        && ownsConfiguration((ILaunchConfiguration) element);
	}
}
