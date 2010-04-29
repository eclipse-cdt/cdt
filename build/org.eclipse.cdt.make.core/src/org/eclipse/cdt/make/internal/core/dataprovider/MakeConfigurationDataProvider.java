/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoProcessor;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator.DiscoveredSettingInfo;
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
	public CConfigurationData applyConfiguration(
			ICConfigurationDescription des,
			ICConfigurationDescription baseDescription,
			CConfigurationData base, IProgressMonitor monitor)
			throws CoreException {
		CConfigurationData result = super.applyConfiguration(des, baseDescription, base, monitor);
		if(!des.isPreferenceConfiguration())
			updateDiscoveredInfo(des.getProjectDescription().getProject(), result);
		return result;
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription des,
			IProgressMonitor monitor) throws CoreException {
		CConfigurationData result = super.loadConfiguration(des, monitor);
		if(!des.isPreferenceConfiguration())
			updateDiscoveredInfo(des.getProjectDescription().getProject(), result);
		return result;
	}

	protected void updateDiscoveredInfo(IProject project, CConfigurationData cfgData){
		updateDiscoveredInfo(project, cfgData, getInfoCalculator(), getInfoProcessor());
	}
	
	public static void updateDiscoveredInfo(IProject project, CConfigurationData cfgData,
			CDataDiscoveredInfoCalculator calculator,
			CDataDiscoveredInfoProcessor processor){
		
		DiscoveredSettingInfo dsInfo = calculator.getSettingInfos(project, cfgData);
		
		processor.applyDiscoveredInfo(cfgData, dsInfo);
	}
	
	protected CDataDiscoveredInfoProcessor getInfoProcessor(){
		return MakeDiscoveredInfoProcessor.getDefault();
	}
	
	protected CDataDiscoveredInfoCalculator getInfoCalculator(){
		return CDataDiscoveredInfoCalculator.getDefault();
	}

}
