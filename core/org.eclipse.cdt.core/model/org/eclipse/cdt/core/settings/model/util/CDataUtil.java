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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
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
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
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
	private static final String EMPTY = "";  //$NON-NLS-1$
	private static final String DELIM = " "; //$NON-NLS-1$

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
		List<String> list = new ArrayList<String>(t.countTokens());
		while (t.hasMoreElements()) {
			list.add(t.nextToken());
		}
		return list.toArray(new String[list.size()]);
	}

	public static ICSettingEntry[] resolveEntries(ICSettingEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;

		ArrayList<ICSettingEntry> out = new ArrayList<ICSettingEntry>(entries.length);
		ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();

		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			out.addAll(Arrays.asList(createResolvedEntry(entry, cfgDes, mngr)));
		}
		return out.toArray(new ICSettingEntry[out.size()]);
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

	private static ICSettingEntry[] createResolvedEntry(ICSettingEntry entry, ICConfigurationDescription cfg, ICdtVariableManager mngr){
		if(entry.isResolved())
			return new ICSettingEntry[] { entry };

		String name = entry.getName();

		String[] names = new String[] { name }; // default value
		try {
			if ((entry.getKind() != ICSettingEntry.MACRO) &&
					mngr.isStringListValue(name, cfg)) {
				names = mngr.resolveStringListValue(name, EMPTY, DELIM, cfg);
			} else {
				names[0] = mngr.resolveValue(name, EMPTY, DELIM, cfg);
			}
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}

		ICSettingEntry[] result = new ICSettingEntry[names.length];

		for (int k=0; k<names.length; k++) {
			String value = null;
			IPath[] exclusionFilters = null;
			IPath srcPath = null, srcRootPath = null, srcPrefixMapping = null;

			switch (entry.getKind()) {
			case ICSettingEntry.MACRO:
				value = entry.getValue();
				try {
					value = mngr.resolveValue(value, EMPTY, DELIM, cfg);
				} catch (CdtVariableException e) {
					CCorePlugin.log(e);
				}
				break;
			case ICSettingEntry.LIBRARY_FILE:
				ICLibraryFileEntry libFile = (ICLibraryFileEntry)entry;
				srcPath = libFile.getSourceAttachmentPath();
				srcRootPath = libFile.getSourceAttachmentRootPath();
				srcPrefixMapping = libFile.getSourceAttachmentPrefixMapping();
				if(srcPath != null)
					srcPath = resolvePath(mngr, cfg, srcPath);
				if(srcRootPath != null)
					srcRootPath = resolvePath(mngr, cfg, srcRootPath);
				if(srcPrefixMapping != null)
					srcPrefixMapping = resolvePath(mngr, cfg, srcPrefixMapping);
				break;
			case ICSettingEntry.SOURCE_PATH:
			case ICSettingEntry.OUTPUT_PATH:
				exclusionFilters = ((ICExclusionPatternPathEntry)entry).getExclusionPatterns();
				for(int i = 0; i < exclusionFilters.length; i++){
					String exclString = exclusionFilters[i].toString();
					try {
						exclString = mngr.resolveValue(exclString, EMPTY, DELIM, cfg);
					} catch (CdtVariableException e) {
						CCorePlugin.log(e);
					}
					exclusionFilters[i] = new Path(exclString);
				}
				break;
			default:
				break;
			}
			result[k] = createEntry(entry.getKind(), names[k], value, exclusionFilters, entry.getFlags() | ICSettingEntry.RESOLVED, srcPath, srcRootPath, srcPrefixMapping);
		}
		return result;
	}

	private static IPath resolvePath(ICdtVariableManager mngr, ICConfigurationDescription cfg, IPath path){
		if(path == null)
			return null;

		try {
			String unresolved = path.toString();
			String resolved = mngr.resolveValue(unresolved, EMPTY, DELIM, cfg);
			if(resolved != null && !resolved.equals(unresolved))
				path = new Path(resolved);
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}

		return path;
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flagsToAdd, int flafsToClear){
		return createEntry(entry, (entry.getFlags() | flagsToAdd) & (~flafsToClear));
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flags){
		switch (entry.getKind()){
		case ICSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(entry.getName(), flags);
			break;
		case ICSettingEntry.MACRO:
			entry = new CMacroEntry(entry.getName(), entry.getValue(), flags);
			break;
		case ICSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(entry.getName(), flags);
			break;
		case ICSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(entry.getName(), flags);
			break;
		case ICSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(entry.getName(), flags);
			break;
		case ICSettingEntry.LIBRARY_FILE:
			ICLibraryFileEntry libFile = (ICLibraryFileEntry)entry;
			entry = new CLibraryFileEntry(entry.getName(),
					flags,
					libFile.getSourceAttachmentPath(),
					libFile.getSourceAttachmentRootPath(),
					libFile.getSourceAttachmentPrefixMapping()
					);
			break;
		}
		return entry;
	}

	public static ICSettingEntry createEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags){
		return createEntry(kind, name, value, exclusionPatterns, flags, null, null, null);
	}


	public static ICSettingEntry createEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, IPath srcPath, IPath srcRootPath, IPath srcPrefixMapping){
		switch (kind){
		case ICSettingEntry.INCLUDE_PATH:
			return new CIncludePathEntry(name, flags);
		case ICSettingEntry.MACRO:
			return new CMacroEntry(name, value, flags);
		case ICSettingEntry.INCLUDE_FILE:
			return new CIncludeFileEntry(name, flags);
		case ICSettingEntry.MACRO_FILE:
			return new CMacroFileEntry(name, flags);
		case ICSettingEntry.LIBRARY_PATH:
			return new CLibraryPathEntry(name, flags);
		case ICSettingEntry.LIBRARY_FILE:
			return new CLibraryFileEntry(name, flags, srcPath, srcRootPath, srcPrefixMapping);
		case ICSettingEntry.OUTPUT_PATH:
			return new COutputEntry(name, exclusionPatterns, flags);
		case ICSettingEntry.SOURCE_PATH:
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
				exts = exts.clone();
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
				List<String> list = new ArrayList<String>();
				for(int i = 0; i < typeIds.length; i++){
					type = manager.getContentType(typeIds[i]);
					if(type != null) {
						list.addAll(Arrays.asList(getContentTypeFileSpecs(project, type)));
					}
				}
				exts = list.toArray(new String[list.size()]);
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

	public static Map<IPath, CResourceData> createPathRcDataMap(CConfigurationData data){
		Map<IPath, CResourceData> map = new HashMap<IPath, CResourceData>();
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

	public static CConfigurationData createEmptyData(String id, String name, CDataFactory factory, boolean performLangAdjustment){
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

	public static CConfigurationData adjustConfig(CConfigurationData cfg, CDataFactory factory){
		LanguageManager mngr = LanguageManager.getInstance();
		ILanguageDescriptor dess[] = mngr.getLanguageDescriptors();
		Map<String, ILanguageDescriptor[]> map = mngr.getContentTypeIdToLanguageDescriptionsMap();

		CResourceData[] rcDatas = cfg.getResourceDatas();
		for(int i = 0; i < rcDatas.length; i++){
			if(rcDatas[i].getType() == ICSettingBase.SETTING_FOLDER){
				adjustFolderData(cfg, (CFolderData)rcDatas[i], factory, dess, new HashMap<String, ILanguageDescriptor[]>(map));
			}
		}

		return cfg;
	}


	private static void adjustFolderData(CConfigurationData cfgData, CFolderData data, CDataFactory factory, ILanguageDescriptor dess[], HashMap<String, ILanguageDescriptor[]> map){
		Map<String, ILanguageDescriptor> langMap = new HashMap<String, ILanguageDescriptor>();
		for(int i = 0; i < dess.length; i++){
			langMap.put(dess[i].getId(), dess[i]);
		}
		CLanguageData lDatas[] = data.getLanguageDatas();
		for(int i = 0; i < lDatas.length; i++){
			CLanguageData lData = lDatas[i];
			String langId = lData.getLanguageId();
			if(langId != null){
				ILanguageDescriptor des = langMap.remove(langId);
				adjustLanguageData(data, lData, des);
						continue;
			} else {
				String[] cTypeIds = lData.getSourceContentTypeIds();
				for(int c = 0; c < cTypeIds.length; c++){
					String cTypeId = cTypeIds[c];
					ILanguageDescriptor[] langs = map.remove(cTypeId);
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

		Set<String> landTypes = new HashSet<String>(Arrays.asList(cTypeIds));
		landTypes.removeAll(Arrays.asList(srcIds));

		if(landTypes.size() != 0){
			List<String> srcList = new ArrayList<String>();
			srcList.addAll(landTypes);
			lData.setSourceContentTypeIds(srcList.toArray(new String[srcList.size()]));
		}

		if(!des.getId().equals(lData.getLanguageId())){
			lData.setLanguageId(des.getId());
		}
		return lData;
	}

	private static void addLangs(CConfigurationData cfgData, CFolderData data, CDataFactory factory, Map<String, ILanguageDescriptor> langMap, Map<String, ILanguageDescriptor[]> cTypeToLangMap){
		List<ILanguageDescriptor> list = new ArrayList<ILanguageDescriptor>(langMap.values());
		ILanguageDescriptor des;
		while(list.size() != 0){
			des = list.remove(list.size() - 1);
			String[] ctypeIds = des.getContentTypeIds();
			boolean addLang = false;
			for(int i = 0; i < ctypeIds.length; i++){
				ILanguageDescriptor[] langs = cTypeToLangMap.remove(ctypeIds[i]);
				if(langs != null && langs.length != 0){
					addLang = true;
					for(int q = 0; q < langs.length; q++){
						list.remove(langs[q]);
					}
				}
			}

			if(addLang){
				CLanguageData lData = factory.createLanguageData(cfgData, data, genId(data.getId()), des.getName(), des.getId(),
						ICSettingEntry.INCLUDE_FILE
						| ICSettingEntry.INCLUDE_PATH
						| ICSettingEntry.MACRO
						| ICSettingEntry.MACRO_FILE,
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
			ICSourceEntry[] out = setExcluded(path, isFolder, excluded, entries, false);
			return !isEqual(entries, out);
		} catch (CoreException e) {	}
		return false;
	}

	/**
	 *
	 * @param ein - initial source entries
	 * @param aus - resulting source entries
	 * @return - true if they are equal
	 */
	public static boolean isEqual(ICSourceEntry[] ein, ICSourceEntry[] aus) {
		if (ein == null || aus == null) return (ein == null && aus == null);
		if (ein.length != aus.length) return false;
		for (int i=0; i<ein.length; i++) {
			boolean found = false;
			for (int j=0; j<aus.length; j++) {
				if (!ein[i].equalsByName(aus[j]))
					continue;
				if (ein[i].equalsByContents(aus[j])) {
					found = true;
					break;
				}
				return false; // contents is changed !
			}
			if (!found)
				return false; // name is not found !
		}
		return true; // all entries are equal by name and contents
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
			List<ICSourceEntry> includeList = new ArrayList<ICSourceEntry>(entries.length);
			List<ICSourceEntry> excludeList = new ArrayList<ICSourceEntry>(entries.length);

			sortEntries(path, false, entries, includeList, excludeList);

			for(int i = 0; i < includeList.size(); i++){
				ICSourceEntry oldEntry = includeList.get(i);
				List<IPath> tmp = new ArrayList<IPath>(1);
				tmp.add(path);
				ICSourceEntry newEntry = addExcludePaths(oldEntry, tmp, true);
				if(newEntry != null)
					excludeList.add(newEntry);
			}

			newEntries = excludeList.toArray(new ICSourceEntry[excludeList.size()]);
		} else {
			List<ICSourceEntry> includeList = new ArrayList<ICSourceEntry>(entries.length + 1);
			List<ICSourceEntry> excludeList = new ArrayList<ICSourceEntry>(entries.length);

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
						throw ExceptionFactory.createCoreException("can not create a source entry for individual file"); //$NON-NLS-1$
					return null;
				}
			}

			includeList.addAll(excludeList);
			newEntries = includeList.toArray(new ICSourceEntry[includeList.size()]);
		}

		return newEntries;
	}

	private static int includeExclusion(IPath path, List<ICSourceEntry> entries){
		for(int i = 0; i < entries.size(); i++){
			ICSourceEntry entry = entries.get(i);
			entry = include(path, entry);
			if(entry != null) {
				entries.set(i, entry);
				return i;
			}
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

	private static void sortIncludingExcludingEntries(IPath path, ICSourceEntry[] entries, List<ICSourceEntry> including, List<ICSourceEntry> excluding){
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

	private static ICSourceEntry[] getDefaultSourceEntries(boolean absolute, IProject project){
		ICSourceEntry entry;
		if(absolute){
			if(project != null)
				entry = new CSourceEntry(project.getFullPath(), null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
			else
				entry = new CSourceEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		} else {
			entry = new CSourceEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		}
		return new ICSourceEntry[]{entry};
	}

	private static ICOutputEntry[] getDefaultOutputEntries(boolean absolute, IProject project){
		ICOutputEntry entry;
		if(absolute){
			if(project != null)
				entry = new COutputEntry(project.getFullPath(), null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
			else
				entry = new COutputEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		} else {
			entry = new COutputEntry(Path.EMPTY, null, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		}
		return new ICOutputEntry[]{entry};
	}

	public static ICOutputEntry[] adjustEntries(ICOutputEntry entries[], boolean makeAbsolute, IProject project){
		if(entries == null || entries.length == 0)
			return getDefaultOutputEntries(makeAbsolute, project);

		return makeAbsolute ? makeAbsolute(project, entries) : makeRelative(project, entries);
	}

	public static ICSourceEntry[] adjustEntries(ICSourceEntry entries[], boolean makeAbsolute, IProject project){
		if(entries == null || entries.length == 0)
			return getDefaultSourceEntries(makeAbsolute, project);

		ICSourceEntry ei, ej;
		LinkedHashMap<ICSourceEntry, List<IPath>> map = new LinkedHashMap<ICSourceEntry, List<IPath>>();
		for(int i = 0; i < entries.length; i++){
			ei = entries[i];
			List<IPath> list = null;
			for(int j = 0; j < entries.length; j++){
				ej = entries[j];
				if(ei == ej)
					continue;

				IPath ejPath = new Path(ej.getName());
				if(!isExcluded(ejPath, ei)){
					if(list == null)
						list = new ArrayList<IPath>();
					list.add(ejPath);
				}
			}

			map.put(ei, list);
		}
		List<ICSourceEntry> resultList = new ArrayList<ICSourceEntry>(entries.length);
		for(Iterator<Map.Entry<ICSourceEntry, List<IPath>>> iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry<ICSourceEntry, List<IPath>> entry = iter.next();
			List<IPath> list = entry.getValue();
			if(list == null)
				resultList.add(entry.getKey());
			else {
				ICSourceEntry se = entry.getKey();
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

		ICSourceEntry[] resultArray = resultList.toArray(new ICSourceEntry[resultList.size()]);
		Arrays.sort(resultArray, new Comparator<ICSourceEntry>() {
			@Override
			public int compare(ICSourceEntry o1, ICSourceEntry o2) {
				return o1.getFullPath().toString().compareTo(o2.getFullPath().toString());
			}
		});

		return resultArray;
	}

	private static List<ICSourceEntry> makeRelative(IProject project, List<ICSourceEntry> list){
		int size = list.size();

		for(int i = 0; i < size; i++){
			list.set(i, makeRelative(project, list.get(i)));
		}
		return list;
	}

	private static List<ICSourceEntry> makeAbsolute(IProject project, List<ICSourceEntry> list){
		int size = list.size();

		for(int i = 0; i < size; i++){
			list.set(i, makeAbsolute(project, list.get(i)));
		}
		return list;
	}

	public static ICSourceEntry makeAbsolute(IProject project, ICSourceEntry entry){
		return (ICSourceEntry)makeAbsolute(project, entry, true);
	}

	public static ICSourceEntry makeRelative(IProject project, ICSourceEntry entry){
		return (ICSourceEntry)makeRelative(project, entry, true);
	}

	public static ICSourceEntry[] makeRelative(IProject project, ICSourceEntry[] entries){
		return (ICSourceEntry[])makeRelative(project, entries, true);
	}

	public static ICSourceEntry[] makeAbsolute(IProject project, ICSourceEntry[] entries){
		return (ICSourceEntry[])makeAbsolute(project, entries, true);
	}

	public static ICOutputEntry makeAbsolute(IProject project, ICOutputEntry entry){
		return (ICOutputEntry)makeAbsolute(project, entry, true);
	}

	public static ICOutputEntry makeRelative(IProject project, ICOutputEntry entry){
		return (ICOutputEntry)makeRelative(project, entry, true);
	}

	public static ICOutputEntry[] makeAbsolute(IProject project, ICOutputEntry[] entries){
		return (ICOutputEntry[])makeAbsolute(project, entries, true);
	}

	public static ICOutputEntry[] makeRelative(IProject project, ICOutputEntry[] entries){
		return (ICOutputEntry[])makeRelative(project, entries, true);
	}

	private static Collection<IPath> removePrefix(IPath prefix, Collection<IPath> paths, Collection<IPath> result){
		if(result == null)
			result = new ArrayList<IPath>(paths.size());
		for(Iterator<IPath> iter = paths.iterator(); iter.hasNext(); ){
			IPath path = iter.next();
			if(prefix.isPrefixOf(path))
				result.add(path.removeFirstSegments(prefix.segmentCount()));
//			else
//				result.add(path);
		}
		return result;
	}

	public static ICSourceEntry addExcludePaths(ICSourceEntry entry, Collection<IPath> paths, boolean removePrefix){
		IPath entryPath = new Path(entry.getName());
		IPath[] oldExclusions = entry.getExclusionPatterns();
//		List newExList = new ArrayList(oldExclusions.length + paths.size());
		LinkedHashSet<IPath> newSet = new LinkedHashSet<IPath>();
		if(removePrefix){
			removePrefix(entryPath, paths, newSet);
		} else {
			newSet.addAll(paths);
		}

		for(Iterator<IPath> iter = newSet.iterator(); iter.hasNext();){
			IPath path = iter.next();
			if(path.segmentCount() == 0)
				return null;
		}

		newSet.addAll(Arrays.asList(oldExclusions));

		IPath[] newExclusions = newSet.toArray(new IPath[newSet.size()]);
		return new CSourceEntry(entry.getName(), newExclusions, entry.getFlags());
	}

	private static void sortEntries(IPath path, boolean byExclude, ICSourceEntry[] entries, List<ICSourceEntry> included, List<ICSourceEntry> excluded){
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

	public static Map<EntryNameKey, ICSettingEntry> fillEntriesMapByNameKey(Map<EntryNameKey, ICSettingEntry> map, ICSettingEntry[] entries){
		if(map == null)
			map = new LinkedHashMap<EntryNameKey, ICSettingEntry>();

		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			map.put(new EntryNameKey(entry), entry);
		}
		return map;
	}

	public static Map<EntryContentsKey, ICSettingEntry> fillEntriesMapByContentsKey(Map<EntryContentsKey, ICSettingEntry> map, ICSettingEntry[] entries){
		if(map == null)
			map = new LinkedHashMap<EntryContentsKey, ICSettingEntry>();

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

		Set<IPath> set = mergeRemovingDups(entry.getExclusionPatterns(), paths, add);
		IPath exclusions[] = set.toArray(new IPath[set.size()]);

		return (ICExclusionPatternPathEntry)createEntry(entry.getKind(), entry.getName(), null, exclusions, entry.getFlags());
	}

	private static Set<IPath> mergeRemovingDups(IPath[] o1, IPath[] o2, boolean add){
		LinkedHashSet<IPath> set = new LinkedHashSet<IPath>();
		set.addAll(Arrays.asList(o1));
		if(add)
			set.addAll(Arrays.asList(o2));
		else
			set.removeAll(Arrays.asList(o2));
		return set;
	}

	public static ICExclusionPatternPathEntry makeAbsolute(IProject project, ICExclusionPatternPathEntry entry, boolean force){
		if(!entry.isValueWorkspacePath() && !force)
			return entry;

		IPath path = new Path(entry.getName());
		IPath projPath = project.getFullPath();
		if(!path.isAbsolute() || (force && !projPath.isPrefixOf(path))){
			path = projPath.append(path).makeAbsolute();
			return (ICExclusionPatternPathEntry)CDataUtil.createEntry(entry.getKind(), path.toString(), null, entry.getExclusionPatterns(), entry.getFlags());
		}
		return entry;
	}

	public static ICExclusionPatternPathEntry makeRelative(IProject project, ICExclusionPatternPathEntry entry, boolean force){
		if(!entry.isValueWorkspacePath() && !force)
			return entry;

		IPath path = new Path(entry.getName());
		IPath projPath = project.getFullPath();

		if(path.isAbsolute()){
			if(projPath.isPrefixOf(path))
				path = path.removeFirstSegments(projPath.segmentCount()).makeRelative();
			else if (force)
				path = path.makeRelative();
			return (ICExclusionPatternPathEntry)CDataUtil.createEntry(entry.getKind(), path.toString(), null, entry.getExclusionPatterns(), entry.getFlags());
		}
		return entry;
	}

	public static ICExclusionPatternPathEntry[] makeRelative(IProject project, ICExclusionPatternPathEntry[] entries, boolean force){
		if(entries == null)
			return null;

		ICExclusionPatternPathEntry[] relEntries = (ICExclusionPatternPathEntry[])Array.newInstance(entries.getClass().getComponentType(), entries.length);
		for(int i = 0; i < entries.length; i++){
			relEntries[i] = makeRelative(project, entries[i], force);
		}
		return relEntries;
	}

	public static ICExclusionPatternPathEntry[] makeAbsolute(IProject project, ICExclusionPatternPathEntry[] entries, boolean force){
		if(entries == null)
			return null;

		ICExclusionPatternPathEntry[] relEntries = (ICExclusionPatternPathEntry[])Array.newInstance(entries.getClass().getComponentType(), entries.length);
		for(int i = 0; i < entries.length; i++){
			relEntries[i] = makeAbsolute(project, entries[i], force);
		}
		return relEntries;
	}
}
