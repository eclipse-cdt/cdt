/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.docker.launcher.ContainerCommandLauncher;
import org.eclipse.cdt.internal.docker.launcher.Messages;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;

public class ContainerCommandLauncherFactory
		implements ICommandLauncherFactory {

	@Override
	public ICommandLauncher getCommandLauncher(IProject project) {
		// check if container build enablement has been checked
		ICConfigurationDescription cfgd = CoreModel.getDefault()
				.getProjectDescription(project).getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager
				.getConfigurationForDescription(cfgd);
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		if (props != null) {
			String enablementProperty = props.getProperty(
					ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
			if (enablementProperty != null) {
				boolean enableContainer = Boolean
						.parseBoolean(enablementProperty);
				// enablement has occurred, we can return a
				// ContainerCommandLauncher
				if (enableContainer) {
					return new ContainerCommandLauncher();
				}
			}
		}
		return null;
	}

	@Override
	public void registerLanguageSettingEntries(IProject project,
			List<? extends ICLanguageSettingEntry> entries) {
		ICConfigurationDescription cfgd = CoreModel.getDefault()
				.getProjectDescription(project).getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager
				.getConfigurationForDescription(cfgd);
		IOptionalBuildProperties props = cfg.getOptionalBuildProperties();
		if (props != null) {
			String enablementProperty = props.getProperty(
					ContainerCommandLauncher.CONTAINER_BUILD_ENABLED);
			if (enablementProperty != null) {
				boolean enableContainer = Boolean
						.parseBoolean(enablementProperty);
				if (enableContainer) {
					String connectionName = props.getProperty(
							ContainerCommandLauncher.CONNECTION_ID);
					String imageName = props
							.getProperty(ContainerCommandLauncher.IMAGE_ID);
					if (connectionName == null || connectionName.isEmpty()
							|| imageName == null || imageName.isEmpty()) {
						DockerLaunchUIPlugin.logErrorMessage(
								Messages.ContainerCommandLauncher_invalid_values);
						return;
					}
					String prefix = getCleanName(connectionName)
							+ IPath.SEPARATOR
							+ getCleanName(imageName); //$NON-NLS-1$ //$NON-NLS-2$
					ContainerLauncher launcher = new ContainerLauncher();
					List<String> paths = new ArrayList<>();
					for (ICLanguageSettingEntry entry : entries) {
						if (entry instanceof ICIncludePathEntry) {
							String path = entry.getValue();
							paths.add(path);
						}
					}
					if (paths.size() > 0) {
						IPath pluginPath = Platform.getStateLocation(Platform
								.getBundle(DockerLaunchUIPlugin.PLUGIN_ID));
						IPath hostDir = pluginPath.append(prefix);
						int status = launcher.fetchContainerDirs(connectionName,
								imageName,
								paths, hostDir);
					}
				}
			}
		}

	}

	private String getCleanName(String name) {
		return name.replaceAll("[:/]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
