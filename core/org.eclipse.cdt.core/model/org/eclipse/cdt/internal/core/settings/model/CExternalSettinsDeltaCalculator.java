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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;

class CExternalSettinsDeltaCalculator {
	static private CExternalSettinsDeltaCalculator fInstance;
	
	private CExternalSettinsDeltaCalculator(){
	}
	
	public static CExternalSettinsDeltaCalculator getInstance(){
		if(fInstance == null)
			fInstance = new CExternalSettinsDeltaCalculator();
		return fInstance;
	}

	
	static class ExtSettingsDelta {
		CExternalSetting fSetting;
		boolean fAdded;
		KindBasedStore fEntryChangeStore;

		ExtSettingsDelta(CExternalSetting setting){
			fSetting = setting;
			fEntryChangeStore = new KindBasedStore();
		}

		ExtSettingsDelta(CExternalSetting setting, boolean added){
			fSetting = setting;
			fAdded = added;
		}
		
		boolean isChange(){
			return fEntryChangeStore != null;
		}
		
		boolean isAdded(){
			return fAdded;
		}
		
		CExternalSetting getSetting(){
			return fSetting;
		}
		
		ICSettingEntry[][] getEntriesDelta(int kind){
			if(fEntryChangeStore != null)
				return (ICSettingEntry[][])fEntryChangeStore.get(kind);
			ICSettingEntry [] entries = fSetting.getEntries(kind);
			if(entries == null || entries.length == 0)
				return null;
			
			ICSettingEntry[][] delta = new ICSettingEntry[2][];
			if(fAdded)
				delta[0] = entries;
			else 
				delta[1] = entries;
			
			return delta;
		}
		
		ICSettingEntry[][] getEntriesDelta(){
			int kinds[] = KindBasedStore.getLanguageEntryKinds();
			List added = new ArrayList();
			List removed = new ArrayList();
			for(int i = 0; i < kinds.length; i++){
				ICSettingEntry[][] d = getEntriesDelta(kinds[i]);
				if(d == null)
					continue;
				
				if(d[0] != null){
					added.addAll(Arrays.asList(d[0]));
				}
				if(d[1] != null){
					removed.addAll(Arrays.asList(d[1]));
				}
			}

			ICSettingEntry[][] delta = new ICSettingEntry[2][];
			
			if(added.size() != 0){
				delta[0] = (ICSettingEntry[])added.toArray(new ICSettingEntry[added.size()]);
			}
			if(removed.size() != 0){
				delta[1] = (ICSettingEntry[])removed.toArray(new ICSettingEntry[removed.size()]);
			}
		
			return delta;
		}
	}
	
	 static class ExtSettingMapKey {
		private ICExternalSetting fSetting;
		public ExtSettingMapKey(ICExternalSetting setting){
			fSetting = setting;
		}
		
		public boolean equals(Object obj) {
			if(obj == this)
				return true;

			if(!(obj instanceof ExtSettingMapKey))
				return false;
			
			ExtSettingMapKey other = (ExtSettingMapKey)obj;
			return settingsMatch(fSetting, other.fSetting);
		}
		public int hashCode() {
			return code(fSetting.getCompatibleLanguageIds())
				+ code(fSetting.getCompatibleContentTypeIds())
				+ code(fSetting.getCompatibleExtensions());
		}
		
		private int code(String[] arr){
			if(arr == null || arr.length == 0)
				return 0;
			
			int code = 0;
			
			for(int i = 0; i < arr.length; i++){
				code += arr[i].hashCode();
			}
			return code;
		}
		
		public ICExternalSetting getSetting(){
			return fSetting;
		}
		
	}

