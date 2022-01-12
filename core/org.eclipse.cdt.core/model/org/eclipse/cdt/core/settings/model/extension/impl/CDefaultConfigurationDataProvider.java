/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension.impl;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.util.CDataSerializer;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CDefaultConfigurationDataProvider extends CConfigurationDataProvider {
	private static final String DEFAULT_STORAGE_ID = "defaultConfigurationDataProvider"; //$NON-NLS-1$

	@Override
	public CConfigurationData applyConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, IProgressMonitor monitor)
			throws CoreException {

		ICStorageElement el = getStorageElement(cfgDescription, true);
		CDataSerializer serializer = getDataSerializer();
		serializer.store(baseData, el);
		return baseData;
	}

	@Override
	public CConfigurationData createConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, boolean clone,
			IProgressMonitor monitor) throws CoreException {

		CDataFactory factory = getDataFactory();
		return factory.createConfigurationdata(cfgDescription.getId(), cfgDescription.getName(), baseData, clone);
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription cfgDescription, IProgressMonitor monitor)
			throws CoreException {
		ICStorageElement el = getStorageElement(cfgDescription, false);
		if (el != null) {
			CDataSerializer serializer = getDataSerializer();
			CDataFactory factory = getDataFactory();
			try {
				return serializer.loadConfigurationData(factory, el);
			} catch (CoreException e) {
				if (cfgDescription.isPreferenceConfiguration())
					return createPreferenceConfig(factory);
				throw e;
			}
		} else if (cfgDescription.isPreferenceConfiguration()) {
			return createPreferenceConfig(getDataFactory());
		}
		return null;
	}

	@Override
	public void removeConfiguration(ICConfigurationDescription cfgDescription, CConfigurationData data,
			IProgressMonitor monitor) {
		//do nothing
	}

	protected CDataFactory getDataFactory() {
		return CDataFactory.getDefault();
	}

	protected CDataSerializer getDataSerializer() {
		return CDataSerializer.getDefault();
	}

	protected String getStorageId() {
		return DEFAULT_STORAGE_ID;
	}

	protected ICStorageElement getStorageElement(ICConfigurationDescription cfgDescription, boolean create)
			throws CoreException {
		return cfgDescription.getStorage(getStorageId(), create);
	}

	protected CConfigurationData createPreferenceConfig(CDataFactory factory) {
		return CDataUtil.createEmptyData(null, "preference", factory, true); //$NON-NLS-1$
	}
}
