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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ProfileInfoProvider.DiscoveredEntry;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.SettingsSet.SettingLevel;


public class EntryStorage {
	private int fKind;
	private SettingsSet fSettings;
//	private EntryListMap fDiscoveredEntries = new EntryListMap();
//	private EntryListMap fUserEntries = new EntryListMap();
//	private ICLanguageSettingEntry fEntries[];
	private BuildLanguageData fLangData;
	private boolean fCacheInited;
	private boolean fUserValuesInited;
	
	private static final String EMPTY_STRING = new String();

	public EntryStorage(int kind, BuildLanguageData lData){
		fKind = kind;
		fLangData = lData;
	}
	
	public int getKind(){
		return fKind;
	}
	
	void optionsChanged(){
		fUserValuesInited = false;
	}
	
	public List getEntries(List list){
		initCache();
		if(list == null)
			list = new ArrayList();
		
		ICLanguageSettingEntry entries[] = fSettings.getEntries();
		list.addAll(Arrays.asList(entries));
//		for(Iterator iter = fUserEntries.getIterator(); iter.hasNext();){
//			EntryInfo info = (EntryInfo)iter.next();
////			if(!info.isOverridden())
//				list.add(info.getEntry());
//		}
//		for(Iterator iter = fDiscoveredEntries.getIterator(); iter.hasNext();){
//			EntryInfo info = (EntryInfo)iter.next();
//			if(!info.isOverridden())
//				list.add(info.getEntry());
//		}
		return list;
	}
	
	private void resetDefaults(){
		resetCache();
		
		IOption options[] = fLangData.getOptionsForKind(fKind);
		ITool tool = fLangData.getTool();
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			if(option.getParent() == tool){
				tool.removeOption(option);
			}
		}
		
