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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryNameKey;

public class SettingsSet {
	public static final int READ_ONLY = 1;
	public static final int WRITABLE = 1 << 1;

	private SettingLevel[] fLevels;
	public class SettingLevel {
		private int fFlagsToSet;
		private int fFlagsToClear;
		private boolean fIsReadOnly;
		private boolean fIsOverrideSupported;
		private EntryListMap fEntries;
		HashSet fOverrideSet;
		
		private SettingLevel(){
			fEntries = new EntryListMap();
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
		
		public Set getOverrideSet(){
			if(fOverrideSet != null)
				return (HashSet)fOverrideSet.clone();
			return new HashSet();
		}
		
		public void addEntry(ICLanguageSettingEntry entry){
			entry = CDataUtil.createEntry(entry, fFlagsToSet, fFlagsToClear);
			fEntries.addEntryInfo(new EntryInfo(entry));
		}
		
		public void addOverrideName(String name){
			if(fOverrideSet == null)
				fOverrideSet = new HashSet();
			
			fOverrideSet.add(name);
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
		
		EntryInfo[] getInfos(){
			return fEntries.getEntries();
		}
		
		public ICLanguageSettingEntry[] getEntries(){
			List list = new ArrayList();
			EntryInfo infos[] = getInfos();
			for(int i = 0; i < infos.length; i++){
				if(!infos[i].isOverridden())
					list.add(infos[i].getEntry());
			}
			
			return (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
		}
	}
	
	public SettingsSet(int num){
		fLevels = new SettingLevel[num];
		for(int i = 0; i < num; i++){
			fLevels[i] = new SettingLevel();
		}
	}
	
	public SettingLevel[] getLevels(){
		return (SettingLevel[])fLevels.clone();
	}
	
	public void adjustOverrideState(){
		int dNum = getDefaultLevelNum();
		SettingLevel dLevel = fLevels[dNum];
		
		
		Set set = dLevel.isOverrideSupported() ? dLevel.getOverrideSet() : new HashSet();
		for(int i = 0; i < fLevels.length; i++){
			adjustOverrideState(fLevels[i], set);
		}
	}
	
	private void adjustOverrideState(SettingLevel level, Set overridenSet){
		EntryInfo[] infos = level.getInfos();
		EntryInfo info;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
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
		List entries = new ArrayList();
		for(int i = 0; i < fLevels.length; i++){
			if(isCompatible(fLevels[i], types))
				getEntries(fLevels[i], entries);
		}

		return (ICLanguageSettingEntry[])entries.toArray(new ICLanguageSettingEntry[entries.size()]);
	}
	
	private void getEntries(SettingLevel level, List list){
		EntryInfo[] infos = level.getInfos();
		EntryInfo info;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			if(!info.isOverridden())
				list.add(info.getEntry());
		}
	}
	
	private boolean isCompatible(SettingLevel level, int types){
		if((types & READ_ONLY) == 0 && level.isReadOnly())
			return false;
		if((types & WRITABLE) == 0 && !level.isReadOnly())
			return false;
		
		return true;
	}
	
	private int getDefaultLevelNum(){
		for(int i = 0; i <fLevels.length; i++){
			if(!fLevels[i].isReadOnly())
				return i;
		}
		
		return 0;
	}

	public void applyEntries(ICLanguageSettingEntry[] entries){
		HashMap map = getEntryLevelMap(WRITABLE | READ_ONLY);
		Map mapCopy = (HashMap)map.clone();
		
		for(int i = 0; i < fLevels.length; i++){
			if(!fLevels[i].isReadOnly()){
				fLevels[i].clear();
			}
		}

		Integer levelInteger;
		int levelNum;
		EntryNameKey key;
		ICLanguageSettingEntry entry;
		int defaultLevel = getDefaultLevelNum();
		SettingLevel level;
		
		for(int i = 0; i < entries.length; i++){
			entry = entries[i];
			key = new EntryNameKey(entry);
			Object[] o = (Object[])map.get(key);

			if(o != null){
				mapCopy.remove(key);
				levelInteger = (Integer)o[0]; 
			} else {
				levelInteger = null;
			}

			levelNum = levelInteger != null ? levelInteger.intValue() : defaultLevel;
			level = fLevels[levelNum];
			if(!level.isReadOnly())
				level.addEntry(entry);
		}
		
		level = fLevels[defaultLevel];
		if(level.isOverrideSupported() && !mapCopy.isEmpty()){
			String str;
			for(Iterator iter = mapCopy.keySet().iterator(); iter.hasNext();){
				str = ((EntryNameKey)iter.next()).getEntry().getName();
				if(str != null)
					level.addOverrideName(str);
			}
		}
		adjustOverrideState();
	}
	
	public HashMap getEntryLevelMap(int types){
		HashMap map = new HashMap();
		for(int i = 0; i < fLevels.length; i++){
			if(isCompatible(fLevels[i], types))
				addLevelInfoToMap(fLevels[i], i, map);
		}

		return map;
		
	}
	
	private void addLevelInfoToMap(SettingLevel level, int l, Map map){
		EntryInfo infos[] = level.getInfos();
		EntryInfo info;
		EntryNameKey key;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			key = info.getNameKey(); 
			if(!map.containsKey(key))
				map.put(key, new Object[]{new Integer(l), info.getEntry()});
		}
	}
}
