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
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.IAdapterFactory;

public class BuildConfigAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IBuildConfiguration
				&& adapterType.equals(ICConfigurationDescription.class)) {
			IBuildConfiguration buildConfig = (IBuildConfiguration) adaptableObject;
			ICProjectDescription prjDesc = CCorePlugin.getDefault()
					.getProjectDescription(buildConfig.getProject());
			if (prjDesc != null) {
				return (T) prjDesc.getConfigurationById(buildConfig.getName());
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ICConfigurationDescription.class };
	}

}
