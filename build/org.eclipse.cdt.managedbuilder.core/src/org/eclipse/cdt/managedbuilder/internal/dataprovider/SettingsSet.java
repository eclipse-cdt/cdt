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
		private EntryListMap fEntries;
		
		private SettingLevel(){
			fEntries = new EntryListMap();
		}
		
		public boolean isReadOnly(){
			return fIsReadOnly;
		}
		
		public void setReadOnly(boolean readOnly){
			fIsReadOnly = readOnly;
		}
		
		public void setFlagsToSet(int flags){
			fFlagsToSet = flags;
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
		
		public void addEntry(ICLanguageSettingEntry entry){
			entry = CDataUtil.createEntry(entry, fFlagsToSet, fFlagsToClear);
			fEntries.addEntryInfo(new EntryInfo(entry));
		}
		
		public void clear(){
			fEntries.clear();
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
		Set set = new HashSet();
		for(int i = 0; i < fLevels.length; i++){
			adjustOverrideState(fLevels[i], set);
		}
	}
	
	private void adjustOverrideState(SettingLevel level, Set overridenSet){
		EntryInfo[] infos = level.getInfos();
		EntryInfo info;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			if(overridenSet.add(info.getNameKey())){
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
		Map map = getEntryLevelMap(WRITABLE | READ_ONLY);
		
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
			levelInteger = (Integer)map.get(key);
			levelNum = levelInteger != null ? levelInteger.intValue() : defaultLevel;
			level = fLevels[levelNum];
			if(!level.isReadOnly())
				level.addEntry(entry);
		}
		
		adjustOverrideState();
	}
	
	public Map getEntryLevelMap(int types){
		Map map = new HashMap();
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
				map.put(key, new Integer(l));
		}
	}
}