		options = fLangData.getUndefOptionsForKind(fKind);
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			if(option.getParent() == tool){
				tool.removeOption(option);
			}
		}
	}
	
	private void resetCache(){
		fCacheInited = false;
	}

	public void setEntries(ICLanguageSettingEntry entries[]){
		if(entries == null){
			resetDefaults();
			return;
		}
		initCache();
		
		fSettings.applyEntries(entries);
//		ArrayList userList = new ArrayList();
//		Map discoveredMap = fDiscoveredEntries.getEntryInfoMap();
//		boolean discoveredReadOnly = isDiscoveredEntriesReadOnly();
//		
//		for(int i = 0; i < entries.length; i++){
//			ICLanguageSettingEntry entry = entries[i];
//			EntryInfo info = (EntryInfo)discoveredMap.remove(new EntryNameKey(entry));
//			if(info == null || info.isOverridden() || !discoveredReadOnly){
//				if(info != null){
//					info.makeOverridden(true);
//				}
//				ICLanguageSettingEntry usrEntry = createEntry(entry, false);
//				userList.add(usrEntry);
//			}
//		}
		
//		for(Iterator iter = discoveredMap.values().iterator(); iter.hasNext();){
//			EntryInfo info = (EntryInfo)iter.next();
//			info.makeOverridden(false);
//		}
		
		SettingLevel level = fSettings.getLevels()[0];
		ICLanguageSettingEntry usrEntries[] = level.getEntries();
		if(usrEntries.length != 0){
			IOption options[] = fLangData.getOptionsForKind(fKind);
			if(options.length > 0){
				IOption option = options[0];
				String optValue[] = new String[usrEntries.length]; 
				for(int i = 0; i < usrEntries.length; i++){
					ICLanguageSettingEntry entry = usrEntries[i];
					optValue[i] = entryValueToOption(entry);
				}
				
				ITool tool = fLangData.getTool();
				IResourceInfo rcInfo = tool.getParentResourceInfo();
				IOption newOption = ManagedBuildManager.setOption(rcInfo, tool, option, optValue);
				options = fLangData.getOptionsForKind(fKind);
				for(int i = 0; i < options.length; i++){
					if(options[i] != newOption)
						ManagedBuildManager.setOption(rcInfo, tool, option, new String[0]);
				}
			}
		}
		
		if(level.containsOverrideInfo()){
			IOption options[] = fLangData.getUndefOptionsForKind(fKind);
			if(options.length != 0){
				Set set = level.getOverrideSet();
				IOption option = options[0];
				String[] optValue = (String[])set.toArray(new String[set.size()]);
				
				ITool tool = fLangData.getTool();
				IResourceInfo rcInfo = tool.getParentResourceInfo();
				IOption newOption = ManagedBuildManager.setOption(rcInfo, tool, option, optValue);
				options = fLangData.getUndefOptionsForKind(fKind);
				for(int i = 0; i < options.length; i++){
					if(options[i] != newOption)
						ManagedBuildManager.setOption(rcInfo, tool, option, new String[0]);
				}
			}
		}
	}
	
	private void initCache(){
//		if(fCacheInited){
//			if(!fUserValuesInited){
//				for(Iterator iter = fDiscoveredEntries.getIterator(); iter.hasNext();){
//					EntryInfo info = (EntryInfo)iter.next();
//					info.makeOverridden(false);
//				}
//				initUserValues();
//				fUserValuesInited = true;
//			}
//			
//		} else {
			fSettings = createEmptySettings();
			SettingLevel levels[] = fSettings.getLevels();
			fCacheInited = true;
			DiscoveredEntry[] dEntries = fLangData.getDiscoveredEntryValues(fKind);
			addEntries(levels[2], dEntries);
			
			dEntries = getDiscoveredEnvironmentEntries();
			addEntries(levels[1], dEntries);
			
			dEntries = getUserDiscoveredEntries();
			addEntries(levels[0], dEntries);
			levels[0].fOverrideSet = getUserUndefinedStringSet(); 
			
			fSettings.adjustOverrideState();
////			fDiscoveredEntries.clear();
//			boolean readOnly = isDiscoveredEntriesReadOnly();
//			if(dEntries.length != 0){
//				SettingLevel level = levels[2];
//				for(int i = 0; i < dEntries.length; i++){
//					DiscoveredEntry dEntry = dEntries[i];
//					ICLanguageSettingEntry entry = createEntry(dEntry, true, readOnly);
//					level.addEntry(entry);
////					EntryInfo info = new EntryInfo(entry, true, false);
////					fDiscoveredEntries.addEntryInfo(info);
//				}
//			}
//			initUserValues();
//			fUserValuesInited = true;
//		}
	}
	
	private void addEntries(SettingLevel level, DiscoveredEntry dEntries[]){
		if(dEntries.length != 0){
			for(int i = 0; i < dEntries.length; i++){
				DiscoveredEntry dEntry = dEntries[i];
				ICLanguageSettingEntry entry = createEntry(dEntry);
				level.addEntry(entry);
			}
		}
	}
	
	private DiscoveredEntry[] getDiscoveredEnvironmentEntries(){
		String paths[] = null;
		switch(fKind){
		case ICLanguageSettingEntry.INCLUDE_PATH:{
				IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
				paths = provider.getBuildPaths(fLangData.getConfiguration(), IEnvVarBuildPath.BUILDPATH_INCLUDE);
			}
			break;
		case ICLanguageSettingEntry.LIBRARY_PATH:{
				IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
				paths = provider.getBuildPaths(fLangData.getConfiguration(), IEnvVarBuildPath.BUILDPATH_LIBRARY);
			}
			break;
		}
		
		if(paths != null && paths.length != 0){
			DiscoveredEntry entries[] = new DiscoveredEntry[paths.length];
			for(int i = 0; i < paths.length; i++){
				entries[i] = new DiscoveredEntry(paths[i]);
			}
			
			return entries;
		}
		return new DiscoveredEntry[0];
	}
	
	private SettingsSet createEmptySettings(){
		SettingsSet settings = new SettingsSet(3);
		SettingLevel levels[] = settings.getLevels();
		
		boolean override = isDiscoveredEntriesOverridable(); 
		int readOnlyFlag = override ? 0 : ICSettingEntry.READONLY;
		levels[0].setFlagsToClear(ICSettingEntry.READONLY | ICSettingEntry.BUILTIN);
		levels[0].setFlagsToSet(0);
		levels[0].setReadOnly(false);
		levels[0].setOverrideSupported(override);

		levels[1].setFlagsToClear(ICSettingEntry.BUILTIN);
		levels[1].setFlagsToSet(readOnlyFlag | ICSettingEntry.RESOLVED);
		levels[1].setReadOnly(true);
		levels[1].setOverrideSupported(false);

		levels[2].setFlagsToClear(0);
		levels[2].setFlagsToSet(readOnlyFlag | ICSettingEntry.BUILTIN | ICSettingEntry.RESOLVED);
		levels[2].setReadOnly(true);
		levels[2].setOverrideSupported(false);

		return settings;
	}
	
	private boolean isDiscoveredEntriesOverridable(){
//		if(!needUndef())
//			return false;
		
		return fLangData.getUndefOptionsForKind(fKind).length != 0;
	}
	
