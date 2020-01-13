/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A reusable descriptor for wrapping projects that can be used by descriptor
 * types that map to projects.
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

	@Override
	public String toString() {
		return getName(); // for debugging purposes
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectLaunchDescriptor other = (ProjectLaunchDescriptor) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
