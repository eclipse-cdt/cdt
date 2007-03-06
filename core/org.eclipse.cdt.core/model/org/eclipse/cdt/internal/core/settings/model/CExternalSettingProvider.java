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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.ExternalSettingsManager.ExtSettingMapKey;

public class CExternalSettingProvider {
	private Map fSettingsMap;;
	static final String ELEMENT_EXT_SETTINGS_CONTAINER = "externalSettings"; //$NON-NLS-1$
	static final CExternalSetting[] EMPTY_EXT_SETTINGS_ARRAY = new CExternalSetting[0];
	
	private boolean fIsModified;

	CExternalSettingProvider(){
		
	}

	CExternalSettingProvider(ICStorageElement element){
		ICStorageElement children[] = element.getChildren();
		List externalSettingList = null;
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			
			if(CExternalSetting.ELEMENT_SETTING_INFO.equals(name)){
				if(externalSettingList == null)
					externalSettingList = new ArrayList();
				
				CExternalSetting setting = new CExternalSetting(child);
				externalSettingList.add(setting);
			}
		}
		
		if(externalSettingList != null && externalSettingList.size() != 0){
			for(int i = 0; i < externalSettingList.size(); i++){
				ICExternalSetting setting = (ICExternalSetting)externalSettingList.get(i);
				createExternalSetting(setting.getCompatibleLanguageIds(),
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(), 
						setting.getEntries());
			}
		}
	}

	CExternalSettingProvider(CExternalSettingProvider base){
		if(base.fSettingsMap != null)
			fSettingsMap = new HashMap(base.fSettingsMap);
	}

	public ICExternalSetting[] getExternalSettings(){
		if(fSettingsMap != null)
			return (ICExternalSetting[])fSettingsMap.values().toArray(new ICExternalSetting[fSettingsMap.size()]);
		return EMPTY_EXT_SETTINGS_ARRAY;
	}
	
	void setExternallSetting(ICExternalSetting[] settings){
		removeExternalSettings();

		for(int i = 0; i < settings.length; i++){
			ICExternalSetting setting = settings[i];
			createExternalSetting(setting.getCompatibleLanguageIds(),
					setting.getCompatibleContentTypeIds(),
					setting.getCompatibleExtensions(),
					setting.getEntries());
		}
		fIsModified = true;
	}

	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIDs, String[] extensions,
			ICLanguageSettingEntry[] entries) {
		return createExternalSetting(new CExternalSetting(languageIDs, contentTypeIDs, extensions, entries));
	}

	private ICExternalSetting createExternalSetting(ICExternalSetting setting){
		ExtSettingMapKey key = new ExtSettingMapKey(setting);
		if(fSettingsMap != null){
			CExternalSetting newSetting = (CExternalSetting)fSettingsMap.get(key);
			if(newSetting == null){
				newSetting = new CExternalSetting(setting);
			} else {
				newSetting = new CExternalSetting(newSetting, setting.getEntries());
			}
			
			fSettingsMap.put(key, newSetting);
		} else {
			CExternalSetting newSetting = new CExternalSetting(setting);
			fSettingsMap = new HashMap();
			fSettingsMap.put(key, newSetting);
		}
		fIsModified = true;
		return setting;
		
	}

	public void removeExternalSetting(ICExternalSetting setting) {
		if(fSettingsMap != null){
			
			ExtSettingMapKey key = new ExtSettingMapKey(setting);
			ICExternalSetting settingToRemove = (ICExternalSetting)fSettingsMap.get(key);
			if(setting.equals(settingToRemove)){
				fSettingsMap.remove(key);
				fIsModified = true;
			}
		}
	}

	public void removeExternalSettings() {
		if(fSettingsMap != null){
			fSettingsMap.clear();
			fSettingsMap = null;
			fIsModified = true;
		}
	}
	
	public void serialize(ICStorageElement el){
		if(fSettingsMap != null && fSettingsMap.size() != 0){
			for(Iterator iter = fSettingsMap.values().iterator(); iter.hasNext();){
				CExternalSetting setting = (CExternalSetting)iter.next();
				ICStorageElement child = el.createChild(CExternalSetting.ELEMENT_SETTING_INFO);
				setting.serialize(child);
			}
		}
	}
	
	public boolean isModified(){
		return fIsModified;
	}
}
