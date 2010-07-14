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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class BuildConfigurationData extends CConfigurationData {
	private Configuration fCfg;
//	private BuildVariablesContributor fCdtVars;
	public BuildConfigurationData(IConfiguration cfg){
		fCfg = (Configuration)cfg;
	}
	
	public IConfiguration getConfiguration(){
		return fCfg;
	}

	@Override
	public CFileData createFileData(IPath path, CFileData base)
			throws CoreException {
		String id = ManagedBuildManager.calculateChildId(fCfg.getId(),null);
		IFileInfo info = fCfg.createFileInfo(path, ((BuildFileData)base).getFileInfo(), id, path.lastSegment());
		return info.getFileData();
	}
	
	@Override
	public CFileData createFileData(IPath path, CFolderData base, CLanguageData baseLangData)
		throws CoreException {
		String id = ManagedBuildManager.calculateChildId(fCfg.getId(),null);
		ITool baseTool;
		if(baseLangData instanceof BuildLanguageData){
			baseTool = ((BuildLanguageData)baseLangData).getTool();
		} else {
			baseTool = null;
		}
		IFileInfo info = fCfg.createFileInfo(path, ((BuildFolderData)base).getFolderInfo(), baseTool, id, path.lastSegment());
		return info.getFileData();
	}


	
	@Override
	public CFolderData createFolderData(IPath path, CFolderData base)
			throws CoreException {
		String id = ManagedBuildManager.calculateChildId(fCfg.getId(),null);
		IFolderInfo folderInfo = fCfg.createFolderInfo(path, ((BuildFolderData)base).getFolderInfo(), id, base.getName());
		return folderInfo.getFolderData();
	}

	@Override
	public String getDescription() {
		return fCfg.getDescription();
	}

	@Override
	public CResourceData[] getResourceDatas() {
		IResourceInfo infos[] = fCfg.getResourceInfos();
		CResourceData datas[] = new CResourceData[infos.length];
		for(int i = 0; i < infos.length; i++){
			datas[i] = infos[i].getResourceData();
		}
		return datas;
	}

	@Override
	public CFolderData getRootFolderData() {
		return fCfg.getRootFolderInfo().getFolderData();
	}

	@Override
	public void removeResourceData(CResourceData data) throws CoreException {
		fCfg.removeResourceInfo(data.getPath());
	}

	@Override
	public void setDescription(String description) {
		fCfg.setDescription(description);
	}

	@Override
	public String getId() {
		return fCfg.getId();
	}

	@Override
	public String getName() {
		return fCfg.getName();
	}

	@Override
	public void setName(String name) {
		fCfg.setName(name);
	}

	@Override
	public boolean isValid() {
		return fCfg != null;
	}

	@Override
	public CTargetPlatformData getTargetPlatformData() {
		return fCfg.getToolChain().getTargetPlatformData();
	}

	@Override
	public ICSourceEntry[] getSourceEntries() {
		return fCfg.getSourceEntries();
	}

	@Override
	public void setSourceEntries(ICSourceEntry[] entries) {
		fCfg.setSourceEntries(entries);
	}

	@Override
	public CBuildData getBuildData() {
		return fCfg.getBuildData();
	}

	@Override
	public ICdtVariablesContributor getBuildVariablesContributor() {
//		if(fCdtVars == null)
//			fCdtVars = new BuildVariablesContributor(this);
//		return fCdtVars;
		return new BuildVariablesContributor(this);
	}
	
	void clearCachedData(){
		fCfg.clearCachedData();
		CResourceData[] datas = getResourceDatas();
		CResourceData data;
//		BuildLanguageData lData;
//		BuildLanguageData[] lDatas;

		
		for(int i = 0; i < datas.length; i++){
			data = datas[i];
			if(data.getType() == ICSettingBase.SETTING_FOLDER){
				((BuildFolderData)data).clearCachedData();
			} else {
				((BuildFileData)data).clearCachedData();
			}
		}
	}

	@Override
	public CConfigurationStatus getStatus() {
		int flags = 0;
		String msg = null;
		if(!fCfg.isSupported()){
			flags |= CConfigurationStatus.TOOLCHAIN_NOT_SUPPORTED;
			msg = DataProviderMessages.getString("BuildConfigurationData.0"); //$NON-NLS-1$
		}
		
		if(flags != 0)
			return new CConfigurationStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), flags, msg, null);
		
		return CConfigurationStatus.CFG_STATUS_OK;
	}
}
