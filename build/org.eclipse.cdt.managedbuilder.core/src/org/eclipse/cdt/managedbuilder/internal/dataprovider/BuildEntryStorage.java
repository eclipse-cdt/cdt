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
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
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
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.core.Option;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildEntryStorage extends AbstractEntryStorage {
	private BuildLanguageData fLangData;

	private static class UserEntryInfo {
		private ICLanguageSettingEntry fEntry;
		private OptionStringValue fOptionValue;
		
		UserEntryInfo(ICLanguageSettingEntry entry, OptionStringValue optionValue){
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
					userInfos[i] = new UserEntryInfo(infos[i].getEntry(), (OptionStringValue)infos[i].getCustomInfo());
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
				Option option = (Option)options[i];
				List list = (List)option.getExactValue();
				int size = list.size();
				if(size > 0){
					for(int j = 0; j < size; j++){
						OptionStringValue ve = (OptionStringValue)list.get(j);
//						if(value.indexOf('"') == 0 && value.lastIndexOf('"') == value.length() - 1 && value.length() != 1){
//							value = value.substring(1, value.length() - 1);
//						}
						ICLanguageSettingEntry entry = createUserEntry(option, ve, flags);  
						entryList.add(new UserEntryInfo(entry, ve));
					}
				}
			}
			
			return (UserEntryInfo[])entryList.toArray(new UserEntryInfo[entryList.size()]);
		}
		return new UserEntryInfo[0];
	}
	
	private static String stripQuotes(String value){
		if(value.indexOf('"') == 0 && value.lastIndexOf('"') == value.length() - 1 && value.length() != 1){
			value = value.substring(1, value.length() - 1);
		}
		return value;
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
	
	private ICLanguageSettingEntry createUserEntry(Option option, OptionStringValue optionValue, int flags){
//	private ICLanguageSettingEntry createUserEntry(Option option, String optionValue, int flags){
		int kind = getKind();
		
		ICLanguageSettingEntry entry = null;
		
		IPath srcPath = null, srcRootPath = null, srcPrefixMapping = null;
		
		switch (kind){
		case ICLanguageSettingEntry.MACRO:
			String nv[] = macroNameValueFromValue(optionValue.getValue());
//			String nv[] = macroNameValueFromValue(optionValue);
			
			entry = new CMacroEntry(nv[0], nv[1], flags);
			break;
//		case ICLanguageSettingEntry.INCLUDE_PATH:
//		case ICLanguageSettingEntry.INCLUDE_FILE:
//		case ICLanguageSettingEntry.MACRO_FILE:
//		case ICLanguageSettingEntry.LIBRARY_PATH:
//		case ICLanguageSettingEntry.LIBRARY_FILE:
		case ICLanguageSettingEntry.LIBRARY_FILE:
			String tmp = optionValue.getSourceAttachmentPath();
			if(tmp != null)
				srcPath = new Path(tmp);
			tmp = optionValue.getSourceAttachmentRootPath();
			if(tmp != null)
				srcRootPath = new Path(tmp);
			tmp = optionValue.getSourceAttachmentPrefixMapping();
			if(tmp != null)
				srcPrefixMapping = new Path(tmp);
			//do not break
		default:
			IOptionPathConverter optionPathConverter = fLangData.getTool().getOptionPathConverter();
			Object[] v = optionPathValueToEntry(stripQuotes(optionValue.getValue()));
//			Object[] v = optionPathValueToEntry(optionValue);
			String name = (String)v[0];
			if(((Boolean)v[1]).booleanValue()){
				flags |= ICLanguageSettingEntry.VALUE_WORKSPACE_PATH;
			} else if (optionPathConverter != null){
				IPath path = optionPathConverter.convertToPlatformLocation(name, option, fLangData.getTool());
				if(path != null)
					name = path.toString();
			}
			entry = (ICLanguageSettingEntry)CDataUtil.createEntry(kind, name, null, null, flags, srcPath, srcRootPath, srcPrefixMapping);
			break;

		}
		return entry;
	}
	
	private OptionStringValue createOptionValue(IOption option, UserEntryInfo info){
		if(info.fOptionValue != null)
			return info.fOptionValue;
		
		return entryValueToOption(option, info.fEntry);
	}

	private OptionStringValue entryValueToOption(IOption option, ICLanguageSettingEntry entry){
		String optionValue = entryValueToOptionStringValue(option, entry);
		if(entry.getKind() == ICSettingEntry.LIBRARY_FILE){
			ICLibraryFileEntry libFile = (ICLibraryFileEntry)entry;
			return new OptionStringValue(optionValue, 
					false,
					pathToString(libFile.getSourceAttachmentPath()),
					pathToString(libFile.getSourceAttachmentRootPath()),
					pathToString(libFile.getSourceAttachmentPrefixMapping()));
		}
		return new OptionStringValue(optionValue);
	}
	
	private static String pathToString(IPath path){
		return path != null ? path.toString() : null;
	}

	private String entryValueToOptionStringValue(IOption option, ICLanguageSettingEntry entry){
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
			nv[1] = ""; //$NON-NLS-1$
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
			OptionStringValue optValue[] = new OptionStringValue[entries.length]; 
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
