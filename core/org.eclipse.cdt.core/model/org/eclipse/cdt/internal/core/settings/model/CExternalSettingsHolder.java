/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingMapKey;

/**
 * The raw external settings as exported by a project configuration.
 */
public class CExternalSettingsHolder extends CExternalSettingsContainer {

	private Map<ExtSettingMapKey, CExternalSetting> fSettingsMap;
	static final String ELEMENT_EXT_SETTINGS_CONTAINER = "externalSettings"; //$NON-NLS-1$
	
	private boolean fIsModified;

	CExternalSettingsHolder(){
		
	}

	CExternalSettingsHolder(ICStorageElement element){
		ICStorageElement children[] = element.getChildren();
		List<CExternalSetting> externalSettingList = null;
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			
			if(CExternalSettingSerializer.ELEMENT_SETTING_INFO.equals(name)){
				if(externalSettingList == null)
					externalSettingList = new ArrayList<CExternalSetting>();
				
				CExternalSetting setting = CExternalSettingSerializer.load(child);
				externalSettingList.add(setting);
			}
		}
		
		if(externalSettingList != null && externalSettingList.size() != 0){
			for(int i = 0; i < externalSettingList.size(); i++){
				CExternalSetting setting = externalSettingList.get(i);
				createExternalSetting(setting.getCompatibleLanguageIds(),
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(), 
						setting.getEntries());
			}
		}
	}

	CExternalSettingsHolder(CExternalSettingsHolder base){
		if(base.fSettingsMap != null)
			fSettingsMap = new HashMap<ExtSettingMapKey, CExternalSetting>(base.fSettingsMap);
	}

	@Override
	public CExternalSetting[] getExternalSettings(){
		if(fSettingsMap != null)
			return fSettingsMap.values().toArray(new CExternalSetting[fSettingsMap.size()]);
		return EMPTY_EXT_SETTINGS_ARRAY;
	}

	void setExternalSettings(CExternalSetting[] settings, boolean add){
		if(!add)
			removeExternalSettings();

		if(settings != null){
			for(int i = 0; i < settings.length; i++){
				CExternalSetting setting = settings[i];
				createExternalSetting(setting.getCompatibleLanguageIds(),
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(),
						setting.getEntries());
			}
		}
		fIsModified = true;
	}
	
	void addExternalSettings(CExternalSetting[] settings){
		setExternalSettings(settings, true);
	}

	public CExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIDs, String[] extensions,
			ICSettingEntry[] entries) {
		return createExternalSetting(new CExternalSetting(languageIDs, contentTypeIDs, extensions, entries));
	}

	private CExternalSetting createExternalSetting(CExternalSetting setting){
		ExtSettingMapKey key = new ExtSettingMapKey(setting);
		CExternalSetting newSetting;
		if(fSettingsMap != null){
			newSetting = fSettingsMap.get(key);
			if(newSetting == null){
				newSetting = new CExternalSetting(setting);
			} else {
				newSetting = new CExternalSetting(newSetting, setting.getEntries());
			}
			
			fSettingsMap.put(key, newSetting);
		} else {
			newSetting = new CExternalSetting(setting);
			fSettingsMap = new HashMap<ExtSettingMapKey, CExternalSetting>();
			fSettingsMap.put(key, newSetting);
		}
		fIsModified = true;
		return newSetting;
	}

	public void removeExternalSetting(CExternalSetting setting) {
		if(fSettingsMap != null){
			
			ExtSettingMapKey key = new ExtSettingMapKey(setting);
			CExternalSetting settingToRemove = fSettingsMap.get(key);
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
			for(Iterator<CExternalSetting> iter = fSettingsMap.values().iterator(); iter.hasNext();){
				CExternalSetting setting = iter.next();
				ICStorageElement child = el.createChild(CExternalSettingSerializer.ELEMENT_SETTING_INFO);
				CExternalSettingSerializer.store(setting, child);
			}
		}
	}
	
	public boolean isModified(){
		return fIsModified;
	}
}
