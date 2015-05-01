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
package org.eclipse.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A reusable descriptor for wrapping projects that can be used by descriptor types
 * that map to projects.
 */
public class ProjectLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {

	private final ILaunchDescriptorType type;
	private final IProject project;

	public ProjectLaunchDescriptor(ILaunchDescriptorType type, IProject project) {
		this.type = type;
		this.project = project;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IProject.class.equals(adapter)) {
			return adapter.cast(project);
		}
		return super.getAdapter(adapter);
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
