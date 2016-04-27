package org.eclipse.cdt.arduino.core.internal.build;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ArduinoToolChain extends GCCToolChain {

	private final ArduinoBuildConfiguration buildConfig;

	public ArduinoToolChain(IToolChainProvider provider, ArduinoBuildConfiguration config) {
		super(provider, config.getProject().getName() + '#' + config.getName());
		this.buildConfig = config;
	}

	ArduinoToolChain(IToolChainProvider provider, String name) throws CoreException {
		super(provider, name);
		String[] segments = name.split("#"); //$NON-NLS-1$
		if (segments.length == 2) {
			String projectName = segments[0];
			String configName = segments[1];

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project != null) {
				ICBuildConfigurationManager configManager = Activator.getService(ICBuildConfigurationManager.class);
				IBuildConfiguration config = configManager.getBuildConfiguration(
						configManager.getProvider(ArduinoBuildConfigurationProvider.ID), project, configName);
				ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
				buildConfig = cconfig.getAdapter(ArduinoBuildConfiguration.class);
			} else {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "No project"));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Bad Name"));
		}
	}

	@Override
	public String getProperty(String key) {
		// TODO architecture if I need it
		if (key.equals(IToolChain.ATTR_OS)) {
			return "arduino"; //$NON-NLS-1$
		} else {
			return null;
		}
	}

	// TODO do I really need this?
	public ArduinoBuildConfiguration getBuildConfig() {
		return buildConfig;
	}

}
