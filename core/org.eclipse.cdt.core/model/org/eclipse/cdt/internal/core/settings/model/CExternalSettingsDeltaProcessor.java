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
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryContentsKey;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingsDelta;

public class CExternalSettingsDeltaProcessor {
	static void applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[]){
		ICResourceDescription rcDess[] = des.getResourceDescriptions();
		for(int i = 0; i < rcDess.length; i++){
			ICResourceDescription rcDes = rcDess[i];
			if(rcDes.getType() == ICSettingBase.SETTING_FOLDER){
				applyDelta((ICFolderDescription)rcDes, deltas);
			} else {
				applyDelta((ICFileDescription)rcDes, deltas);
			}
		}
	}
	
	private static void applyDelta(ICFileDescription des, ExtSettingsDelta deltas[]){
		ICLanguageSetting setting = des.getLanguageSetting();
		if(setting == null)
			return;
		for(int i = 0; i < deltas.length; i++){
			if(isSettingCompatible(setting, deltas[i].fSetting)){
				applyDelta(setting, deltas[i]);
			}
		}
	}

	private static void applyDelta(ICFolderDescription des, ExtSettingsDelta deltas[]){
		ICLanguageSetting settings[] = des.getLanguageSettings();
		if(settings == null || settings.length == 0)
			return;
		
		ICLanguageSetting setting;
		for(int k = 0; k < settings.length; k++){
			setting = settings[k];
			for(int i = 0; i < deltas.length; i++){
				if(isSettingCompatible(setting, deltas[i].fSetting)){
					applyDelta(setting, deltas[i]);
				}
			}
		}
	}

	private static void applyDelta(ICLanguageSetting setting, ExtSettingsDelta delta){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		ICLanguageSettingEntry entries[];
		ICSettingEntry diff[][];
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			diff = delta.getEntriesDelta(kind);
			if(diff == null)
				continue;
			
			entries = setting.getSettingEntries(kind);
			List list = calculateUpdatedEntries(entries, diff[0], diff[1]);
			
			if(list != null)
				setting.setSettingEntries(kind, list);
		}
	}
	
	private static List calculateUpdatedEntries(ICSettingEntry current[], ICSettingEntry added[], ICSettingEntry removed[]){
		LinkedHashMap map = new LinkedHashMap();
		boolean changed = false;
		if(added != null){
			CDataUtil.fillEntriesMapByContentsKey(map, added);
		}
		if(current != null){
			CDataUtil.fillEntriesMapByContentsKey(map, current);
			if(current.length != map.size()){
				changed = true;
			}
		} else {
			if(map.size() != 0){
				changed = true;
			}
		}
		if(removed != null){
			for(int i = 0; i < removed.length; i++){
				ICSettingEntry entry = removed[i];
				EntryContentsKey cKey = new EntryContentsKey(entry);
				ICSettingEntry cur = (ICSettingEntry)map.get(cKey);
				if(cur != null && !cur.isBuiltIn()){
					map.remove(cKey);
					changed = true;
				}
			}
		}
		return changed ? new ArrayList(map.values()) : null;
	}
	
	private static boolean isSettingCompatible(ICLanguageSetting setting, CExternalSetting provider){
		String ids[] = provider.getCompatibleLanguageIds();
		String id;
		if(ids != null && ids.length > 0){
			id = setting.getLanguageId();
			if(id != null){
				if(contains(ids, id))
					return true;
				return false;
			}
			return false;
		}
		
		ids = provider.getCompatibleContentTypeIds();
		if(ids != null && ids.length > 0){
			String[] cTypeIds = setting.getSourceContentTypeIds();
			if(cTypeIds.length != 0){
				for(int i = 0; i < cTypeIds.length; i++){
					id = cTypeIds[i];
					if(contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}
		
		ids = provider.getCompatibleExtensions();
		if(ids != null && ids.length > 0){
			String [] srcIds = setting.getSourceExtensions();
			if(srcIds.length != 0){
				for(int i = 0; i < srcIds.length; i++){
					id = srcIds[i];
					if(contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}
		return true;
	}
	
	private static boolean contains(Object array[], Object value){
		for(int i = 0; i < array.length; i++){
			if(array[i].equals(value))
				return true;
		}
		return false;
	}
}
