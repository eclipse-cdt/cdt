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
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class SettingsSet {
	public static final int READ_ONLY = 1;
	public static final int WRITABLE = 1 << 1;

	private SettingLevel[] fLevels;
	public class SettingLevel {
		private int fFlagsToSet;
		private int fFlagsToClear;
		private boolean fIsReadOnly;
		private boolean fIsOverrideSupported;
		private LinkedHashMap<EntryNameKey, EntryInfo> fEntries;
		HashSet<String> fOverrideSet;
		private Object fContext;
		
		private SettingLevel(){
			fEntries = new LinkedHashMap<EntryNameKey, EntryInfo>();
		}
		
		public boolean isReadOnly(){
			return fIsReadOnly;
		}
		
		public void setReadOnly(boolean readOnly){
			fIsReadOnly = readOnly;
		}

		public boolean isOverrideSupported(){
			return fIsOverrideSupported;
		}
		
		public void setOverrideSupported(boolean supported){
			fIsOverrideSupported = supported;
		}

		public void setFlagsToSet(int flags){
			fFlagsToSet = flags;
		}
		
		public boolean containsOverrideInfo(){
			return fOverrideSet != null;
		}

		public void setFlagsToClear(int flags){
			fFlagsToClear = flags;
		}
		
		public int getFlagsToSet(){
			return fFlagsToSet;
		}
		
		public int getFlagsToClear(){
			return fFlagsToClear;
		}
		
		public int getFlags(int baseFlags){
			return (baseFlags | fFlagsToSet) & (~fFlagsToClear);
		}
		
		@SuppressWarnings("unchecked")
		public Set<String> getOverrideSet(){
			if(fOverrideSet != null)
				return (HashSet<String>)fOverrideSet.clone();
			return new HashSet<String>();
		}

		public void addEntries(ICLanguageSettingEntry entries[]){
			if(entries != null){
				for(int i = 0; i < entries.length; i++){
					addEntry(entries[i]);
				}
			}
		}

		public void addEntries(List<ICLanguageSettingEntry> list){
			for(ICLanguageSettingEntry se : list)
				addEntry(se);
		}
		
		public void addEntry(ICLanguageSettingEntry entry){
			addEntry(entry, null);
		}

		public void addEntry(ICLanguageSettingEntry entry, Object customInfo){
			entry = CDataUtil.createEntry(entry, fFlagsToSet, fFlagsToClear);
			EntryInfo info = new EntryInfo(entry, customInfo);
			fEntries.put(info.getContentsKey(), info);
		}
		
		public void addOverrideName(String name){
			if(fOverrideSet == null)
				fOverrideSet = new HashSet<String>();
			
			fOverrideSet.add(name);
		}
		
		public void addOverrideNameSet(Set<String> set){
			if(set == null)
				return;
			if(fOverrideSet != null){
				fOverrideSet.addAll(set);
			} else if(set.size() != 0){
				fOverrideSet = new HashSet<String>(set);
			}
		}

		public void removeOverrideName(String name){
			if(fOverrideSet == null)
				return;
			
			fOverrideSet.remove(name);
			
			if(fOverrideSet.size() == 0)
				fOverrideSet = null;
		}

		public void clear(){
			fEntries.clear();
			fOverrideSet = null;
		}
		
		public Map<EntryNameKey, EntryInfo> clearAndGetMap(){
			Map<EntryNameKey, EntryInfo> map = fEntries;
			fEntries = new LinkedHashMap<EntryNameKey, EntryInfo>();
			fOverrideSet = null;
			return map;
		}
		
		public EntryInfo[] getInfos(){
			return fEntries.values().toArray(new EntryInfo[fEntries.size()]);
		}
		
		public ICLanguageSettingEntry[] getEntries(){
			List<ICLanguageSettingEntry> list = getEntriesList(false);
			return list.toArray(new ICLanguageSettingEntry[list.size()]);
		}

		public ICLanguageSettingEntry[] getEntries(boolean includeOverridden){
			List<ICLanguageSettingEntry> list = getEntriesList(includeOverridden);
			return list.toArray(new ICLanguageSettingEntry[list.size()]);
		}

		public List<ICLanguageSettingEntry> getEntriesList(boolean includeOverridden){
			List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
			EntryInfo infos[] = getInfos();
			for(EntryInfo info : infos){
				if(includeOverridden || !info.isOverridden())
					list.add(info.getEntry());
			}
			
			return list;
		}
		
		public Object getContext(){
			return fContext;
		}
		
		public void setContext(Object context){
			fContext = context;
		}
	}
	
	public static class EntryInfo {
		private ICLanguageSettingEntry fEntry;
		private EntryNameKey fNameKey;
		private boolean fIsOverRidden;
		private Object fCustomInfo;

		private EntryInfo(ICLanguageSettingEntry entry, Object customInfo){
			fEntry = entry;
			fCustomInfo = customInfo;
		}

		public EntryNameKey getContentsKey(){
			if(fNameKey == null){
				fNameKey = new EntryNameKey(fEntry);
			}
			return fNameKey;
		}
		
		private void makeOverridden(boolean overrridden){
			fIsOverRidden = overrridden;
		}

		public ICLanguageSettingEntry getEntry(){
			return fEntry;
		}

		public boolean isOverridden(){
			return fIsOverRidden;
		}

		public Object getCustomInfo(){
			return fCustomInfo;
		}
	}
	
	public SettingsSet(int num){
		fLevels = new SettingLevel[num];
		for(int i = 0; i < num; i++){
			fLevels[i] = new SettingLevel();
		}
	}
	
	public SettingLevel[] getLevels(){
		return fLevels.clone();
	}
	
	public void adjustOverrideState(){
		Set<String> set = new HashSet<String>();
		SettingLevel level;
		for(int i = 0; i < fLevels.length; i++){
			level = fLevels[i];
			if(level.isOverrideSupported() && level.fOverrideSet != null)
				set.addAll(level.fOverrideSet);
			adjustOverrideState(fLevels[i], set);
		}
	}
	
	private void adjustOverrideState(SettingLevel level, Set<String> overridenSet){
		for(EntryInfo info : level.getInfos()){
			if(overridenSet.add(info.getEntry().getName())){
				info.makeOverridden(false);
			} else {
				info.makeOverridden(true);
			}
		}
	}

	public ICLanguageSettingEntry[] getEntries(){
		return getEntries(READ_ONLY | WRITABLE);
	}

	public ICLanguageSettingEntry[] getEntries(int types){
		adjustOverrideState();
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		for(SettingLevel sl : fLevels){
			if(isCompatible(sl, types))
				getEntries(sl, entries);
		}
		return entries.toArray(new ICLanguageSettingEntry[entries.size()]);
	}
	
	private void getEntries(SettingLevel level, List<ICLanguageSettingEntry> list){
		for(EntryInfo info : level.getInfos())
			if(!info.isOverridden())
				list.add(info.getEntry());
	}
	
	private boolean isCompatible(SettingLevel level, int types){
		if((types & READ_ONLY) == 0 && level.isReadOnly())
			return false;
		if((types & WRITABLE) == 0 && !level.isReadOnly())
			return false;
		
		return true;
	}
	
	private int getWritableLevelNum(){
		for(int i = 0; i <fLevels.length; i++){
			if(!fLevels[i].isReadOnly())
				return i;
		}
		
		return -1;
	}

	private int getOverrideLevelNum(){
		for(int i = 0; i <fLevels.length; i++){
			if(fLevels[i].isOverrideSupported())
				return i;
		}
		
		return -1;
	}

	@SuppressWarnings("unchecked")
	public void applyEntries(ICLanguageSettingEntry[] entries){
		HashMap<EntryNameKey, Object[]> map = getEntryLevelMap(WRITABLE | READ_ONLY);
		Map<EntryNameKey, Object[]> mapCopy = (HashMap<EntryNameKey, Object[]>)map.clone();
		Map<EntryNameKey, EntryInfo>[] clearedInfos = new Map [fLevels.length];
		
		for(int i = 0; i < fLevels.length; i++){
			if(!fLevels[i].isReadOnly())
				clearedInfos[i] = fLevels[i].clearAndGetMap();
		}

		Integer levelInteger;
		int levelNum;
		ICLanguageSettingEntry entry;
		int writableLevel = getWritableLevelNum();
		SettingLevel level;
		
		for(int i = 0; i < entries.length; i++){
			entry = entries[i];
			EntryNameKey key = new EntryNameKey(entry);
			Object[] o = map.get(key);

			
			if(o != null && valueMatches(entry, o[1])){
				mapCopy.remove(key);
				levelInteger = (Integer)o[0];
				if (! entry.isBuiltIn()) // allow overwrite existing entry,
					levelInteger = null; // even with the same value
			} else {
				levelInteger = null;
			}

			levelNum = levelInteger != null ? levelInteger.intValue() : writableLevel;
			if(levelNum >= 0){
				level = fLevels[levelNum];
				if(!level.isReadOnly()){
					Map<EntryNameKey, EntryInfo> clearedInfo = clearedInfos[levelNum];
					Object customInfo = null;
					if(clearedInfo != null){
						EntryInfo info = clearedInfo.get(key);
						if(info != null && entry.equalsByContents(info.getEntry()))
							customInfo = info.getCustomInfo();
					}
					level.addEntry(entry, customInfo);
				}
			}
		}
		
		int overrideLevel = getOverrideLevelNum();
		if(overrideLevel >= 0){
			level = fLevels[overrideLevel];
			if(level.isOverrideSupported() && !mapCopy.isEmpty()){
				for(EntryNameKey enk : mapCopy.keySet()){
					ICSettingEntry e = enk.getEntry();
					if ((e.getFlags() & ICSettingEntry.BUILTIN) == 0)
						continue;
					String str = e.getName();
					if(str != null) 
						level.addOverrideName(str);
				}
			}
		}
		adjustOverrideState();
	}
	
	public HashMap<EntryNameKey, Object[]> getEntryLevelMap(int types){
		HashMap<EntryNameKey, Object[]> map = new HashMap<EntryNameKey, Object[]>();
		for(int i = 0; i < fLevels.length; i++){
			if(isCompatible(fLevels[i], types))
				addLevelInfoToMap(fLevels[i], i, map);
		}
		return map;
	}
	
	private void addLevelInfoToMap(SettingLevel level, int l, Map<EntryNameKey, Object[]> map){
		for(EntryInfo info : level.getInfos()){
			EntryNameKey key = info.getContentsKey(); 
			if(!map.containsKey(key))
				map.put(key, new Object[]{new Integer(l), info.getEntry()});
		}
	}
	
	private static boolean valueMatches(ICLanguageSettingEntry e, Object o) {
		if (!(e instanceof ICMacroEntry))
			return true; // ignore values for other entries
		if (!(o instanceof ICMacroEntry))
			return false; // cannot compare different entries
		String s1 = e.getValue();
		String s2 = ((ICMacroEntry)o).getValue();
		if (s1 == null && s2 == null)
			return true;
		if (s1 != null)
			return s1.equals(s2);
		else	
			return s2.equals(s1);
	}

	

}
