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
package org.eclipse.cdt.make.internal.core.dataprovider;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.util.CDataSerializer;
import org.eclipse.cdt.core.settings.model.util.UserAndDiscoveredEntryDataSerializer;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator.DiscoveredSettingInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoProcessor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class MakeConfigurationDataProvider extends CDefaultConfigurationDataProvider {
	private static final String STORAGE_ID = "makeConfigDataProvider"; //$NON-NLS-1$

	@Override
	protected CDataFactory getDataFactory() {
		return MakeCDataFactory.getDefault();
	}

	@Override
	protected CDataSerializer getDataSerializer() {
		return UserAndDiscoveredEntryDataSerializer.getDefault();
	}

	@Override
	protected String getStorageId() {
		return STORAGE_ID;
	}

	@Override
	public CConfigurationData applyConfiguration(ICConfigurationDescription cfgDescription,
			ICConfigurationDescription baseCfgDescription, CConfigurationData baseData, IProgressMonitor monitor)
			throws CoreException {

		CConfigurationData result = super.applyConfiguration(cfgDescription, baseCfgDescription, baseData, monitor);
		if (!cfgDescription.isPreferenceConfiguration())
			updateDiscoveredInfo(cfgDescription.getProjectDescription().getProject(), result);
		return result;
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription cfgDescription, IProgressMonitor monitor)
			throws CoreException {
		CConfigurationData result = super.loadConfiguration(cfgDescription, monitor);
		if (!cfgDescription.isPreferenceConfiguration())
			updateDiscoveredInfo(cfgDescription.getProjectDescription().getProject(), result);
		return result;
	}

	protected void updateDiscoveredInfo(IProject project, CConfigurationData cfgData) {
		updateDiscoveredInfo(project, cfgData, getInfoCalculator(), getInfoProcessor());
	}

	public static void updateDiscoveredInfo(IProject project, CConfigurationData cfgData,
			CDataDiscoveredInfoCalculator calculator, CDataDiscoveredInfoProcessor processor) {

		DiscoveredSettingInfo dsInfo = calculator.getSettingInfos(project, cfgData);
		processor.applyDiscoveredInfo(cfgData, dsInfo);
	}

	protected CDataDiscoveredInfoProcessor getInfoProcessor() {
		return MakeDiscoveredInfoProcessor.getDefault();
	}

	protected CDataDiscoveredInfoCalculator getInfoCalculator() {
		return CDataDiscoveredInfoCalculator.getDefault();
	}

}
