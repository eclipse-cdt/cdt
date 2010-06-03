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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


public abstract class CConfigurationData extends CDataObject {

	protected CConfigurationData() {
	}

	@Override
	public final int getType(){
		return SETTING_CONFIGURATION;
	}
	
	public abstract CFolderData getRootFolderData();

	public abstract CResourceData[] getResourceDatas();

//	public abstract CResourceData getResourceData(IPath path);
	
	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract void removeResourceData(CResourceData data) throws CoreException;

	public abstract CFolderData createFolderData(IPath path, CFolderData base) throws CoreException;

	public abstract CFileData createFileData(IPath path, CFileData base) throws CoreException;

	public abstract CFileData createFileData(IPath path, CFolderData base, CLanguageData langData) throws CoreException;

//	public abstract CDataObject[] getChildrenOfKind(int kind);

//	public abstract CDataObject getChildById(String id);
	
	public abstract CTargetPlatformData getTargetPlatformData();
	
	public abstract ICSourceEntry[] getSourceEntries();

	public abstract void setSourceEntries(ICSourceEntry[] entries);
	
	public abstract CBuildData getBuildData();
	
	public abstract ICdtVariablesContributor getBuildVariablesContributor();
	
	public abstract void setName(String name);
	
	public CConfigurationStatus getStatus(){
		return CConfigurationStatus.CFG_STATUS_OK;
	}
}
