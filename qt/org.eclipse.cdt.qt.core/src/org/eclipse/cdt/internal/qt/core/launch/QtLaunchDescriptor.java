package org.eclipse.cdt.internal.qt.core.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class QtLaunchDescriptor extends PlatformObject implements ILaunchDescriptor {

	private final QtLaunchDescriptorType type;
	private final IProject project;

	public QtLaunchDescriptor(QtLaunchDescriptorType type, IProject project) {
		this.type = type;
		this.project = project;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}

	public IProject getProject() {
		return project;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IProject.class)) {
			return (T) project;
		} else {
			return super.getAdapter(adapter);
		}
	}

}
