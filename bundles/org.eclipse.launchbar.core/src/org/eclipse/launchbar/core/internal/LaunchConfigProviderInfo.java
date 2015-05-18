package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;

public class LaunchConfigProviderInfo {
	private final String descriptorTypeId;
	private final int priority;
	private IConfigurationElement element;
	private ILaunchConfigurationProvider provider;

	public LaunchConfigProviderInfo(IConfigurationElement element) {
		this.descriptorTypeId = element.getAttribute("descriptorType"); //$NON-NLS-1$

		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		int priorityNum;
		try {
			priorityNum = Integer.parseInt(priorityStr);
		} catch (NumberFormatException e) {
			priorityNum = 0;
		}
		priority = priorityNum;

		this.element = element;
	}

	public String getDescriptorTypeId() {
		return descriptorTypeId;
	}

	public int getPriority() {
		return priority;
	}

	public ILaunchConfigurationProvider getProvider() throws CoreException {
		if (provider == null) {
			provider = (ILaunchConfigurationProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
			element = null;
		}
		return provider;
	}
}