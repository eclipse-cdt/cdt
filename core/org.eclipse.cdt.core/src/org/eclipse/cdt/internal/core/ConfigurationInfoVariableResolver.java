/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.osgi.util.NLS;

public abstract class ConfigurationInfoVariableResolver implements IDynamicVariableResolver {
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			String message = NLS.bind(CCorePlugin.getResourceString("ConfigurationInfoVariableResolver.noProjectName"), //$NON-NLS-1$
					variable.getName());
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, message, null));
		}
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
		if (!project.exists()) {
			String message = NLS.bind(CCorePlugin.getResourceString("ConfigurationInfoVariableResolver.wrongProjectName"), //$NON-NLS-1$
					argument, variable.getName());
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, message, null));
		}
    	ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);
    	return fetchConfigurationInfo(projectDescription.getActiveConfiguration());
	}
	
	protected abstract String fetchConfigurationInfo(ICConfigurationDescription configuration);
}
