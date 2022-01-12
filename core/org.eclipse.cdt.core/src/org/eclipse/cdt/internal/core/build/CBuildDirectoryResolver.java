/*******************************************************************************
 * Copyright (c) 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.build;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration2;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

public class CBuildDirectoryResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null || argument.isEmpty()) {
			return null;
		}

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
		if (project == null || !project.exists()) {
			return null;
		}

		IBuildConfiguration config = project.getActiveBuildConfig();

		ICBuildConfigurationManager manager = CCorePlugin.getService(ICBuildConfigurationManager.class);
		ICBuildConfiguration coreConfig = manager.getBuildConfiguration(config);
		if (coreConfig == null) {
			return null;
		}

		if (coreConfig instanceof ICBuildConfiguration2) {
			URI uri = ((ICBuildConfiguration2) coreConfig).getBuildDirectoryURI();
			if (uri != null) {
				return new File(uri).getAbsolutePath();
			}
		}

		return null;
	}

}
