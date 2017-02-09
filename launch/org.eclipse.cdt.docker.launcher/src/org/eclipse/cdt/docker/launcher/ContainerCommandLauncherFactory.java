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

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.ICommandLauncherFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.docker.launcher.ContainerCommandLauncher;
import org.eclipse.cdt.managedbuilder.buildproperties.IOptionalBuildProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;

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

}
