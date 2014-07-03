package org.eclipse.cdt.launchbar.cdt.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;

public class CDTLaunchConfigDescriptor implements ILaunchConfigurationDescriptor {

	private final ILaunchBarManager manager;
	private String projectName;
	private ILaunchConfiguration config;
	private ILaunchMode[] launchModes;

	public CDTLaunchConfigDescriptor(ILaunchBarManager manager, IProject project) {
		this.manager = manager;
		this.projectName = project.getName();
	}

	public CDTLaunchConfigDescriptor(ILaunchBarManager manager, ILaunchConfiguration config) {
		this.manager = manager;
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
	public ILaunchConfiguration getLaunchConfiguration() throws CoreException {
		if (config == null) {
			ILaunchConfigurationType configType = config.getType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(projectName));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			wc.setMappedResources(new IResource[] { getProject() });
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			// TODO finish this off

			config = wc.doSave();
		}
		return config;
	}

	@Override
	public boolean matches(ILaunchConfiguration launchConfiguration) throws CoreException {
		if (config == launchConfiguration)
			return true;
		
		String pname = launchConfiguration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		return pname.equals(projectName);
	}

	@Override
	public ILaunchTarget[] getLaunchTargets() {
		return new ILaunchTarget[] { manager.getLocalLaunchTarget() };
	}

	@Override
	public ILaunchTarget getLaunchTarget(String id) {
		ILaunchTarget localTarget = manager.getLocalLaunchTarget();
		if (localTarget.getId().equals(id))
			return localTarget;
		return null;
	}

	@Override
	public void setActiveLaunchTarget(ILaunchTarget target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ILaunchMode[] getLaunchModes() throws CoreException {
		if (launchModes == null) {
			List<ILaunchMode> mymodes = new ArrayList<>();
			ILaunchConfigurationType type = config.getType();
			ILaunchMode[] modes = DebugPlugin.getDefault().getLaunchManager().getLaunchModes();
			for (ILaunchMode mode : modes) {
				if (type.supportsMode(mode.getIdentifier())) {
					mymodes.add(mode);
				}
			}
			launchModes = mymodes.toArray(new ILaunchMode[mymodes.size()]);
		}
		return launchModes;
	}

	@Override
	public ILaunchMode getLaunchMode(String id) throws CoreException {
		for (ILaunchMode mode : getLaunchModes())
			if (mode.getIdentifier().equals(id))
				return mode;
		return null;
	}

	@Override
	public void setActiveLaunchMode(ILaunchMode mode) {
		// TODO Auto-generated method stub
		
	}
	
}
