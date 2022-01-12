/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * Represents a manager of registered qmakeEnvProvider extensions.
 */
public final class QMakeEnvProviderManager {

	private static QMakeEnvProviderManager INSTANCE = new QMakeEnvProviderManager();

	private final List<QMakeEnvProviderDescriptor> descriptors;

	public static QMakeEnvProviderManager getInstance() {
		return INSTANCE;
	}

	private QMakeEnvProviderManager() {
		descriptors = loadDescriptors();
	}

	/**
	 * Returns a list of all registerd qmakeEnvProvider extensions.
	 *
	 * @return the list of extensions
	 */
	private static List<QMakeEnvProviderDescriptor> loadDescriptors() {
		List<QMakeEnvProviderDescriptor> descriptors = new ArrayList<>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.ID,
				Activator.QMAKE_ENV_PROVIDER_EXT_POINT_NAME);
		for (IConfigurationElement element : elements) {
			descriptors.add(new QMakeEnvProviderDescriptor(element));
		}
		Collections.sort(descriptors);
		return descriptors;
	}

	/**
	 * Called by QMakeProjectInfo to create IQMakeEnv for a specified IController.
	 * It asks each qmakeEnvProvider extensions to provide the instance. If none of them provides, then the default IQMakeEnvProvider is returned.
	 *
	 * @param controller the controller
	 * @return the IQMakeEnv instance for specifying the QMake environment
	 */
	public IQMakeEnv createEnv(IQMakeEnvProvider.IController controller) {
		for (QMakeEnvProviderDescriptor descriptor : descriptors) {
			IQMakeEnv env = descriptor.createEnv(controller);
			if (env != null) {
				return env;
			}
		}
		return new ConfigurationQMakeEnv(controller.getConfiguration());
	}

	/**
	 * Represents a fallback IQMakeEnvProvider that is used for a project that has QtNature
	 * while there is no registered IQMakeEnvProvider that would provide any IQMakeEnv.
	 */
	private static class ConfigurationQMakeEnv implements IQMakeEnv2 {

		private static final String PRO_FILE_SUFFIX = ".pro"; //$NON-NLS-1$
		private static final String ENV_VAR_QMAKE = "QMAKE"; //$NON-NLS-1$

		private final ICConfigurationDescription configuration;

		public ConfigurationQMakeEnv(ICConfigurationDescription configuration) {
			this.configuration = configuration;
		}

		@Override
		public void init() {
		}

		@Override
		public void destroy() {
		}

		/**
		 * Returns QMakeEnvInfo resolved from a project in a generic way.
		 * If exists, the root-level .pro file is resolved as the one that is located directly under the project and has the same name.
		 * If exists, qmake executable is resolved from "QMAKE" environment variable that is stored in the project configuration.
		 *
		 * @return the QMakeEnvInfo instance if the project configuration exists; otherwise null.
		 */
		@Override
		public QMakeEnvInfo getQMakeEnvInfo() {
			if (configuration == null) {
				return null;
			}
			IProject project = configuration.getProjectDescription().getProject();
			IFile proFile = project != null ? project.getFile(project.getName() + PRO_FILE_SUFFIX) : null;

			IEnvironmentVariable variable = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(ENV_VAR_QMAKE, configuration, true);
			String qmakeFilePath = variable != null ? variable.getValue() : null;
			return new QMakeEnvInfo(proFile, qmakeFilePath, Collections.<String, String>emptyMap(),
					Collections.<IFile>emptyList());
		}

	}

}
