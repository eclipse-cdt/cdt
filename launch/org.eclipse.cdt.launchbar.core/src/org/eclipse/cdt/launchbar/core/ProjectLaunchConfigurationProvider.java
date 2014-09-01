package org.eclipse.cdt.launchbar.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public abstract class ProjectLaunchConfigurationProvider extends LaunchConfigurationProvider {

	private Map<IProject, ILaunchConfiguration> projectMap = new HashMap<>();

	@Override
	protected void populateConfiguration(ILaunchConfigurationWorkingCopy workingCopy, ILaunchDescriptor descriptor) throws CoreException {
		super.populateConfiguration(workingCopy, descriptor);
		
		// Add our project to the mapped resources
		IProject project = (IProject) descriptor.getAdapter(IProject.class);
		IResource[] mappedResources = workingCopy.getMappedResources();
		if (mappedResources == null || mappedResources.length == 0) {
			workingCopy.setMappedResources(new IResource[] { project });
		} else {
			IResource[] newResources = new IResource[mappedResources.length + 1];
			System.arraycopy(mappedResources, 0, newResources, 0, mappedResources.length);
			newResources[mappedResources.length] = project;
			workingCopy.setMappedResources(newResources);
		}
	}
	
	protected IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		// by default return the first project in the mapped resources
		for (IResource resource : configuration.getMappedResources()) {
			if (resource instanceof IProject) {
				return (IProject) resource;
			}
		}

		return null;
	}

	@Override
	public Object launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		IProject project = getProject(configuration);
		if (project == null) {
			return null;
		}

		if (!ownsConfiguration(configuration)) {
			return null;
		}

		projectMap.put(project, configuration);
		return project;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		IProject project = getProject(configuration);
		if (project == null) {
			return false;
		}

		if (configuration.equals(projectMap.get(project))) {
			projectMap.remove(project);
			return true;
		}

		return false;
	}

}
