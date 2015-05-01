package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.IConfigurationElement;

public class LaunchConfigTypeInfo {
	private final String descriptorTypeId;
	private final String targetTypeId;
	private final String launchConfigTypeId;

	public LaunchConfigTypeInfo(IConfigurationElement element) {
		this.descriptorTypeId = element.getAttribute("descriptorType"); //$NON-NLS-1$
		this.targetTypeId = element.getAttribute("targetType"); //$NON-NLS-1$
		this.launchConfigTypeId = element.getAttribute("launchConfigurationType"); //$NON-NLS-1$
	}

	public String getDescriptorTypeId() {
		return descriptorTypeId;
	}

	public String getTargetTypeId() {
		return targetTypeId;
	}

	public String getLaunchConfigTypeId() {
		return launchConfigTypeId;
	}
}