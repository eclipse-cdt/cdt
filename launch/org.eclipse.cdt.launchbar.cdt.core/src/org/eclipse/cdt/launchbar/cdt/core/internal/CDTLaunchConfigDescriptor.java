package org.eclipse.cdt.launchbar.cdt.core.internal;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class CDTLaunchConfigDescriptor implements ILaunchConfigurationDescriptor {

	private String projectName;
	private ILaunchConfiguration config;

	public CDTLaunchConfigDescriptor(IProject project) {
		projectName = project.getName();
	}

	public CDTLaunchConfigDescriptor(ILaunchConfiguration config) {
		this.config = config;
	}

	@Override
	public String getName() {
		if (config != null)
			return config.getName();
		else
			return projectName;
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType() throws CoreException {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration() throws CoreException {
		if (config == null) {
			ILaunchConfigurationType configType = getLaunchConfigurationType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(projectName));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			wc.setMappedResources(new IResource[] { getProject() });

			// TODO finish this off

			config = wc.doSave();
		}
		return config;
	}

	@Override
	public boolean matches(ILaunchConfiguration launchConfiguration) {
		// TODO Auto-generated method stub

		// matches if it's the same project
		return false;
	}

}
