/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

public class CConfigurationAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof ICConfigurationDescription
				&& adapterType.equals(IBuildConfiguration.class)) {
			ICConfigurationDescription cfgDesc = (ICConfigurationDescription) adaptableObject;
			IProject project = cfgDesc.getProjectDescription().getProject();
			if (project != null) {
				try {
					String configId = cfgDesc.getId();
					return (T) project.getBuildConfig(configId);
				} catch (CoreException e) {
					CCorePlugin.log(e.getStatus());
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
