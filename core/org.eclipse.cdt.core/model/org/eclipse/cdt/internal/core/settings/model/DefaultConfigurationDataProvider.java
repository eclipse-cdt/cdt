/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.core.runtime.CoreException;

public class DefaultConfigurationDataProvider extends
		CConfigurationDataProvider {

	public CConfigurationData applyConfiguration(
			ICConfigurationDescription des, CConfigurationData base)
			throws CoreException {
		//TODO: implement load/store
		return base;
	}

	public CConfigurationData createConfiguration(
			ICConfigurationDescription des, CConfigurationData base,
			boolean clone) throws CoreException {
		//TODO: implement load/store
		CDefaultConfigurationData data = new CDefaultConfigurationData(des.getId(), des.getName(), base, null, clone);
		return data;
	}

	public CConfigurationData loadConfiguration(ICConfigurationDescription des)
			throws CoreException {
		//TODO: implement load/store
		CDefaultConfigurationData data = new CDefaultConfigurationData(des.getId(), des.getName(), null);
		data.initEmptyData();
		return data;
	}

	public void removeConfiguration(ICConfigurationDescription des,
			CConfigurationData data) {
		//TODO: implement load/store

	}

}
