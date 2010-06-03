/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension.impl;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.IPath;

public class CDataFactory {
	private static CDataFactory fInstance;
	
	public static CDataFactory getDefault(){
		if(fInstance == null)
			fInstance = new CDataFactory();
		return fInstance;
	}

	public CConfigurationData createConfigurationdata(String id, 
			String name, 
			CConfigurationData base, 
			boolean clone){
		if(clone){
			id = base.getId();
		} else if(id == null){
			id = CDataUtil.genId(null);
		}
		
		return new CDefaultConfigurationData(id, name, base, this, clone);
	}

	public CFolderData createFolderData(CConfigurationData cfg, 
			CFolderData base,
			String id,
			boolean clone, 
			IPath path){
		if(id == null)
			id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		return new CDefaultFolderData(id, path, base, cfg, this, clone);
	}

	public CFileData createFileData(CConfigurationData cfg, 
			CResourceData base,
			CLanguageData lBase,
			String id,
			boolean clone, 
			IPath path){
		if(id == null)
			id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		if(base != null && base.getType() == ICSettingBase.SETTING_FILE)
			return new CDefaultFileData(id, path, (CFileData)base, cfg, this, clone);
		return new CDefaultFileData(id, path, (CFolderData)base, lBase, cfg, this);
	}

	public CLanguageData createLanguageData(CConfigurationData cfg, 
			CResourceData rcBase,
			CLanguageData base,
			String id,
			boolean clone){
			if(id == null)
				id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
		return new CDefaultLanguageData(id, base);
	}

	public CLanguageData createLanguageData(CConfigurationData cfg, 
			CResourceData rcBase,
			String id,
			String name,
			String languageId,
			int supportedEntryKinds,
			String[] rcTypes,
			boolean isContentTypes){
		if(id == null)
			id = CDataUtil.genId(rcBase.getId());
		CDefaultLanguageData lData = new CDefaultLanguageData(id, languageId, rcTypes, isContentTypes);
		lData.fName = name;
		lData.fSupportedKinds = supportedEntryKinds;
		return lData;
	}

	public CBuildData createBuildData(CConfigurationData cfg, CBuildData base, String id, String name, boolean clone){
		if(id == null)
			id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		CDefaultBuildData data = new CDefaultBuildData(id, base);
		data.fName = name;
		return data;
	}
	
	public CTargetPlatformData createTargetPlatformData(CConfigurationData cfg, CTargetPlatformData base, String id, String name, boolean clone){
		if(id == null)
			id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		CDefaultTargetPlatformData tpData = new CDefaultTargetPlatformData(id, base);
		tpData.fName = name;
		return tpData;
	}

	public boolean isModified(CDataObject data){
		switch (data.getType()) {
		case ICSettingBase.SETTING_CONFIGURATION:
			return ((CDefaultConfigurationData)data).isModified();
		case ICSettingBase.SETTING_FOLDER:
			return ((CDefaultFolderData)data).isModified();
		case ICSettingBase.SETTING_FILE:
			return ((CDefaultFileData)data).isModified();
		case ICSettingBase.SETTING_LANGUAGE:
			return ((CDefaultLanguageData)data).isModified();
		case ICSettingBase.SETTING_TARGET_PLATFORM:
			return ((CDefaultTargetPlatformData)data).isModified();
		case ICSettingBase.SETTING_BUILD:
			return ((CDefaultBuildData)data).isModified();
		}
		return false;
	}

	public void setModified(CDataObject data, boolean modified){
		if(data == null)
			return;
		switch (data.getType()) {
		case ICSettingBase.SETTING_CONFIGURATION:
			((CDefaultConfigurationData)data).setModified(modified);
			break;
		case ICSettingBase.SETTING_FOLDER:
			((CDefaultFolderData)data).setModified(modified);
			break;
		case ICSettingBase.SETTING_FILE:
			((CDefaultFileData)data).setModified(modified);
			break;
		case ICSettingBase.SETTING_LANGUAGE:
			((CDefaultLanguageData)data).setModified(modified);
			break;
		case ICSettingBase.SETTING_TARGET_PLATFORM:
			((CDefaultTargetPlatformData)data).setModified(modified);
			break;
		case ICSettingBase.SETTING_BUILD:
			((CDefaultBuildData)data).setModified(modified);
			break;
		}
	}
	
	public void link(CDataObject parent, CDataObject child){
		switch(parent.getType()){
		case ICSettingBase.SETTING_CONFIGURATION:
			switch(child.getType()){
			case ICSettingBase.SETTING_FILE:
			case ICSettingBase.SETTING_FOLDER:
				((CDefaultConfigurationData)parent).addRcData((CResourceData)child);
				break;
			case ICSettingBase.SETTING_TARGET_PLATFORM:
				((CDefaultConfigurationData)parent).fTargetPlatformData = (CTargetPlatformData)child;
				break;
			case ICSettingBase.SETTING_BUILD:
				((CDefaultConfigurationData)parent).fBuildData = (CBuildData)child;
				break;
			}
			break;
		case ICSettingBase.SETTING_FOLDER:
			((CDefaultFolderData)parent).fLanguageDatas.add((CLanguageData)child);
			break;
		case ICSettingBase.SETTING_FILE:
			((CDefaultFileData)parent).fLanguageData = (CLanguageData)child;
			break;
		}
	}
}
