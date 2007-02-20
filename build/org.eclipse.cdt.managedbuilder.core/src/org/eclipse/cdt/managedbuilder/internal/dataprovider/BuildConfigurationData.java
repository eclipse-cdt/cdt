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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class BuildConfigurationData extends CConfigurationData {
	private IConfiguration fCfg;
	private BuildVariablesContributor fCdtVars;
	public BuildConfigurationData(IConfiguration cfg){
		fCfg = cfg;
	}
	
	public IConfiguration getConfiguration(){
		return fCfg;
	}

	public CFileData createFileData(IPath path, CFileData base)
			throws CoreException {
		String id = ManagedBuildManager.calculateChildId(fCfg.getId(),null);
		IFileInfo info = fCfg.createFileInfo(path, ((BuildFileData)base).getFileInfo(), id, path.lastSegment());
		return info.getFileData();
	}
	
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


	
	public CFolderData createFolderData(IPath path, CFolderData base)
			throws CoreException {
		String id = ManagedBuildManager.calculateChildId(fCfg.getId(),null);
		IFolderInfo folderInfo = fCfg.createFolderInfo(path, ((BuildFolderData)base).getFolderInfo(), id, base.getName());
		return folderInfo.getFolderData();
	}

	public String getDescription() {
		return fCfg.getDescription();
	}

	public CResourceData[] getResourceDatas() {
		IResourceInfo infos[] = fCfg.getResourceInfos();
		CResourceData datas[] = new CResourceData[infos.length];
		for(int i = 0; i < infos.length; i++){
			datas[i] = infos[i].getResourceData();
		}
		return datas;
	}

	public CFolderData getRootFolderData() {
		return fCfg.getRootFolderInfo().getFolderData();
	}

	public void removeResourceData(CResourceData data) throws CoreException {
		fCfg.removeResourceInfo(data.getPath());
	}

	public void setDescription(String description) {
		fCfg.setDescription(description);
	}

	public String getId() {
		return fCfg.getId();
	}

	public String getName() {
		return fCfg.getName();
	}

	public void setName(String name) {
		fCfg.setName(name);
	}

	public boolean isValid() {
		return fCfg != null;
	}

	public CTargetPlatformData getTargetPlatformData() {
		return fCfg.getToolChain().getTargetPlatformData();
	}

	public IPath[] getSourcePaths() {
		return fCfg.getSourcePaths();
	}

	public void setSourcePaths(IPath[] paths) {
		fCfg.setSourcePaths(paths);
	}

	public CBuildData getBuildData() {
		return fCfg.getBuildData();
	}

	public ICdtVariablesContributor getBuildVariablesContributor() {
		if(fCdtVars == null)
			fCdtVars = new BuildVariablesContributor(this);
		return fCdtVars;
	}
}