	private static ExtSettingsDelta createDelta(CExternalSetting setting1, CExternalSetting setting2){

		int kind;
		int kinds[] = KindBasedStore.getAllEntryKinds();
		ExtSettingsDelta extDelta = null;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			ICSettingEntry entries1[] = setting1.getEntries(kind);
			ICSettingEntry entries2[] = setting2.getEntries(kind);
			Map map1 = CDataUtil.fillEntriesMapByContentsKey(new LinkedHashMap(), entries1); 
			Map map2 = CDataUtil.fillEntriesMapByContentsKey(new LinkedHashMap(), entries2);
			Map map1Copy = new LinkedHashMap(map1);
//			Set set1 = new HashSet(Arrays.asList(entries1));
//			Set set2 = new HashSet(Arrays.asList(entries2));
//			Set set1Copy = new HashSet(set1);
			map1.keySet().removeAll(map2.keySet());
			map2.keySet().removeAll(map1Copy.keySet());

			ICSettingEntry entriesAdded[] = null, entriesRemoved[] = null;

			if(map1.size() != 0)
				entriesAdded = (ICSettingEntry[])map1.values().toArray(new ICSettingEntry[map1.size()]);

			if(map2.size() != 0)
				entriesRemoved = (ICSettingEntry[])map2.values().toArray(new ICSettingEntry[map2.size()]);
			
			if(entriesAdded == null && entriesRemoved == null)
				continue;
			
			if(extDelta == null){
				extDelta = new ExtSettingsDelta(setting1);
			}
			extDelta.fEntryChangeStore.put(kind, new ICSettingEntry[][]{entriesAdded, entriesRemoved});
		}
		
		return extDelta;
	}
	
	static boolean settingsMatch(ICExternalSetting setting1, ICExternalSetting setting2) {
		if(setting1.equals(setting2))
			return true;
		
		return settingsMatch(setting1, 
				setting2.getCompatibleLanguageIds(),
				setting2.getCompatibleContentTypeIds(),
				setting2.getCompatibleExtensions());
	}

	static boolean settingsMatch(ICExternalSetting setting, 
			String languageIDs[], String contentTypeIDs[], String extensions[]){
		if(!Arrays.equals(setting.getCompatibleLanguageIds(), languageIDs))
			return false;
		if(!Arrays.equals(setting.getCompatibleContentTypeIds(), contentTypeIDs))
			return false;
		if(!Arrays.equals(setting.getCompatibleExtensions(), extensions))
			return false;

		return true;
	}
	
	private static Map toSettingsKeyMap(ICExternalSetting[] settings){
		Map map = new HashMap();
		for(int i = 0; i < settings.length; i++){
			if(map.put(new ExtSettingMapKey(settings[i]), settings[i]) != null)
				throw new IllegalArgumentException();
		}
		return map;
	}

	ExtSettingsDelta[] getSettingChange(CExternalSetting newSettings[],
			CExternalSetting oldSettings[]){


		if(newSettings == null || newSettings.length == 0)
			return createDeltas(oldSettings, false);
		if(oldSettings == null || oldSettings.length == 0)
			return createDeltas(newSettings, true);

		List deltaList = new ArrayList();
		
		Map newMap= toSettingsKeyMap(newSettings);
		Map oldMap = toSettingsKeyMap(oldSettings);
		for(Iterator iter = newMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			CExternalSetting newSetting = (CExternalSetting)entry.getValue();
			CExternalSetting oldSetting = (CExternalSetting)oldMap.remove(entry.getKey());
			if(oldSetting == null){
				deltaList.add(new ExtSettingsDelta(newSetting, true));
			} else {
				ExtSettingsDelta delta = createDelta(newSetting, oldSetting);
				if(delta != null)
					deltaList.add(delta);
			}
		}
		
		for(Iterator iter = oldMap.values().iterator(); iter.hasNext();){
			CExternalSetting oldSettng = (CExternalSetting)iter.next();
			deltaList.add(new ExtSettingsDelta(oldSettng, false));
		}
		
		if(deltaList.size() == 0)
			return null;
		return (ExtSettingsDelta[])deltaList.toArray(new ExtSettingsDelta[deltaList.size()]);
	}
	
	private static ExtSettingsDelta[] createDeltas(CExternalSetting settings[], boolean added){
		if(settings == null || settings.length == 0)
			return null;
		
		ExtSettingsDelta deltas[] = new ExtSettingsDelta[settings.length];
		for(int i = 0; i < settings.length; i++){
			deltas[i] = new ExtSettingsDelta(settings[i], added);
		}
		
		return deltas;
	}

	Set calculateUpdatedEntries(ICSettingEntry current[], ICSettingEntry added[], ICSettingEntry removed[]){
	//	EntryComparator comparator = new EntryComparator();
		LinkedHashSet set = new LinkedHashSet();
		set.addAll(Arrays.asList(current));
		set.addAll(Arrays.asList(added));
		set.removeAll(Arrays.asList(removed));
		
		return set;
	}

}
