package org.eclipse.cdt.debug.core.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.launchbar.core.AbstractLaunchConfigProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;

/**
 * Core launch configuration provider used by generic target types.
 *
 * @since 8.3
 */
public class CoreBuildGenericLaunchConfigProvider extends AbstractLaunchConfigProvider {

	private static final String ATTR_OS = CDebugCorePlugin.PLUGIN_ID + ".target_os"; //$NON-NLS-1$
	private static final String ATTR_ARCH = CDebugCorePlugin.PLUGIN_ID + ".target_arch"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	private Map<IProject, Map<String, ILaunchConfiguration>> configs = new HashMap<>();

	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		return target.getTypeId().equals(GenericTargetTypeProvider.TYPE_ID);
	}

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(CoreBuildGenericLaunchConfigDelegate.TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfiguration config = null;
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}

			String os = target.getAttribute(ILaunchTarget.ATTR_OS, EMPTY);
			String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, EMPTY);
			String targetConfig = os + '.' + arch;
			config = projectConfigs.get(targetConfig);
			if (config == null) {
				config = createLaunchConfiguration(descriptor, target);
			}
		}
		return config;
	}

	@Override
	protected void populateLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target,
			ILaunchConfigurationWorkingCopy workingCopy) throws CoreException {
		super.populateLaunchConfiguration(descriptor, target, workingCopy);

		// Set the project
		IProject project = descriptor.getAdapter(IProject.class);
		workingCopy.setMappedResources(new IResource[] { project });

		// set the OS and ARCH
		String os = target.getAttribute(ILaunchTarget.ATTR_OS, EMPTY);
		workingCopy.setAttribute(ATTR_OS, os);

		String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, EMPTY);
		workingCopy.setAttribute(ATTR_ARCH, arch);
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		// TODO make sure it's the correct type
		if (ownsLaunchConfiguration(configuration)) {
			IProject project = configuration.getMappedResources()[0].getProject();
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs == null) {
				projectConfigs = new HashMap<>();
				configs.put(project, projectConfigs);
			}

			String os = configuration.getAttribute(ATTR_OS, EMPTY);
			String arch = configuration.getAttribute(ATTR_ARCH, EMPTY);
			String targetConfig = os + '.' + arch;
			projectConfigs.put(targetConfig, configuration);
			return true;
		}
		return false;
	}

	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		for (Entry<IProject, Map<String, ILaunchConfiguration>> projectEntry : configs.entrySet()) {
			Map<String, ILaunchConfiguration> projectConfigs = projectEntry.getValue();
			for (Entry<String, ILaunchConfiguration> entry : projectConfigs.entrySet()) {
				if (configuration.equals(entry.getValue())) {
					projectConfigs.remove(entry.getKey());
					if (projectConfigs.isEmpty()) {
						configs.remove(projectEntry.getKey());
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		// nothing to do
		return false;
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		IProject project = descriptor.getAdapter(IProject.class);
		if (project != null) {
			Map<String, ILaunchConfiguration> projectConfigs = configs.get(project);
			if (projectConfigs != null) {
				for (ILaunchConfiguration config : projectConfigs.values()) {
					config.delete();
				}
			}
		}
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		// Any other targets have the same OS and ARCH?
		String os = target.getAttribute(ILaunchTarget.ATTR_OS, EMPTY);
		String arch = target.getAttribute(ILaunchTarget.ATTR_ARCH, EMPTY);

		ILaunchTargetManager targetManager = CDebugCorePlugin.getService(ILaunchTargetManager.class);
		for (ILaunchTarget t : targetManager.getLaunchTargets()) {
			if (!target.equals(t) && os.equals(t.getAttribute(ILaunchTarget.ATTR_OS, EMPTY))
					&& arch.equals(t.getAttribute(ILaunchTarget.ATTR_ARCH, EMPTY))) {
				// Yup, nothing to do then
				return;
			}
		}

		for (Entry<IProject, Map<String, ILaunchConfiguration>> projectEntry : configs.entrySet()) {
			Map<String, ILaunchConfiguration> projectConfigs = projectEntry.getValue();
			ILaunchConfiguration config = projectConfigs.get(os);
			if (config != null) {
				config.delete();
			}
		}
	}

}
