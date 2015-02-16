package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.IConfigurationElement;

public class LaunchConfigTypeInfo {
	private final String descriptorTypeId;
	private final String targetTypeId;
	private final String launchConfigTypeId;

	public LaunchConfigTypeInfo(IConfigurationElement element) {
		this.descriptorTypeId = element.getAttribute("descriptorType");
		this.targetTypeId = element.getAttribute("targetType");
		this.launchConfigTypeId = element.getAttribute("launchConfigurationType");
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