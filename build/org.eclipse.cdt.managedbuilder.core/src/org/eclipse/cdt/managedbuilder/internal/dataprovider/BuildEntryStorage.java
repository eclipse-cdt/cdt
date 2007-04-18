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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.AbstractEntryStorage;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.SettingsSet;
import org.eclipse.cdt.core.settings.model.util.SettingsSet.EntryInfo;
import org.eclipse.cdt.core.settings.model.util.SettingsSet.SettingLevel;
import org.eclipse.cdt.managedbuilder.core.IEnvVarBuildPath;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.IReverseOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.IPath;

public class BuildEntryStorage extends AbstractEntryStorage {
	private BuildLanguageData fLangData;

	private static class UserEntryInfo {
		private ICLanguageSettingEntry fEntry;
		private String fOptionValue;
		
		UserEntryInfo(ICLanguageSettingEntry entry, String optionValue){
			fEntry = entry;
			fOptionValue = optionValue;
		}
	}
	public BuildEntryStorage(int kind, BuildLanguageData lData) {
		super(kind);
		fLangData = lData;
	}

	protected SettingsSet createEmptySettings() {
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
		return fLangData.getUndefOptionsForKind(getKind()).length != 0;
	}

	protected void obtainEntriesFromLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case 0:
			if(level == null)
				restoreDefaults();
			else {
				EntryInfo infos[] = level.getInfos();
				UserEntryInfo[] userInfos = new UserEntryInfo[infos.length];
				for(int i = 0; i < infos.length; i++){
					userInfos[i] = new UserEntryInfo(infos[i].getEntry(), (String)infos[i].getCustomInfo());
				}
				setUserEntries(userInfos);
				setUserUndefinedStringSet(level.containsOverrideInfo() ? level.getOverrideSet() : null);
			}
			break;
		}
	}
	
	private void restoreDefaults(){
		IOption options[] = fLangData.getOptionsForKind(getKind());
		ITool tool = fLangData.getTool();
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			if(option.getParent() == tool){
				tool.removeOption(option);
			}
		}
		
		options = fLangData.getUndefOptionsForKind(getKind());
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			if(option.getParent() == tool){
				tool.removeOption(option);
			}
		}
	}

	protected void putEntriesToLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case 0:
			UserEntryInfo[] userEntries = getUserEntries(level.getFlags(0));
			for(int i = 0; i < userEntries.length; i++){
				level.addEntry(userEntries[i].fEntry, userEntries[i].fOptionValue);
			}
			level.addOverrideNameSet(getUserUndefinedStringSet());
			break;
		case 1:
			ICLanguageSettingEntry[] envEntries = getEnvEntries(level.getFlags(0));
			level.addEntries(envEntries);
			break;
		case 2:
			ICLanguageSettingEntry[] discoveredEntries = getDiscoveredEntries(level.getFlags(0));
			level.addEntries(discoveredEntries);
			break;
		}
	}
	
	private ICLanguageSettingEntry[] getDiscoveredEntries(int flags){
		return ProfileInfoProvider.getInstance().getEntryValues(fLangData, getKind(), flags);
	}
	
	private UserEntryInfo[] getUserEntries(int flags){
		IOption options[] = fLangData.getOptionsForKind(getKind());
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
						ICLanguageSettingEntry entry = createUserEntry(option, value, flags);  
						entryList.add(new UserEntryInfo(entry, value));
					}
				}
			}
			
			return (UserEntryInfo[])entryList.toArray(new UserEntryInfo[entryList.size()]);
		}
		return new UserEntryInfo[0];
	}
	
	private HashSet getUserUndefinedStringSet(){
		HashSet set = null;
		IOption options[] = fLangData.getUndefOptionsForKind(getKind());
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

	private ICLanguageSettingEntry[] getEnvEntries(int flags){
		String paths[] = null;
		int kind = getKind();
		switch(kind){
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
			ICLanguageSettingEntry entries[] = new ICLanguageSettingEntry[paths.length];
			for(int i = 0; i < paths.length; i++){
				entries[i] = (ICLanguageSettingEntry)CDataUtil.createEntry(kind, paths.toString(), null, null, flags);
			}
			
			return entries;
		}
		return new ICLanguageSettingEntry[0];
	}
	
	private ICLanguageSettingEntry createUserEntry(IOption option, String optionValue, int flags){
		int kind = getKind();
		
		ICLanguageSettingEntry entry = null;
		
		switch (kind){
		case ICLanguageSettingEntry.MACRO:
			String nv[] = macroNameValueFromValue(optionValue);
			
			entry = new CMacroEntry(nv[0], nv[1], flags);
			break;
//		case ICLanguageSettingEntry.INCLUDE_PATH:
//		case ICLanguageSettingEntry.INCLUDE_FILE:
//		case ICLanguageSettingEntry.MACRO_FILE:
//		case ICLanguageSettingEntry.LIBRARY_PATH:
//		case ICLanguageSettingEntry.LIBRARY_FILE:
		default:
			IOptionPathConverter optionPathConverter = fLangData.getTool().getOptionPathConverter();
			Object[] v = optionPathValueToEntry(optionValue);
			String name = (String)v[0];
			if(((Boolean)v[1]).booleanValue()){
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			} else if (optionPathConverter != null){
				IPath path = optionPathConverter.convertToPlatformLocation(name, option, fLangData.getTool());
				if(path != null)
					name = path.toString();
			}
			entry = (ICLanguageSettingEntry)CDataUtil.createEntry(kind, name, null, null, flags);
			break;

		}
		return entry;
	}
	
	private String createOptionValue(IOption option, UserEntryInfo info){
		if(info.fOptionValue != null)
			return info.fOptionValue;
		
		return entryValueToOption(option, info.fEntry);
	}
	
	private String entryValueToOption(IOption option, ICLanguageSettingEntry entry){
		if(entry.getKind() == ICLanguageSettingEntry.MACRO && entry.getValue().length() > 0){
			return new StringBuffer(entry.getName()).append('=').append(entry.getValue()).toString();
		} else if(entry instanceof ICLanguageSettingPathEntry){
			IOptionPathConverter converter = fLangData.getTool().getOptionPathConverter();
			if(converter instanceof IReverseOptionPathConverter){
				return ((IReverseOptionPathConverter)converter).convertToOptionValue(entry, option, fLangData.getTool());
			}
			ICLanguageSettingPathEntry pathEntry = (ICLanguageSettingPathEntry)entry;
			if(pathEntry.isValueWorkspacePath()){
				return ManagedBuildManager.fullPathToLocation(pathEntry.getValue());
			}
		}
		return entry.getName();
	}

	
	public static String[] macroNameValueFromValue(String value){
		String nv[] = new String[2];
		int index = value.indexOf('=');
		if(index > 0){
			nv[0] = value.substring(0, index);
			nv[1] = value.substring(index + 1);
		} else {
			nv[0] = value;
			nv[1] = "";
		}
		return nv;
	}
	
	private static Object[] optionPathValueToEntry(String value){
		String wspPath = ManagedBuildManager.locationToFullPath(value);
		if(wspPath != null)
			return new Object[]{wspPath, Boolean.valueOf(true)};
		return new Object[]{value, Boolean.valueOf(false)};
	}
	
	private void setUserEntries(UserEntryInfo[] entries){
		int kind = getKind();
		IOption options[] = fLangData.getOptionsForKind(kind);
		if(options.length != 0){
			IOption option = options[0];
			String optValue[] = new String[entries.length]; 
			if(entries.length != 0){
				for(int i = 0; i < entries.length; i++){
					optValue[i] = createOptionValue(option, entries[i]);
				}
			}

			ITool tool = fLangData.getTool();
			IResourceInfo rcInfo = tool.getParentResourceInfo();
			IOption newOption = ManagedBuildManager.setOption(rcInfo, tool, option, optValue);
			options = fLangData.getOptionsForKind(kind);
			for(int i = 0; i < options.length; i++){
				if(options[i] != newOption)
					ManagedBuildManager.setOption(rcInfo, tool, option, new String[0]);
			}
		}
	}
	
	private void setUserUndefinedStringSet(Set set){
		int kind = getKind();
		IOption[] options = fLangData.getUndefOptionsForKind(kind);
		if(options.length != 0){
			if(set != null && set.size() == 0)
				set = null;
			
			String[] optValue = set != null ? (String[])set.toArray(new String[set.size()]) : new String[0];
			IOption option = options[0];
			ITool tool = fLangData.getTool();
			IResourceInfo rcInfo = tool.getParentResourceInfo();
			IOption newOption = ManagedBuildManager.setOption(rcInfo, tool, option, optValue);
			options = fLangData.getUndefOptionsForKind(kind);
			for(int i = 0; i < options.length; i++){
				if(options[i] != newOption)
					ManagedBuildManager.setOption(rcInfo, tool, option, new String[0]);
			}

		}

	}
	
	void optionsChanged(){
	}
}
