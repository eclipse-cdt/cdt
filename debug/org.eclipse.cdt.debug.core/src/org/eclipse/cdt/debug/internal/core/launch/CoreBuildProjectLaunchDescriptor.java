/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

/**
 * A launch descriptor representing a project built with the new Core Build system.
 */
public class CoreBuildProjectLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {

	private final IProject project;
	private final CoreBuildLaunchDescriptorType type;

	public CoreBuildProjectLaunchDescriptor(CoreBuildLaunchDescriptorType type, IProject project) {
		this.type = type;
		this.project = project;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IProject.class.equals(adapter)) {
			return (T) project;
		} else {
			return super.getAdapter(adapter);
		}
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}

}
