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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryContentsKey;
import org.eclipse.cdt.core.settings.model.util.EntryNameKey;
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
		KindBasedStore<ICSettingEntry[][]> fEntryChangeStore;

		ExtSettingsDelta(CExternalSetting setting){
			fSetting = setting;
			fEntryChangeStore = new KindBasedStore<ICSettingEntry[][]>(false);
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
				return fEntryChangeStore.get(kind);
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
			List<ICSettingEntry> added = new ArrayList<ICSettingEntry>();
			List<ICSettingEntry> removed = new ArrayList<ICSettingEntry>();
			for (int kind : kinds) {
				ICSettingEntry[][] d = getEntriesDelta(kind);
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
				delta[0] = added.toArray(new ICSettingEntry[added.size()]);
			}
			if(removed.size() != 0){
				delta[1] = removed.toArray(new ICSettingEntry[removed.size()]);
			}

			return delta;
		}
	}

	 static class ExtSettingMapKey {
		private ICExternalSetting fSetting;
		public ExtSettingMapKey(ICExternalSetting setting){
			fSetting = setting;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;

			if(!(obj instanceof ExtSettingMapKey))
				return false;

			ExtSettingMapKey other = (ExtSettingMapKey)obj;
			return settingsMatch(fSetting, other.fSetting);
		}
		@Override
		public int hashCode() {
			return code(fSetting.getCompatibleLanguageIds())
				+ code(fSetting.getCompatibleContentTypeIds())
				+ code(fSetting.getCompatibleExtensions());
		}

		private int code(String[] arr){
			if(arr == null || arr.length == 0)
				return 0;

			int code = 0;

			for (String str : arr) {
				code += str.hashCode();
			}
			return code;
		}

		public ICExternalSetting getSetting(){
			return fSetting;
		}

	}

	private static ExtSettingsDelta createDelta(CExternalSetting setting1, CExternalSetting setting2){

		int kinds[] = KindBasedStore.getAllEntryKinds();
		ExtSettingsDelta extDelta = null;
		for (int kind : kinds) {
			ICSettingEntry entries1[] = setting1.getEntries(kind);
			ICSettingEntry entries2[] = setting2.getEntries(kind);
			Map<EntryContentsKey, ICSettingEntry> map1 = CDataUtil.fillEntriesMapByContentsKey(new LinkedHashMap<EntryContentsKey, ICSettingEntry>(), entries1);
			Map<EntryContentsKey, ICSettingEntry> map2 = CDataUtil.fillEntriesMapByContentsKey(new LinkedHashMap<EntryContentsKey, ICSettingEntry>(), entries2);
			Map<EntryContentsKey, ICSettingEntry> map1Copy = new LinkedHashMap<EntryContentsKey, ICSettingEntry>(map1);
//			Set set1 = new HashSet(Arrays.asList(entries1));
//			Set set2 = new HashSet(Arrays.asList(entries2));
//			Set set1Copy = new HashSet(set1);
			map1.keySet().removeAll(map2.keySet());
			map2.keySet().removeAll(map1Copy.keySet());

			ICSettingEntry entriesAdded[] = null, entriesRemoved[] = null;

			if(map1.size() != 0)
				entriesAdded = map1.values().toArray(new ICSettingEntry[map1.size()]);

			if(map2.size() != 0)
				entriesRemoved = map2.values().toArray(new ICSettingEntry[map2.size()]);

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

	private static Map<ExtSettingMapKey, ICExternalSetting> toSettingsKeyMap(ICExternalSetting[] settings){
		Map<ExtSettingMapKey, ICExternalSetting> map = new HashMap<ExtSettingMapKey, ICExternalSetting>();
		for (ICExternalSetting setting : settings) {
			if(map.put(new ExtSettingMapKey(setting), setting) != null)
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

		List<ExtSettingsDelta> deltaList = new ArrayList<ExtSettingsDelta>();

		Map<ExtSettingMapKey, ICExternalSetting> newMap= toSettingsKeyMap(newSettings);
		Map<ExtSettingMapKey, ICExternalSetting> oldMap = toSettingsKeyMap(oldSettings);
		for (Entry<ExtSettingMapKey, ICExternalSetting> entry : newMap.entrySet()) {
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

		for (ICExternalSetting oldSettng : oldMap.values()) {
			deltaList.add(new ExtSettingsDelta((CExternalSetting)oldSettng, false));
		}

		if(deltaList.size() == 0)
			return null;
		return deltaList.toArray(new ExtSettingsDelta[deltaList.size()]);
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

	Set<ICSettingEntry> calculateUpdatedEntries(ICSettingEntry current[], ICSettingEntry added[], ICSettingEntry removed[]){
	//	EntryComparator comparator = new EntryComparator();
		LinkedHashSet<ICSettingEntry> set = new LinkedHashSet<ICSettingEntry>();
		set.addAll(Arrays.asList(current));
		set.addAll(Arrays.asList(added));
		set.removeAll(Arrays.asList(removed));

		return set;
	}

	static ICSettingEntry[][] getAllEntries(ExtSettingsDelta[] deltas, int kind){
		if(deltas == null || deltas.length == 0)
			return null;

		Map<EntryNameKey, ICSettingEntry> addedMap = new LinkedHashMap<EntryNameKey, ICSettingEntry>();
		Map<EntryNameKey, ICSettingEntry> removedMap = new LinkedHashMap<EntryNameKey, ICSettingEntry>();
		for (ExtSettingsDelta delta : deltas) {
			ICSettingEntry[][] change = delta.getEntriesDelta(kind);
			if(change == null)
				continue;

			if(change[0] != null){
				CDataUtil.fillEntriesMapByNameKey(addedMap, change[0]);
			}
			if(change[1] != null){
				CDataUtil.fillEntriesMapByNameKey(removedMap, change[1]);
			}
			removedMap.keySet().removeAll(addedMap.keySet());
		}

		if(addedMap.size() == 0 && removedMap.size() == 0)
			return null;

		ICSettingEntry[][] result = new ICSettingEntry[2][];
		if(addedMap.size() != 0){
			result[0] = addedMap.values().toArray(new ICSettingEntry[addedMap.size()]);
		}
		if(removedMap.size() != 0){
			result[1] = removedMap.values().toArray(new ICSettingEntry[removedMap.size()]);
		}

		return result;
	}

}
