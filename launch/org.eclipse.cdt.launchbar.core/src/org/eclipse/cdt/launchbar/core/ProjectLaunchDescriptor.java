package org.eclipse.cdt.launchbar.core;

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
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (IProject.class.equals(adapter)) {
			return project;
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