//	private boolean needUndef(){
//		return fKind != ICLanguageSettingEntry.MACRO;
//	}
	
//	private void initUserValues(){
//		IOption options[] = fLangData.getOptionsForKind(fKind);
//		fUserEntries.clear();
//		if(options.length > 0){
//			for(int i = 0; i < options.length; i++){
//				IOption option = options[i];
//				List list = (List)option.getValue();
//				int size = list.size();
//				if(size > 0){
//					for(int j = 0; j < size; j++){
//						String value = (String)list.get(j);
//						if(value.indexOf('"') == 0 && value.lastIndexOf('"') == value.length() - 1 && value.length() != 1){
//							value = value.substring(1, value.length() - 1);
//						}
//						ICLanguageSettingEntry entry = createEntry(discoveredEntryFromString(value), false, false);
//						EntryInfo discoveredInfo = fDiscoveredEntries.getEntryInfo(entry);
//						if(discoveredInfo != null){
////							discoveredInfo.setOptionInfo(option, j);
//							discoveredInfo.makeOverridden(true);
//						}
//						EntryInfo userInfo = new EntryInfo(entry, false, true);
//						fUserEntries.addEntryInfo(userInfo);
//					}
//				}
//				
//			}
//		}
//	}
	
	private HashSet getUserUndefinedStringSet(){
		HashSet set = null;
		IOption options[] = fLangData.getUndefOptionsForKind(fKind);
		if(options.length > 0){
			for(int i = 0; i < options.length; i++){
				IOption option = options[i];
				List list = (List)option.getValue();
				if(list.size() != 0){
					if(set == null)
						set = new HashSet();
					set.addAll(list);
				}
			}
		}
		return set;
	}
	private DiscoveredEntry[] getUserDiscoveredEntries(){
		IOption options[] = fLangData.getOptionsForKind(fKind);
		if(options.length > 0){
			List entryList = new ArrayList();
			for(int i = 0; i < options.length; i++){
				IOption option = options[i];
				List list = (List)option.getValue();
				int size = list.size();
				if(size > 0){
					for(int j = 0; j < size; j++){
						String value = (String)list.get(j);
						if(value.indexOf('"') == 0 && value.lastIndexOf('"') == value.length() - 1 && value.length() != 1){
							value = value.substring(1, value.length() - 1);
						}
						entryList.add(discoveredEntryFromString(value));
					}
				}
			}
			
			return (DiscoveredEntry[])entryList.toArray(new DiscoveredEntry[entryList.size()]);
		}
		return new DiscoveredEntry[0];
	}
	
	private DiscoveredEntry discoveredEntryFromString(String str){
		if(fKind == ICLanguageSettingEntry.MACRO){
			String nv[] = macroNameValueFromValue(str);
			return new DiscoveredEntry(nv[0], nv[1]);
		}
		return new DiscoveredEntry(str);
	}
	
