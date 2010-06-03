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

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class CDataSerializer {
	protected static final String NAME = "name"; //$NON-NLS-1$
	protected static final String ID = "id"; //$NON-NLS-1$
	protected static final String DESCRIPTION = "description"; //$NON-NLS-1$
	protected static final String SOURCE_ENTRIES = "sourceEntries"; //$NON-NLS-1$
	protected static final String PATH = "path"; //$NON-NLS-1$
	protected static final String LANGUAGE_ID = "languageId"; //$NON-NLS-1$
	protected static final String CONTENT_TYPE_IDS = "contentTypeIds"; //$NON-NLS-1$
	protected static final String EXTENSIONS = "extensions"; //$NON-NLS-1$
	protected static final String DELIMITER = ";"; //$NON-NLS-1$
	protected static final String FOLDER_DATA = "folderData"; //$NON-NLS-1$
	protected static final String FILE_DATA = "fileData"; //$NON-NLS-1$
	protected static final String BUILD_DATA = "buildData"; //$NON-NLS-1$
	protected static final String TARGET_PLATFORM_DATA = "targetPlatformData"; //$NON-NLS-1$
	protected static final String LANGUAGE_DATA = "languageData"; //$NON-NLS-1$
//	protected static final String EXCLUDED = "excluded";
	protected static final String OUTPUT_ENTRIES = "outputEntries"; //$NON-NLS-1$
	protected static final String ERROR_PARSERS = "errorParsers"; //$NON-NLS-1$
	protected static final String BINARY_PARSERS = "binaryParsers"; //$NON-NLS-1$
	protected static final String CWD = "cwd"; //$NON-NLS-1$
	protected static final String SETTING_ENTRIES = "settingEntries"; //$NON-NLS-1$
	protected static final String SUPPORTED_ENTRY_KINDS = "supportedEntryKinds"; //$NON-NLS-1$
	
	private static CDataSerializer fInstance;
	
	public static CDataSerializer getDefault(){
		if(fInstance == null)
			fInstance = new CDataSerializer();
		return fInstance;
	}

	public CConfigurationData loadConfigurationData(CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.20"))); //$NON-NLS-1$
		
		String name = el.getAttribute(NAME);
		CConfigurationData data = factory.createConfigurationdata(id, name, null, false);
		if(data == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.21"))); //$NON-NLS-1$
		
		String tmp = el.getAttribute(DESCRIPTION);
		if(tmp != null)
			data.setDescription(tmp);
		
//		tmp = el.getAttribute(SOURCE_PATHS);
//		if(tmp != null){
//			String[] strPaths = CDataUtil.stringToArray(tmp, DELIMITER);
//			IPath[] paths = new IPath[strPaths.length];
//			for(int i = 0; i < paths.length; i++){
//				paths[i] = new Path(strPaths[i]);
//			}
//			data.setSourcePaths(paths);
//		}
		
		ICStorageElement[] children = el.getChildren();
		ICStorageElement child;
		String childName;
		for(int i = 0; i < children.length; i++){
			child = children[i];
			childName = child.getName();
			if(FOLDER_DATA.equals(childName)){
				CFolderData foData = loadFolderData(data, factory, child);
				if(foData != null)
					factory.link(data, foData);
			} else if (FILE_DATA.equals(childName)){
				CFileData fiData = loadFileData(data, factory, child);
				if(fiData != null)
					factory.link(data, fiData);
			} else if (BUILD_DATA.equals(childName)){
				CBuildData bData = loadBuildData(data, factory, child);
				if(bData != null)
					factory.link(data, bData);
			} else if (TARGET_PLATFORM_DATA.equals(childName)){
				CTargetPlatformData tpData = loadTargetPlatformData(data, factory, child);
				if(tpData != null)
					factory.link(data, tpData);
			} else if (SOURCE_ENTRIES.equals(childName)){
				List<ICSettingEntry> list = LanguageSettingEntriesSerializer.loadEntriesList(child, ICSettingEntry.SOURCE_PATH);
				ICSourceEntry[] entries = list.toArray(new ICSourceEntry[list.size()]);
				data.setSourceEntries(entries);
			}
		}
		return data;
	}
	
	public CFolderData loadFolderData(CConfigurationData data, CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.22"))); //$NON-NLS-1$
		
		String tmp = el.getAttribute(PATH);
		IPath path = null;
		if(tmp != null){
			path = new Path(tmp);
		}
		if(path == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.23"))); //$NON-NLS-1$

		CFolderData foData = factory.createFolderData(data, null, id, false, path);
		if(foData == null){
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.24"))); //$NON-NLS-1$
		}
		
//		tmp = el.getAttribute(EXCLUDED);
//		if(tmp != null){
//			boolean b = Boolean.valueOf(tmp).booleanValue();
//			foData.setExcluded(b);
//		}
		
		ICStorageElement[] children = el.getChildren();
		ICStorageElement child;
		String childName;
		for(int i = 0; i < children.length; i++){
			child = children[i];
			childName = child.getName();
			if(LANGUAGE_DATA.equals(childName)){
				CLanguageData lData = loadLanguageData(data, foData, factory, child);
				if(lData != null)
					factory.link(foData, lData);
			}
		}
		return foData;
	}
	
	public CFileData loadFileData(CConfigurationData data, CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.25"))); //$NON-NLS-1$
		
		String tmp = el.getAttribute(PATH);
		IPath path = null;
		if(tmp != null){
			path = new Path(tmp);
		}
		if(path == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.26"))); //$NON-NLS-1$

		CFileData fiData = factory.createFileData(data, null, null, id, false, path);
		if(fiData == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.27"))); //$NON-NLS-1$
		
//		tmp = el.getAttribute(EXCLUDED);
//		if(tmp != null){
//			boolean b = Boolean.valueOf(tmp).booleanValue();
//			fiData.setExcluded(b);
//		}
		
		ICStorageElement[] children = el.getChildren();
		ICStorageElement child;
		String childName;
		for(int i = 0; i < children.length; i++){
			child = children[i];
			childName = child.getName();
			if(LANGUAGE_DATA.equals(childName)){
				CLanguageData lData = loadLanguageData(data, fiData, factory, child);
				if(lData != null)
					factory.link(fiData, lData);

			}
		}
		return fiData;
	}

	public CBuildData loadBuildData(CConfigurationData data, CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.28"))); //$NON-NLS-1$
		
		String name = el.getAttribute(NAME);
		
		CBuildData bData = factory.createBuildData(data, null, id, name, false);
		if(bData == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.29"))); //$NON-NLS-1$

		String tmp = el.getAttribute(ERROR_PARSERS);
		if(tmp != null){
			String ids[] = CDataUtil.stringToArray(tmp, DELIMITER);
			bData.setErrorParserIDs(ids);
		}
		
		tmp = el.getAttribute(CWD);
		if(tmp != null){
			IPath cwd = new Path(tmp);
			bData.setBuilderCWD(cwd);
		}
		
		ICStorageElement[] children = el.getChildren();
		ICStorageElement child;
		String childName;
		for(int i = 0; i < children.length; i++){
			child = children[i];
			childName = child.getName();
			if(OUTPUT_ENTRIES.equals(childName)){
				List<ICSettingEntry> list = LanguageSettingEntriesSerializer.loadEntriesList(child);
				for(int k = 0; k < list.size(); k++){
					ICSettingEntry e = list.get(i);
					if(e.getKind() != ICSettingEntry.OUTPUT_PATH)
						list.remove(i);
				}
				bData.setOutputDirectories(list.toArray(new ICOutputEntry[list.size()]));
			}
		}
		return bData;
	}

	public CTargetPlatformData loadTargetPlatformData(CConfigurationData data, CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.30"))); //$NON-NLS-1$
		
		String name = el.getAttribute(NAME);
		
		CTargetPlatformData tpData = factory.createTargetPlatformData(data, null, id, name, false);
		if(tpData == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.31"))); //$NON-NLS-1$

		String tmp = el.getAttribute(BINARY_PARSERS);
		if(tmp != null){
			String ids[] = CDataUtil.stringToArray(tmp, DELIMITER);
			tpData.setBinaryParserIds(ids);
		}
		
		return tpData;
	}
	
	public CLanguageData loadLanguageData(CConfigurationData data, CResourceData rcData, CDataFactory factory, ICStorageElement el) throws CoreException {
		String id = el.getAttribute(ID);
		if(id == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.32"))); //$NON-NLS-1$
		
		String name = el.getAttribute(NAME);
		String langId = el.getAttribute(LANGUAGE_ID);
		boolean cTypes = true;
		String ids[];
		String typesStr = el.getAttribute(CONTENT_TYPE_IDS);
		if(typesStr == null){
			cTypes = false;
			typesStr = el.getAttribute(EXTENSIONS);
		}
		
		if(typesStr != null){
			ids = CDataUtil.stringToArray(typesStr, DELIMITER);
		} else {
			ids = CDefaultLanguageData.EMPTY_STRING_ARRAY;
		}
		
		String tmp = el.getAttribute(SUPPORTED_ENTRY_KINDS);
		int supportedKinds = 0;
		if(tmp != null){
			String[] strKinds = CDataUtil.stringToArray(tmp, DELIMITER);
			for(int i = 0; i < strKinds.length; i++){
				supportedKinds |= LanguageSettingEntriesSerializer.stringToKind(strKinds[i]);
			}
		}
		CLanguageData lData = factory.createLanguageData(data, rcData, id, name, langId, supportedKinds, ids, cTypes);
		if(lData == null)
			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, UtilMessages.getString("CDataSerializer.33"))); //$NON-NLS-1$

		ICStorageElement[] children = el.getChildren();
		String childName;
		ICStorageElement child;
		for(int i = 0; i < children.length; i++){
			child = children[i];
			childName = child.getName();
			if(SETTING_ENTRIES.equals(childName)){
				loadEntries(lData, child);
			}
		}
		return lData;
	}
	
	public void loadEntries(CLanguageData lData, ICStorageElement el){
		List<ICSettingEntry> entries = LanguageSettingEntriesSerializer.loadEntriesList(el);
		EntryStore store = new EntryStore();
		store.addEntries(entries.toArray(new ICLanguageSettingEntry[entries.size()]));
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		ICLanguageSettingEntry[] sortedEntries;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			if(store.containsEntriesList(kind)){
				sortedEntries = store.getEntries(kind);
				lData.setEntries(kind, sortedEntries);
			}
		}
	}
	
	private void setAttribute(ICStorageElement el, String name, String value){
		if(value != null)
			el.setAttribute(name, value);
	}

	public void store(CConfigurationData data, ICStorageElement el) throws CoreException {
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());
		setAttribute(el, DESCRIPTION, data.getDescription());
		
		ICSourceEntry[] entries = data.getSourceEntries();
		ICStorageElement child = el.createChild(SOURCE_ENTRIES);
		LanguageSettingEntriesSerializer.serializeEntries(entries, child);
//		if(paths != null && paths.length != 0){
//			setAttribute(el, SOURCE_PATHS, CDataUtil.arrayToString(paths, DELIMITER));
//		}

		CResourceData[] rcDatas = data.getResourceDatas();
		CResourceData rcData;
//		ICStorageElement child;
		for(int i = 0; i < rcDatas.length; i++){
			rcData = rcDatas[i];
			if(rcData.getType() == ICSettingBase.SETTING_FILE){
				child = el.createChild(FILE_DATA);
				store((CFileData)rcData, child);
			} else {
				child = el.createChild(FOLDER_DATA);
				store((CFolderData)rcData, child);
			}
		}
		
		CBuildData bData = data.getBuildData();
		if(bData != null){
			child = el.createChild(BUILD_DATA);
			store(bData, child);
		}
		
		CTargetPlatformData tpData = data.getTargetPlatformData();
		if(tpData != null){
			child = el.createChild(TARGET_PLATFORM_DATA);
			store(tpData, child);
		}
	}

	public void store(CFolderData data, ICStorageElement el){
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());
		
		IPath path = data.getPath();
		if(path != null){
			setAttribute(el, PATH, path.toString());
		}

//		setAttribute(el, EXCLUDED, Boolean.valueOf(data.isExcluded()).toString());

		CLanguageData lDatas[] = data.getLanguageDatas();
		ICStorageElement child;
		for(int i = 0; i < lDatas.length; i++){
			child = el.createChild(LANGUAGE_DATA);
			store(lDatas[i], child);
		}
	}

	public void store(CFileData data, ICStorageElement el){
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());
		
		IPath path = data.getPath();
		if(path != null){
			setAttribute(el, PATH, path.toString());
		}

//		setAttribute(el, EXCLUDED, Boolean.valueOf(data.isExcluded()).toString());

		CLanguageData lData = data.getLanguageData();
		if(lData != null){
			ICStorageElement child = el.createChild(LANGUAGE_DATA);
			store(lData, child);
		}
	}

	public void store(CBuildData data, ICStorageElement el){
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());
		
		String[] errParserIds = data.getErrorParserIDs();
		if(errParserIds != null && errParserIds.length != 0){
			setAttribute(el, ERROR_PARSERS, CDataUtil.arrayToString(errParserIds, DELIMITER));
		}

		IPath cwd = data.getBuilderCWD();
		if(cwd != null){
			setAttribute(el, CWD, cwd.toString());
		}
		
		ICOutputEntry[] outEntries = data.getOutputDirectories();
		if(outEntries != null && outEntries.length != 0){
			ICStorageElement child = el.createChild(OUTPUT_ENTRIES);
			LanguageSettingEntriesSerializer.serializeEntries(outEntries, child);
		}
	}

	public void store(CTargetPlatformData data, ICStorageElement el){
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());

		String[] binParserIds = data.getBinaryParserIds();
		if(binParserIds != null && binParserIds.length != 0){
			setAttribute(el, BINARY_PARSERS, CDataUtil.arrayToString(binParserIds, DELIMITER));
		}
	}

	public void store(CLanguageData data, ICStorageElement el){
		setAttribute(el, ID, data.getId());
		setAttribute(el, NAME, data.getName());
		setAttribute(el, LANGUAGE_ID, data.getLanguageId());
		
		String[] tmp = data.getSourceContentTypeIds();
		if(tmp != null && tmp.length != 0){
			setAttribute(el, CONTENT_TYPE_IDS, CDataUtil.arrayToString(tmp, DELIMITER));
		}
		
		tmp = data.getSourceExtensions();
		if(tmp != null && tmp.length != 0){
			setAttribute(el, EXTENSIONS, CDataUtil.arrayToString(tmp, DELIMITER));
		}
		
		int kinds = data.getSupportedEntryKinds();
		int[] allKinds = KindBasedStore.getLanguageEntryKinds();
		StringBuffer buf = new StringBuffer();
		boolean found = false;
		for(int i = 0; i < allKinds.length; i++){
			if((allKinds[i] & kinds) != 0){
				if(found)
					buf.append(DELIMITER);
				found = true;
				buf.append(LanguageSettingEntriesSerializer.kindToString(allKinds[i]));
					
			}
		}
		
		if(found){
			el.setAttribute(SUPPORTED_ENTRY_KINDS, buf.toString());
		}
			
		ICStorageElement child = el.createChild(SETTING_ENTRIES);
		storeEntries(data, child);
	}
	
	public void storeEntries(CLanguageData lData, ICStorageElement el){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		ICLanguageSettingEntry[] sortedEntries;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			sortedEntries = lData.getEntries(kind);
			if(sortedEntries != null && sortedEntries.length != 0){
				LanguageSettingEntriesSerializer.serializeEntries(sortedEntries, el);
			}
		}
	}
}
