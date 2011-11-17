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
package org.eclipse.cdt.core.settings.model.extension.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerVisitor;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.runtime.IPath;

public class CResourceDataContainer {
	private PathSettingsContainer fRcDataContainer;
	private boolean fIncludeCurrent;

	public CResourceDataContainer(PathSettingsContainer pathSettings, boolean includeCurrent){
		fRcDataContainer = pathSettings;
		fIncludeCurrent = includeCurrent;
	}

	public void changeCurrentPath(IPath path, boolean moveChildren){
		fRcDataContainer.setPath(path, moveChildren);
	}

	public IPath getCurrentPath(){
		return fRcDataContainer.getPath();
	}

	public CResourceData getCurrentResourceData(){
		return (CResourceData)fRcDataContainer.getValue();
	}

	public CResourceData getResourceData(IPath path, boolean exactPath) {
		PathSettingsContainer cr = fRcDataContainer.getChildContainer(path, false, exactPath);
		if(cr != null)
			return (CResourceData)cr.getValue();
		return null;
	}

	public CResourceData[] getResourceDatas() {
		return getResourceDatas(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER);
	}

	public CResourceData[] getResourceDatas(final int kind) {
		return getResourceDatas(kind, CResourceData.class);
	}

	public CResourceData[] getResourceDatas(int kind, Class<CResourceData> clazz){
		List<CResourceData> list = getRcDataList(kind);

		CResourceData datas[] = (CResourceData[])Array.newInstance(clazz, list.size());

		return list.toArray(datas);
	}

	public List<CResourceData> getRcDataList(final int kind){
		final List<CResourceData> list = new ArrayList<CResourceData>();
		fRcDataContainer.accept(new IPathSettingsContainerVisitor(){

			@Override
			public boolean visit(PathSettingsContainer container) {
				if(fIncludeCurrent || container != fRcDataContainer){
					CResourceData data = (CResourceData)container.getValue();
					if((data.getType() & kind) == data.getType())
						list.add(data);
				}
				return true;
			}
		});

		return list;
	}

	public CResourceData getResourceData(IPath path, boolean exactPath, int kind){
		CResourceData data = getResourceData(path, exactPath);
		if(data != null && (data.getType() & kind) == data.getType())
			return data;
		return null;
	}

	public void removeResourceData(IPath path) {
		fRcDataContainer.removeChildContainer(path);
	}

	public void addResourceData(CResourceData data){
		PathSettingsContainer cr = fRcDataContainer.getChildContainer(data.getPath(), true, true);
		cr.setValue(data);
	}

	public CFileData getFileData(IPath path){
		return (CFileData)getResourceData(path, true, ICSettingBase.SETTING_FILE);
	}

	public CFolderData getFolderData(IPath path){
		return (CFolderData)getResourceData(path, true, ICSettingBase.SETTING_FOLDER);
	}
}
