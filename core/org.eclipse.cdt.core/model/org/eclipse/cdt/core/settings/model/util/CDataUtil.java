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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
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
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFacroty;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
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
	
	public static ICLanguageSettingEntry[] resolveEntries(ICLanguageSettingEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;
		
		ICLanguageSettingEntry[] resolved = new ICLanguageSettingEntry[entries.length];
		ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();

		for(int i = 0; i < entries.length; i++){
			ICLanguageSettingEntry entry = entries[i];
			resolved[i] = createResolvedEntry(entry, cfgDes, mngr);
		}
		
		return resolved;
	}
	
	private static ICLanguageSettingEntry createResolvedEntry(ICLanguageSettingEntry entry, ICConfigurationDescription cfg, ICdtVariableManager mngr){
		if(entry.isResolved())
			return entry;
		
		String name = entry.getName();
		try {
			name = mngr.resolveValue(name, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		
		switch (entry.getKind()) {
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return new CIncludePathEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return new CIncludeFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.MACRO:
			String value = entry.getValue();
			try {
				value = mngr.resolveValue(value, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}
			return new CMacroEntry(name, value, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.MACRO_FILE:
			return new CMacroFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return new CLibraryPathEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return new CLibraryFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		default:
			throw new IllegalArgumentException();
		}
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

}
