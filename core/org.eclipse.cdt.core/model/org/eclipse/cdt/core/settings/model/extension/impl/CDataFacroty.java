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

public class CDataFacroty {
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
			boolean clone, 
			IPath path){
		String id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		return new CDefaultFolderData(id, path, base, cfg, this, clone);
	}

	public CFileData createFileData(CConfigurationData cfg, 
			CResourceData base,
			CLanguageData lBase,
			boolean clone, 
			IPath path){
		String id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		if(base.getType() == ICSettingBase.SETTING_FILE)
			return new CDefaultFileData(id, path, (CFileData)base, cfg, this, clone);
		return new CDefaultFileData(id, path, (CFolderData)base, lBase, cfg, this);
	}

	public CLanguageData createLanguageData(CConfigurationData cfg, 
			CResourceData rcBase,
			CLanguageData base,
			boolean clone){
		String id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
		return new CDefaultLanguageData(id, base);
	}

	public CLanguageData createLanguageData(CConfigurationData cfg, 
			CResourceData rcBase,
			String languageId,
			String[] rcTypes,
			boolean isContentTypes){
		String id = CDataUtil.genId(rcBase.getId());
		return new CDefaultLanguageData(id, languageId, rcTypes, isContentTypes);
	}

	public CBuildData createBuildData(CConfigurationData cfg, CBuildData base, boolean clone){
		String id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		return new CDefaultBuildData(id, base);
	}
	
	public CTargetPlatformData createTargetPlatformData(CConfigurationData cfg, CTargetPlatformData base, boolean clone){
		String id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
		return new CDefaultTargetPlatformData(id, base);
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

}
