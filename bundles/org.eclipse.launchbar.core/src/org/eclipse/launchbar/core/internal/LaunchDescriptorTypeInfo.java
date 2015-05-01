package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class LaunchDescriptorTypeInfo {
	private final String id;
	private int priority;
	private IConfigurationElement element;
	private ILaunchDescriptorType type;

	public LaunchDescriptorTypeInfo(IConfigurationElement element) {
		this.id = element.getAttribute("id"); //$NON-NLS-1$
		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		this.priority = 1;
		if (priorityStr != null) {
			try {
				priority = Integer.parseInt(priorityStr);
			} catch (NumberFormatException e) {
				// Log it but keep going with the default
				Activator.log(e);
			}
		}
		this.element = element;
	}

	// Used for testing
	public LaunchDescriptorTypeInfo(String id, int priority, ILaunchDescriptorType type) {
		this.id = id;
		this.priority = priority;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	public ILaunchDescriptorType getType() throws CoreException {
		if (type == null) {
			type = (ILaunchDescriptorType) element.createExecutableExtension("class"); //$NON-NLS-1$
			element = null;
		}
		return type;
	}
}