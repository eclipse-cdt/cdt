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
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator.DiscoveredSettingInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator.ILangSettingInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.CDataDiscoveredInfoCalculator.IRcSettingInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class CDataDiscoveredInfoProcessor {

	public void applyDiscoveredInfo(CConfigurationData cfgData, DiscoveredSettingInfo dsIinfo){
		Map<IPath, CResourceData> map = CDataUtil.createPathRcDataMap(cfgData);
		IRcSettingInfo info;

		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
		
		IRcSettingInfo[] infos = dsIinfo.getRcSettingInfos();
		
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			applyInfo(cfgData, info, cr);
			map.remove(info.getResourceData().getPath());
		}
		
		if(map.size() != 0){
			CResourceData rcData = map.get(Path.EMPTY);
			if(rcData != null){
				info = CDataDiscoveredInfoCalculator.createEmptyRcSettingInfo((CFolderData)rcData);
				applyInfo(cfgData, info, cr);
				map.remove(Path.EMPTY);
			}
			
			if(map.size() != 0){
				Set<Entry<IPath, CResourceData>> entries = map.entrySet();
				for (Entry<IPath, CResourceData> entry : entries) {
					IPath path = entry.getKey();
					PathSettingsContainer curCr = cr.getChildContainer(path, false, false);
					rcData = entry.getValue();
					info = (IRcSettingInfo)curCr.getValue();
					applyInfo(cfgData, rcData, info);
				}
			}
		}
	}

	protected void applyInfo(CConfigurationData cfgData, CResourceData rcData, IRcSettingInfo info){
		CLanguageData[] lDatas = getLangDatas(rcData);
		ILangSettingInfo lInfo;
		ILangSettingInfo lInfos[] = info.getLangInfos();
		CLanguageData lData;
		for(int k = 0; k < lDatas.length; k++){
			lData = lDatas[k];
			lInfo = getMatch(lData, lInfos);
			if(lInfo != null){
				setInfoForData(cfgData, 
						rcData, 
						lData, 
						lInfo.getFilePathInfo(), 
						info.getResourceData(),
						lInfo.getLanguageData());
			} else {
				setInfoForData(cfgData, rcData, lData, null, null, null);
			}
		}
	}
	
	protected CLanguageData[] getLangDatas(CResourceData rcData){
		if(rcData.getType() == ICSettingBase.SETTING_FILE){
			CLanguageData lData = ((CFileData)rcData).getLanguageData();
			if(lData != null)
				return new CLanguageData[]{lData};
			return new CLanguageData[0];
		}
		return ((CFolderData)rcData).getLanguageDatas();
	}

	protected ILangSettingInfo getMatch(CLanguageData lData, ILangSettingInfo lInfos[]){
		ILangSettingInfo lInfo;
		for(int i = 0; i < lInfos.length; i++){
			lInfo = lInfos[i];
			if(langDatasMatch(lData, lInfo.getLanguageData()))
				return lInfo;
		}
		return null;
	}

	protected CLanguageData getMatch(CLanguageData lData, CLanguageData[] datas){
		for(int i = 0; i < datas.length; i++){
			if(langDatasMatch(lData, datas[i]))
				return datas[i];
		}
		return null;
	}

	protected boolean langDatasMatch(CLanguageData lData1, CLanguageData lData2){
		if(!CDataUtil.objectsEqual(lData1.getLanguageId(), lData2.getLanguageId()))
			return false;
		
		String[] tmp = lData1.getSourceContentTypeIds();
		if(tmp != null && tmp.length != 0){
			if(!Arrays.equals(tmp, lData2.getSourceContentTypeIds()))
				return false;
		} else {
			if(!Arrays.equals(lData1.getSourceExtensions(), lData2.getSourceExtensions()))
				return false;
		}
		return true;
	}

	protected void applyInfo(CConfigurationData cfgData, IRcSettingInfo info, PathSettingsContainer cr){
		CResourceData rcData;
		CLanguageData lData;
		ILangSettingInfo lInfo;
		rcData = info.getResourceData();
		IPath path = rcData.getPath();
		PathSettingsContainer curCr = cr.getChildContainer(path, true, true);
		curCr.setValue(info);
		ILangSettingInfo lInfos[] = info.getLangInfos();
		for(int k = 0; k < lInfos.length; k++){
			lInfo = lInfos[k];
			lData = lInfo.getLanguageData();
			setInfoForData(cfgData, rcData, lData, lInfo.getFilePathInfo(), null, null);
		}
	}
	
	protected abstract void setInfoForData(CConfigurationData cfgData, 
			CResourceData rcData, 
			CLanguageData lData,
			PathInfo pi,
			CResourceData baseRcData,
			CLanguageData baseLangData);
	
}
