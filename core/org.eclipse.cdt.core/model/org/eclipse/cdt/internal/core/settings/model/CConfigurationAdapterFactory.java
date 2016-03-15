/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CConfigurationAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ICConfigurationDescription
				&& adapterType.equals(IBuildConfiguration.class)) {
			ICConfigurationDescription cfgDesc = (ICConfigurationDescription) adaptableObject;
			IProject project = cfgDesc.getProjectDescription().getProject();
			if (project != null) {
				String configId = cfgDesc.getId();
				try {
					IBuildConfiguration config = project.getBuildConfig(configId);
					if (config == null) {
						CCorePlugin.log(String.format("null config %s: %s", project.getName(), configId)); //$NON-NLS-1$
					}
					return (T) config;
				} catch (CoreException e) {
					if (!ResourcesPlugin.getWorkspace().isTreeLocked() &&
							e.getStatus().getCode() == IResourceStatus.BUILD_CONFIGURATION_NOT_FOUND) {
						try {
							Set<String> configNames = new HashSet<>();
							for (IBuildConfiguration config : project.getBuildConfigs()) {
								configNames.add(config.getName());
							}
							configNames.add(configId);

							IProjectDescription projectDesc = project.getDescription();
							projectDesc.setBuildConfigs(
									configNames.toArray(new String[configNames.size()]));
							project.setDescription(projectDesc, new NullProgressMonitor());
							return (T) project.getBuildConfig(configId);
						} catch (CoreException e2) {
							CCorePlugin.log(e2.getStatus());
						}
					} else {
						CCorePlugin.log(e.getStatus());
					}
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IBuildConfiguration.class };
	}

}