/*	private List processValues(List valuesList, boolean discovered, List entriesList){
		for(Iterator iter = valuesList.iterator(); iter.hasNext();){
			String value = (String)iter.next();
			ICLanguageSettingEntry entry = createEntry(value, discovered);
			if(entry != null)
				entriesList.add(entry);
		}
		return entriesList;
	}
*/	
	private ICLanguageSettingEntry createEntry(DiscoveredEntry dEntry/*, boolean discovered, boolean readOnly*/){
		ICLanguageSettingEntry entry = null;
		int flags = 0;//discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0;
		Object v[];
		String value = dEntry.getValue(); 
		String name = dEntry.getName(); 
		switch (fKind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			v = optionPathValueToEntry(value);
			value = (String)v[0];
			if(((Boolean)v[1]).booleanValue())
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			entry = new CIncludePathEntry(value, flags);
			break;
		case ICLanguageSettingEntry.MACRO:
			//String nv[] = macroNameValueFromValue(value);
			
			entry = new CMacroEntry(name, value, flags);
			break;
		case ICLanguageSettingEntry.INCLUDE_FILE:
			v = optionPathValueToEntry(value);
			value = (String)v[0];
			if(((Boolean)v[1]).booleanValue())
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			entry = new CIncludeFileEntry(value, flags);
			break;
		case ICLanguageSettingEntry.MACRO_FILE:
			v = optionPathValueToEntry(value);
			value = (String)v[0];
			if(((Boolean)v[1]).booleanValue())
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			entry = new CMacroFileEntry(value, flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			v = optionPathValueToEntry(value);
			value = (String)v[0];
			if(((Boolean)v[1]).booleanValue())
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			entry = new CLibraryPathEntry(value, flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			v = optionPathValueToEntry(value);
			value = (String)v[0];
			if(((Boolean)v[1]).booleanValue())
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			entry = new CLibraryFileEntry(value, flags);
			break;
		}
		return entry;
		
	}

	private ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, boolean discovered){
		//ICLanguageSettingEntry entry = null;
		int flags = entry.getFlags();
		if(discovered)
			flags |= ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY;
		
		switch (fKind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.MACRO:
			entry = new CMacroEntry(entry.getName(), entry.getValue(), flags);
			break;
		case ICLanguageSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			entry = new CLibraryFileEntry(entry.getName(), flags);
			break;
		}
		return entry;
		
	}

	public static String[] macroNameValueFromValue(String value){
		String nv[] = new String[2];
		int index = value.indexOf('=');
		if(index > 0){
			nv[0] = value.substring(0, index);
			nv[1] = value.substring(index + 1);
		} else {
			nv[0] = value;
			nv[1] = EMPTY_STRING;
		}
		return nv;
	}
	
	private String nameFromValue(String value){
		if(fKind != ICLanguageSettingEntry.MACRO){
			return value;
		}
		return macroNameValueFromValue(value)[0];
	}
	
	private String entryValueToOption(ICLanguageSettingEntry entry){
		if(entry.getKind() == ICLanguageSettingEntry.MACRO && entry.getValue().length() > 0){
			return new StringBuffer(entry.getName()).append('=').append(entry.getValue()).toString();
		} else if(entry instanceof ICLanguageSettingPathEntry){
			ICLanguageSettingPathEntry pathEntry = (ICLanguageSettingPathEntry)entry;
			if(pathEntry.isValueWorkspacePath()){
				return ManagedBuildManager.fullPathToLocation(pathEntry.getValue());
			}
		}
		return entry.getName();
	}
	
	private Object[] optionPathValueToEntry(String value){
		String wspPath = ManagedBuildManager.locationToFullPath(value);
		if(wspPath != null)
			return new Object[]{wspPath, Boolean.valueOf(true)};
		return new Object[]{value, Boolean.valueOf(false)};
	}
	
}
