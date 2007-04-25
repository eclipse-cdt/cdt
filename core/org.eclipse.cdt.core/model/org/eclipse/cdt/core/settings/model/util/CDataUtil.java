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
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFacroty;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class CDataUtil {
	private static Random randomNumber;
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static int genRandomNumber(){
		if (randomNumber == null) {
			// Set the random number seed
			randomNumber = new Random();
			randomNumber.setSeed(System.currentTimeMillis());
		}
		int i = randomNumber.nextInt();
		if (i < 0) {
			i *= -1;
		}
		return i;
	}
	
	public static String genId(String baseId){
		String suffix = new Integer(genRandomNumber()).toString();
		return baseId != null ? 
				new StringBuffer(baseId).append(".").append(suffix).toString() 	//$NON-NLS-1$
				: suffix;
	}
	
	public static boolean objectsEqual(Object o1, Object o2){
		if(o1 == null)
			return o2 == null;
		return o1.equals(o2);
	}

	public static String arrayToString(String[] array, String separator){
		return arrayToString((Object[])array, separator);
	}

	public static String arrayToString(Object[] array, String separator){
		if(array == null)
			return null;
		if(array.length == 0)
			return ""; //$NON-NLS-1$
		if(array.length == 1)
			return array[0].toString();
		StringBuffer buf = new StringBuffer();
		buf.append(array[0]);
		for(int i = 1; i < array.length; i++){
			buf.append(separator).append(array[i]);
		}
		
		return buf.toString();
	}

	public static String[] stringToArray(String string, String separator){
		if(string == null)
			return null;
		if(string.length() == 0)
			return EMPTY_STRING_ARRAY;
		StringTokenizer t = new StringTokenizer(string, separator);
		List list = new ArrayList(t.countTokens());
		while (t.hasMoreElements()) {
			list.add(t.nextToken());
		}
		return (String[])list.toArray(new String[list.size()]);
	}

	public static ICSettingEntry[] resolveEntries(ICSettingEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;
		
		ICSettingEntry[] resolved = new ICSettingEntry[entries.length];
		ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();

		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			resolved[i] = createResolvedEntry(entry, cfgDes, mngr);
		}
		
		return resolved;
	}

	public static ICLanguageSettingEntry[] resolveEntries(ICLanguageSettingEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;

		ICSettingEntry[] resolved = resolveEntries((ICSettingEntry[])entries, cfgDes);
		ICLanguageSettingEntry[] resolvedLangEntries = new ICLanguageSettingEntry[resolved.length];
		System.arraycopy(resolved, 0, resolvedLangEntries, 0, resolved.length);
		return resolvedLangEntries;
	}

	public static ICSourceEntry[] resolveEntries(ICSourceEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;

		ICSettingEntry[] resolved = resolveEntries((ICSettingEntry[])entries, cfgDes);
		ICSourceEntry[] resolvedLangEntries = new ICSourceEntry[resolved.length];
		System.arraycopy(resolved, 0, resolvedLangEntries, 0, resolved.length);
		return resolvedLangEntries;
	}

	public static ICOutputEntry[] resolveEntries(ICOutputEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;

		ICSettingEntry[] resolved = resolveEntries((ICSettingEntry[])entries, cfgDes);
		ICOutputEntry[] resolvedLangEntries = new ICOutputEntry[resolved.length];
		System.arraycopy(resolved, 0, resolvedLangEntries, 0, resolved.length);
		return resolvedLangEntries;
	}

	private static ICSettingEntry createResolvedEntry(ICSettingEntry entry, ICConfigurationDescription cfg, ICdtVariableManager mngr){
		if(entry.isResolved())
			return entry;
		
		String name = entry.getName();
		try {
			name = mngr.resolveValue(name, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		
		String value = null;
		IPath[] exclusionFilters = null;
		
		switch (entry.getKind()) {
		case ICSettingEntry.MACRO:
			value = entry.getValue();
			try {
				value = mngr.resolveValue(value, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}
			break;
		case ICSettingEntry.SOURCE_PATH:
		case ICSettingEntry.OUTPUT_PATH:
			exclusionFilters = ((ICExclusionPatternPathEntry)entry).getExclusionPatterns();
			for(int i = 0; i < exclusionFilters.length; i++){
				String exclString = exclusionFilters[i].toString();
				try {
					exclString = mngr.resolveValue(exclString, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
				exclusionFilters[i] = new Path(exclString);
			}
			break;
//		default:
//			throw new IllegalArgumentException();
		}
		
		return createEntry(entry.getKind(), name, value, exclusionFilters, entry.getFlags() | ICSettingEntry.RESOLVED);
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flagsToAdd, int flafsToClear){
		return createEntry(entry, (entry.getFlags() | flagsToAdd) & (~flafsToClear));
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flags){
		switch (entry.getKind()){
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

	public static ICSettingEntry createEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags){
		switch (kind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return new CIncludePathEntry(name, flags);
		case ICLanguageSettingEntry.MACRO:
			return new CMacroEntry(name, value, flags);
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return new CIncludeFileEntry(name, flags);
		case ICLanguageSettingEntry.MACRO_FILE:
			return new CMacroFileEntry(name, flags);
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return new CLibraryPathEntry(name, flags);
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return new CLibraryFileEntry(name, flags);
		case ICLanguageSettingEntry.OUTPUT_PATH:
			return new COutputEntry(name, exclusionPatterns, flags);
		case ICLanguageSettingEntry.SOURCE_PATH:
			return new CSourceEntry(name, exclusionPatterns, flags);
		}
		throw new IllegalArgumentException();
	}

	public static String[] getSourceExtensions(IProject project, CLanguageData data) {
		String[] exts = null;
		String[] typeIds = data.getSourceContentTypeIds();
		if(typeIds != null && typeIds.length != 0){
			exts = getExtensionsFromContentTypes(project, typeIds);
		} else {
			exts = data.getSourceExtensions();
			if(exts != null && exts.length != 0)
				exts = (String[])exts.clone();
			else
				exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
		}
		
		if(exts == null)
			exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
		return exts;
	}

	public static String[] getExtensionsFromContentTypes(IProject project, String[] typeIds){
		String[] exts = null;
		if(typeIds != null && typeIds.length != 0){
			IContentTypeManager manager = Platform.getContentTypeManager();
			IContentType type;
			if(typeIds.length == 1){
				type = manager.getContentType(typeIds[0]);
				if(type != null)
					exts = getContentTypeFileSpecs(project, type);
			} else {
				List list = new ArrayList();
				for(int i = 0; i < typeIds.length; i++){
					type = manager.getContentType(typeIds[i]);
					if(type != null) {
						list.addAll(Arrays.asList(getContentTypeFileSpecs(project, type)));
					}
				}
				exts = (String[])list.toArray(new String[list.size()]);
			}
		}
		
		if(exts == null)
			exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
		return exts;
	}

	public static String[] getContentTypeFileSpecs (IProject project, IContentType type) {
		String[] globalSpecs = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC); 
		IContentTypeSettings settings = null;
		if (project != null) {
			IScopeContext projectScope = new ProjectScope(project);
			try {
				settings = type.getSettings(projectScope);
			} catch (Exception e) {}
			if (settings != null) {
				String[] specs = settings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				if (specs.length > 0) {
					int total = globalSpecs.length + specs.length;
					String[] projSpecs = new String[total];
					int i=0;
					for (int j=0; j<specs.length; j++) {
						projSpecs[i] = specs[j];
						i++;
					}								
					for (int j=0; j<globalSpecs.length; j++) {
						projSpecs[i] = globalSpecs[j];
						i++;
					}								
					return projSpecs;
				}
			}
		}
		return globalSpecs;		
	}

	public static CLanguageData findLanguagDataForFile(String fileName, IProject project, CFolderData fData){
		return findLanguagDataForFile(fileName, project, fData.getLanguageDatas());
	}

	public static CLanguageData findLanguagDataForFile(String fileName, IProject project, CLanguageData datas[]){
		//	if(cType != null){
		//		setting = findLanguageSettingForContentTypeId(cType.getId(), settings, true);
		//		if(setting == null)
		//			setting = findLanguageSettingForContentTypeId(cType.getId(), settings, false);
		//	}
			CLanguageData data = null;
			int index = fileName.lastIndexOf('.');
			if(index > 0){
				String ext = fileName.substring(index + 1).trim();
				if(ext.length() > 0){
					data = findLanguageDataForExtension(ext, datas);
				}
			}
			return data;
		}
	
	public static CLanguageData findLanguageDataForExtension(String ext, CLanguageData datas[]/*, boolean src*/){
		CLanguageData data;
		for(int i = 0; i < datas.length; i++){
			data = datas[i]; 
			String exts[] = data.getSourceExtensions();
/*			if(src){
				if(setting.getSourceContentType() == null){
					exts = setting.getSourceExtensions();
				}
			} else {
				if(setting.getHeaderContentType() == null){
					exts = setting.getHeaderExtensions();
				}
			}
*/			
			if(exts != null && exts.length != 0){
				for(int j = 0; j < exts.length; j++){
					if(ext.equals(exts[j]))
						return data;
				}
			}
		}
		return null;
	}

	public static Map createPathRcDataMap(CConfigurationData data){
		Map map = new HashMap();
		CResourceData[] rcDatas = data.getResourceDatas();
		CResourceData rcData;
		for(int i = 0; i < rcDatas.length; i++){
			rcData = rcDatas[i];
			map.put(rcData.getPath(), rcData);
		}
		return map;
	}

	public static PathSettingsContainer createRcDataHolder(CConfigurationData data){
		PathSettingsContainer h = PathSettingsContainer.createRootContainer();
		
		h.setValue(data.getRootFolderData());
		CResourceData[] rcDatas = data.getResourceDatas();
		CResourceData rcData;
		PathSettingsContainer child;
		for(int i = 0; i < rcDatas.length; i++){
			rcData = rcDatas[i];
			child = h.getChildContainer(rcData.getPath(), true, true);
			child.setValue(rcData);
		}
		return h;
	}
	
	public static CConfigurationData createEmptyData(String id, String name, CDataFacroty factory, boolean performLangAdjustment){
		if(id == null)
			id = genId(null);
		
		CConfigurationData data = factory.createConfigurationdata(id, name, null, false);
		if(data.getRootFolderData() == null){
			CFolderData foData = factory.createFolderData(data, null, genId(data.getId()), false, Path.EMPTY);
			factory.link(data, foData);
		}
		
		if(data.getBuildData() == null){
			CBuildData bData = factory.createBuildData(data, null, genId(data.getId()), null, false);
			factory.link(data, bData);
		}
		
		if(data.getTargetPlatformData() == null){
			CTargetPlatformData tpData = factory.createTargetPlatformData(data, null, genId(data.getId()), null, false);
			factory.link(data, tpData);
		}

		if(performLangAdjustment)
			adjustConfig(data, factory);
		
		return data;
	}

	public static CConfigurationData adjustConfig(CConfigurationData cfg, CDataFacroty factory){
		LanguageManager mngr = LanguageManager.getInstance();
		ILanguageDescriptor dess[] = mngr.getLanguageDescriptors();
		Map map = mngr.getContentTypeIdToLanguageDescriptionsMap();
		
		CResourceData[] rcDatas = cfg.getResourceDatas();
		for(int i = 0; i < rcDatas.length; i++){
			if(rcDatas[i].getType() == ICSettingBase.SETTING_FOLDER){
				adjustFolderData(cfg, (CFolderData)rcDatas[i], factory, dess, new HashMap(map));
			}
		}
		
		return cfg;
	}

	
	private static void adjustFolderData(CConfigurationData cfgData, CFolderData data, CDataFacroty factory, ILanguageDescriptor dess[], HashMap map){
		Map langMap = new HashMap();
		for(int i = 0; i < dess.length; i++){
			langMap.put(dess[i].getId(), dess[i]);
		}
		CLanguageData lDatas[] = data.getLanguageDatas();
		for(int i = 0; i < lDatas.length; i++){
			CLanguageData lData = (CLanguageData)lDatas[i];
			String langId = lData.getLanguageId();
			if(langId != null){
				ILanguageDescriptor des = (ILanguageDescriptor)langMap.remove(langId);
				adjustLanguageData(data, lData, des);
						continue;
			} else {
				String[] cTypeIds = lData.getSourceContentTypeIds();
				for(int c = 0; c < cTypeIds.length; c++){
					String cTypeId = cTypeIds[c];
					ILanguageDescriptor[] langs = (ILanguageDescriptor[])map.remove(cTypeId);
					if(langs != null && langs.length != 0){
						for(int q = 0; q < langs.length; q++){
							langMap.remove(langs[q].getId());
						}
								
						adjustLanguageData(data, lData, langs[0]);
					}
				}
			}
		}
			
		if(!langMap.isEmpty()){
			addLangs(cfgData, data, factory, langMap, map);
		}
		
	}
	
	private static CLanguageData adjustLanguageData(CFolderData data, CLanguageData lData, ILanguageDescriptor des){
		String [] cTypeIds = des.getContentTypeIds();
		String srcIds[] = lData.getSourceContentTypeIds();
		
		Set landTypes = new HashSet(Arrays.asList(cTypeIds));
		landTypes.removeAll(Arrays.asList(srcIds));
		
		if(landTypes.size() != 0){
			List srcList = new ArrayList();
			srcList.addAll(landTypes);
			lData.setSourceContentTypeIds((String[])srcList.toArray(new String[srcList.size()]));
		}
		
		if(!des.getId().equals(lData.getLanguageId())){
			lData.setLanguageId(des.getId());
		}
		return lData;
	}
	
	private static void addLangs(CConfigurationData cfgData, CFolderData data, CDataFacroty factory, Map langMap, Map cTypeToLangMap){
		List list = new ArrayList(langMap.values());
		ILanguageDescriptor des;
		while(list.size() != 0){
			des = (ILanguageDescriptor)list.remove(list.size() - 1);
			String[] ctypeIds = des.getContentTypeIds();
			boolean addLang = false;
			for(int i = 0; i < ctypeIds.length; i++){
				ILanguageDescriptor[] langs = (ILanguageDescriptor[])cTypeToLangMap.remove(ctypeIds[i]);
				if(langs != null && langs.length != 0){
					addLang = true;
					for(int q = 0; q < langs.length; q++){
						list.remove(langs[q]);
					}
				}
			}
			
			if(addLang){
				CLanguageData lData = factory.createLanguageData(cfgData, data, genId(data.getId()), des.getName(), des.getId(), 
						ICLanguageSettingEntry.INCLUDE_FILE 
						| ICLanguageSettingEntry.INCLUDE_PATH
						| ICLanguageSettingEntry.MACRO
						| ICLanguageSettingEntry.MACRO_FILE,
						ctypeIds, true);
				factory.link(data, lData);
			}
		}
	}
	
	public static boolean isExcluded(IPath path, ICSourceEntry[] entries){
		for(int i = 0; i < entries.length; i++){
			if(!isExcluded(path, entries[i]))
				return false;
		}
		return true;
	}
	
	public static boolean isExcluded(IPath path, ICSourceEntry entry){
		IPath entryPath = new Path(entry.getName());
		
		if(path.isPrefixOf(entryPath))
			return false;
		
		if(!entryPath.isPrefixOf(path))
			return true;
		
		if(path.segmentCount() == 0)
			return false;
		char[][] exclusions = entry.fullExclusionPatternChars();
		return CoreModelUtil.isExcluded(path, exclusions);
	}

	public static boolean isOnSourceEntry(IPath path, ICSourceEntry entry){
		IPath entryPath = new Path(entry.getName());
		
		if(path.equals(entryPath))
			return true;
		
		if(!entryPath.isPrefixOf(path))
			return false;
		
		if(path.segmentCount() == 0)
			return true;
		char[][] exclusions = entry.fullExclusionPatternChars();
		return !CoreModelUtil.isExcluded(path, exclusions);
	}

	public static boolean canExclude(IPath path, boolean isFolder, boolean excluded, ICSourceEntry[] entries){
		try {
			return setExcluded(path, isFolder, excluded, entries, false) != null;
		} catch (CoreException e) {
		}
		return false;
	}

	public static ICSourceEntry[] setExcluded(IPath path, boolean isFolder, boolean excluded, ICSourceEntry[] entries) throws CoreException {
		return setExcluded(path, isFolder, excluded, entries, true);
	}

	public static ICSourceEntry[] setExcludedIfPossible(IPath path, boolean isFolder, boolean excluded, ICSourceEntry[] entries) {
		try {
			ICSourceEntry[] newEntries = setExcluded(path, isFolder, excluded, entries, false);
			if(newEntries == null)
				newEntries = entries;
			return newEntries;
		} catch (CoreException e) {
		}
		return entries;
	}

	public static ICSourceEntry[] setExcluded(IPath path, boolean isFolder, boolean excluded, ICSourceEntry[] entries, boolean throwExceptionOnErr) throws CoreException {
		if(isExcluded(path, entries) == excluded)
			return entries;
		
		ICSourceEntry[] newEntries;
		if(excluded){
			List includeList = new ArrayList(entries.length);
			List excludeList = new ArrayList(entries.length);
			
			sortEntries(path, false, entries, includeList, excludeList);
			
			for(int i = 0; i < includeList.size(); i++){
				ICSourceEntry oldEntry = (ICSourceEntry)includeList.get(i);
				List tmp = new ArrayList(1);
				tmp.add(path);
				ICSourceEntry newEntry = addExcludePaths(oldEntry, tmp, true);
				if(newEntry != null)
					excludeList.add(newEntry);
			}
			
			newEntries = (ICSourceEntry[])excludeList.toArray(new ICSourceEntry[excludeList.size()]);
		} else {
			List includeList = new ArrayList(entries.length + 1);
			List excludeList = new ArrayList(entries.length);

			sortIncludingExcludingEntries(path, entries, includeList, excludeList);
			boolean included = false;
			if(includeList.size() != 0){
				if(includeExclusion(path, includeList) >= 0)
					included = true;
			}
			
			if(!included){
				if(isFolder){
					includeList.add(new CSourceEntry(path, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED));
				} else {
					if(throwExceptionOnErr)
						throw ExceptionFactory.createCoreException("can not create a source entry for individual file");
					return null;
				}
			}
			
			includeList.addAll(excludeList);
			newEntries = (ICSourceEntry[])includeList.toArray(new ICSourceEntry[includeList.size()]);
		}
		
		return newEntries;
	}
	
	private static int includeExclusion(IPath path, List entries){
		for(int i = 0; i < entries.size(); i++){
			ICSourceEntry entry = (ICSourceEntry)entries.get(i);
			entry = include(path, entry);
			if(entry != null)
				entries.set(i, entry);
			return i;
		}
		return -1;
	}
	
	private static ICSourceEntry include(IPath path, ICSourceEntry entry){
		IPath[] exclusions = entry.getExclusionPatterns();
		IPath entryPath = new Path(entry.getName());
		IPath relPath = path.removeFirstSegments(entryPath.segmentCount()).makeRelative();
		for(int k = 0; k < exclusions.length; k++){
			if(exclusions[k].equals(relPath)){
				IPath updatedExclusions[] = new IPath[exclusions.length - 1];
				System.arraycopy(exclusions, 0, updatedExclusions, 0, k);
				System.arraycopy(exclusions, k + 1, updatedExclusions, k, updatedExclusions.length - k);
				ICSourceEntry updatedEntry = new CSourceEntry(entry.getName(), updatedExclusions, entry.getFlags());
				if(isOnSourceEntry(path, updatedEntry))
					return updatedEntry;
				exclusions = updatedExclusions;
				entry = updatedEntry;
			}
		}
		return null;
	}
	
	private static void sortIncludingExcludingEntries(IPath path, ICSourceEntry[] entries, List including, List excluding){
		for(int i = 0; i < entries.length; i++){
			IPath entryPath = new Path(entries[i].getName());
			if(entryPath.isPrefixOf(path))
				including.add(entries[i]);
			else
				excluding.add(entries[i]);
		}
	}

	public static ICSourceEntry[] adjustEntries(ICSourceEntry entries[]){
		return adjustEntries(entries, false, null);
	}

	private static ICSourceEntry[] getDefaultEntries(boolean absolute, IProject project){
		ICSourceEntry entry;
		if(absolute){
			if(project != null)
				entry = new CSourceEntry(project.getFullPath(), null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSourceEntry.RESOLVED);
			else
				entry = new CSourceEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSourceEntry.RESOLVED);
		} else {
			entry = new CSourceEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSourceEntry.RESOLVED);
		}
		return new ICSourceEntry[]{entry};
	}

	public static ICSourceEntry[] adjustEntries(ICSourceEntry entries[], boolean makeAbsolute, IProject project){
		if(entries == null)
			return getDefaultEntries(makeAbsolute, project);
		
		ICSourceEntry ei, ej;
		LinkedHashMap map = new LinkedHashMap();
		for(int i = 0; i < entries.length; i++){
			ei = entries[i];
			List list = null;
			for(int j = 0; j < entries.length; j++){
				ej = entries[j];
				if(ei == ej)
					continue;
				
				IPath ejPath = new Path(ej.getName());
				if(!isExcluded(ejPath, ei)){
					if(list == null)
						list = new ArrayList();
					list.add(ejPath);
				}
			}
			
			map.put(ei, list);
		}
		List resultList = new ArrayList(entries.length);
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			List list = (List)entry.getValue();
			if(list == null)
				resultList.add(entry.getKey());
			else {
				ICSourceEntry se = (ICSourceEntry)entry.getKey();
				se = addExcludePaths(se, list, true);
				if(se != null)
					resultList.add(se);
			}
		}
		
		if(makeAbsolute){
			if(project != null)
				resultList = makeAbsolute(project, resultList);
		} else {
			resultList = makeRelative(project, resultList);
		}
		return (ICSourceEntry[])resultList.toArray(new ICSourceEntry[resultList.size()]);
	}

	private static List makeRelative(IProject project, List list){
		int size = list.size();
		
		for(int i = 0; i < size; i++){
			list.set(i, makeRelative(project, (ICSourceEntry)list.get(i)));
		}
		return list;
	}

	private static List makeAbsolute(IProject project, List list){
		int size = list.size();
		
		for(int i = 0; i < size; i++){
			list.set(i, makeAbsolute(project, (ICSourceEntry)list.get(i)));
		}
		return list;
	}

	public static ICSourceEntry makeAbsolute(IProject project, ICSourceEntry entry){
		if(project == null)
			return entry;
		
		IPath path = new Path(entry.getName());
		if(path.isAbsolute())
			return entry;
		
		path = project.getFullPath().append(path);
		
		return new CSourceEntry(path, entry.getExclusionPatterns(), entry.getFlags());
	}

	public static ICSourceEntry makeRelative(IProject project, ICSourceEntry entry){
		IPath path = new Path(entry.getName());
		if(!path.isAbsolute())
			return entry;
		
//		if(project != null){
//			
//			IPath projPath = project.getFullPath();
//			
//		}
//		if(pro)
		
		return new CSourceEntry(path.removeFirstSegments(1).makeRelative(), entry.getExclusionPatterns(), entry.getFlags());
	}

	private static Collection removePrefix(IPath prefix, Collection paths, Collection result){
		if(result == null)
			result = new ArrayList(paths.size());
		for(Iterator iter = paths.iterator(); iter.hasNext(); ){
			IPath path = (IPath)iter.next();
			if(prefix.isPrefixOf(path))
				result.add(path.removeFirstSegments(prefix.segmentCount()));
//			else
//				result.add(path);
		}
		return result;
	}

	public static ICSourceEntry addExcludePaths(ICSourceEntry entry, Collection paths, boolean removePrefix){
		IPath entryPath = new Path(entry.getName());
		IPath[] oldExclusions = entry.getExclusionPatterns();
//		List newExList = new ArrayList(oldExclusions.length + paths.size()); 
		LinkedHashSet newSet = new LinkedHashSet();
		if(removePrefix){
			removePrefix(entryPath, paths, newSet);
		} else {
			newSet.addAll(paths);
		}
		
		for(Iterator iter = newSet.iterator(); iter.hasNext();){
			IPath path = (IPath)iter.next();
			if(path.segmentCount() == 0)
				return null;
		}
		
		newSet.addAll(Arrays.asList(oldExclusions));
		
		IPath[] newExclusions = (IPath[])newSet.toArray(new IPath[newSet.size()]);
		return new CSourceEntry(entry.getName(), newExclusions, entry.getFlags());
	}
	
	private static void sortEntries(IPath path, boolean byExclude, ICSourceEntry[] entries, List included, List excluded){
		for(int i = 0; i < entries.length; i++){
			if(byExclude ? isExcluded(path, entries[i]) : !isOnSourceEntry(path, entries[i])){
				if(excluded != null)
					excluded.add(entries[i]);
			} else {
				if(included != null)
					included.add(entries[i]);
			}
		}
	}
	
	public static Map fillEntriesMapByNameKey(Map map, ICSettingEntry[] entries){
		if(map == null)
			map = new LinkedHashMap();
		
		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			map.put(new EntryNameKey(entry), entry);
		}
		return map;
	}

	public static Map fillEntriesMapByContentsKey(Map map, ICSettingEntry[] entries){
		if(map == null)
			map = new LinkedHashMap();
		
		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			map.put(new EntryContentsKey(entry), entry);
		}
		return map;
	}
	
	public static boolean getBoolean(ICStorageElement el, String attr, boolean defaultValue){
		if(el != null){
			String tmp = el.getAttribute(attr);
			if(tmp != null){
				return Boolean.valueOf(tmp).booleanValue();
			}
		}
		return defaultValue;
	}

	public static void setBoolean(ICStorageElement el, String attr, boolean value){
		el.setAttribute(attr, Boolean.valueOf(value).toString());
	}

	public static int getInteger(ICStorageElement el, String attr, int defaultValue){
		if(el != null){
			String tmp = el.getAttribute(attr);
			if(tmp != null){
				try {
					return Integer.parseInt(tmp);
				} catch (NumberFormatException e) {
				}
			}
		}
		return defaultValue;
	}

	public static void setInteger(ICStorageElement el, String attr, int value){
		el.setAttribute(attr, new Integer(value).toString());
	}
	
	public static ICExclusionPatternPathEntry addRemoveExclusionsToEntry(ICExclusionPatternPathEntry entry, IPath[] paths, boolean add) throws IllegalArgumentException{
		if(paths == null || paths.length == 0)
			return entry;
		
		Set set = mergeRemovingDups(entry.getExclusionPatterns(), paths, add);
		IPath exclusions[] = (IPath[])set.toArray(new IPath[set.size()]);
		
		return (ICExclusionPatternPathEntry)createEntry(entry.getKind(), entry.getName(), null, exclusions, entry.getFlags());
	}

	private static Set mergeRemovingDups(Object o1[], Object o2[], boolean add){
		LinkedHashSet set = new LinkedHashSet();
		set.addAll(Arrays.asList(o1));
		if(add)
			set.addAll(Arrays.asList(o2));
		else
			set.removeAll(Arrays.asList(o2));
		return set;
	}
}
