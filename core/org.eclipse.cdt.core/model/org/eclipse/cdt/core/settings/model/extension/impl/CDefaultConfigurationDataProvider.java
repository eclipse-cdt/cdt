/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

public class CDefaultConfigurationDataProvider extends
		CConfigurationDataProvider {
	private static final String DEFAULT_STORAGE_ID = "defaultConfigurationDataProvider";  //$NON-NLS-1$
	
	@Override
	public CConfigurationData applyConfiguration(
			ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData base,
			IProgressMonitor monitor)
			throws CoreException {
		ICStorageElement el = getStorageElement(des, true);
		CDataSerializer serializer = getDataSerializer();
		serializer.store(base, el);
		return base;
	}

	@Override
	public CConfigurationData createConfiguration(
			ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData base,
			boolean clone,
			IProgressMonitor monitor) throws CoreException {
		CDataFactory factory = getDataFactory();
		return factory.createConfigurationdata(des.getId(), des.getName(), base, clone);
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription des,
			IProgressMonitor monitor)
			throws CoreException {
		ICStorageElement el = getStorageElement(des, false);
		if(el != null){
			CDataSerializer serializer = getDataSerializer();
			CDataFactory factory = getDataFactory();
			try {
				return serializer.loadConfigurationData(factory, el);
			} catch (CoreException e){
				if(des.isPreferenceConfiguration())
					return createPreferenceConfig(factory);
				throw e;
			}
		} else if (des.isPreferenceConfiguration()){
			return createPreferenceConfig(getDataFactory());
		}
		return null;
	}

	@Override
	public void removeConfiguration(ICConfigurationDescription des,
			CConfigurationData data,
			IProgressMonitor monitor) {
		//do nothing
	}
	
	protected CDataFactory getDataFactory(){
		return CDataFactory.getDefault();
	}
	
	protected CDataSerializer getDataSerializer(){
		return CDataSerializer.getDefault();
	}
	
	protected String getStorageId(){
		return DEFAULT_STORAGE_ID;
	}
	
	protected ICStorageElement getStorageElement(ICConfigurationDescription des, boolean create) throws CoreException{
		return des.getStorage(getStorageId(), create);
	}
	
	protected CConfigurationData createPreferenceConfig(CDataFactory factory){
		return CDataUtil.createEmptyData(null, "preference", factory, true); //$NON-NLS-1$
	}
}
