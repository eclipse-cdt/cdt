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
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.IKindBasedInfo;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ProfileInfoProvider.DiscoveredEntry;

public class BuildLanguageData extends CLanguageData {
	private ITool fTool;
	private IInputType fInputType;
	private KindBasedStore fKindToOptionArrayStore = new KindBasedStore();
	private KindBasedStore fKindToUndefOptionArrayStore = new KindBasedStore();
	private static final IOption[] EMPTY_OPTION_ARRAY = new IOption[0];
	private boolean fOptionStoreInited;
//	private Map fKindToEntryArrayMap = new HashMap();
	private ProfileInfoProvider fDiscoveredInfo;
	private KindBasedStore fKindToEntryStore = new KindBasedStore();
	private String fId;
	

	public BuildLanguageData(ITool tool, IInputType inType){
		fTool = tool;
		if(inType != null){ 
//			inType = tool.getEdtableInputType(inType);
			fInputType = inType;
			if(inType.getParent() != tool)
				throw new IllegalArgumentException();
//			IInputType extType = inType;
//			for(;extType != null && !extType.isExtensionElement(); extType = extType.getSuperClass());
//			String typeId;
//			if(extType != null)
//				typeId = extType.getId();
//			else
//				typeId = inType.getId();
			fId = inType.getId();//new StringBuffer(fTool.getId()).append(".").append(typeId).toString(); //$NON-NLS-1$
		} else {
			fInputType = null;
			fId = new StringBuffer(fTool.getId()).append(".").append("languagedata").toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		fDiscoveredInfo = new ProfileInfoProvider(this);
	}
	
	private void obtainEditableInputType(){
		if(fInputType != null){
			IInputType old = fInputType;
			fInputType = fTool.getEdtableInputType(fInputType);
			if(old != fInputType){
				fDiscoveredInfo.checkUpdateInputType(fInputType);
			}
		}
	}

	public void setEntries(int kind, ICLanguageSettingEntry entries[]) {
		EntryStorage storage = getEntryStorage(kind);
		if(storage != null)
			storage.setEntries(entries);
	}
	
	private EntryStorage getEntryStorage(int kind){
		if(getOptionsForKind(kind).length == 0 && isToolChainDiscoveryProfile())
			return null;
			
		EntryStorage starage = (EntryStorage)fKindToEntryStore.get(kind);
		if(starage == null){
			starage = new EntryStorage(kind, this);
			fKindToEntryStore.put(kind, starage);
		}
		return starage;
	}
	
	private void notifyOptionsChangeForKind(int kind){
		fOptionStoreInited = false;
		EntryStorage storage = getEntryStorage(kind);
		if(storage != null)
			storage.optionsChanged();
	}
	
	public void optionsChanged(int type){
		int kind = ManagedBuildManager.optionTypeToEntryKind(type);
		if(kind == 0)
			kind = ManagedBuildManager.optionUndefTypeToEntryKind(type);
		
		if(kind != 0)
			notifyOptionsChangeForKind(kind);
	}
	
	private ProfileInfoProvider getDiscoveredInfoProvider(){
		return fDiscoveredInfo;
	}
/*	
	private String getOptionValueFromEntry(ICLanguageSettingEntry entry){
		String optValue = entry.getName();
		if(entry.getKind() == ICLanguageSettingEntry.MACRO){
			String macroValue = entry.getValue();
			StringBuffer buf = new StringBuffer(optValue).append('=').append(macroValue);
			optValue = buf.toString();
		}
		return optValue;
		
	}
*/
	public String getLanguageId() {
		return fInputType != null ? fInputType.getLanguageId(fTool) : null;
	}

	public ICLanguageSettingEntry[] getEntries(int kinds) {
		List list = new ArrayList();
		
		if((kinds & ICLanguageSettingEntry.INCLUDE_PATH) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.INCLUDE_PATH);
			if(storage != null)
				storage.getEntries(list);
		} else if((kinds & ICLanguageSettingEntry.INCLUDE_FILE) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.INCLUDE_FILE);
			if(storage != null)
				storage.getEntries(list);
		} else if((kinds & ICLanguageSettingEntry.MACRO) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.MACRO);
			if(storage != null)
				storage.getEntries(list);
		} else if((kinds & ICLanguageSettingEntry.MACRO_FILE) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.MACRO_FILE);
			if(storage != null)
				storage.getEntries(list);
		} else if((kinds & ICLanguageSettingEntry.LIBRARY_PATH) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.LIBRARY_PATH);
			if(storage != null)
				storage.getEntries(list);
		} else if((kinds & ICLanguageSettingEntry.LIBRARY_FILE) != 0) {
			EntryStorage storage = getEntryStorage(ICLanguageSettingEntry.LIBRARY_FILE);
			if(storage != null)
				storage.getEntries(list);
		}

		return (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
	}
	
	public void updateInputType(IInputType type){
		fInputType = type;
	}
	
	public String[] getSourceContentTypeIds() {
		if(fInputType != null){
			return fInputType.getSourceContentTypeIds();
		}
		return null;
	}

	public String[] getSourceExtensions() {
		return fInputType != null ? fInputType.getSourceExtensions(fTool) : fTool.getPrimaryInputExtensions();
	}
/*	
	private List getUserEntryValues(int kind){
		IOption options[] = getOptionsForKind(kind);
		List valueList = new ArrayList();
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			List value = (List)option.getValue();
			valueList.addAll(value);
		}
		return valueList;
	}
*/	
	DiscoveredEntry[] getDiscoveredEntryValues(int kind){
		return getDiscoveredInfoProvider().getEntryValues(kind);
	}
/*
	private List addLanguageEntries(int kind, List list){
		ICLanguageSettingEntry entries[] = getLanguageEntries(kind);
		for(int i = 0; i < entries.length; i++){
			list.add(entries[i]);
		}
		return list;
	}

	private ICLanguageSettingEntry[] getLanguageEntries(int kind){
		Integer iKind = new Integer(kind);
		ICLanguageSettingEntry[] entries = (ICLanguageSettingEntry[])fKindToEntryArrayMap.get(iKind);
		if(entries == null){
			entries = calculateLanguageEntries(kind);
			fKindToEntryArrayMap.put(iKind, entries);
		}
		return entries;
	}
		
	private ICLanguageSettingEntry[] calculateLanguageEntries(int kind){
		List discoveredList = fDiscoveredInfo.getEntryValues(kind);
		List optionValueList = getUserEntryValues(kind);
		List entryList = new ArrayList();
		if(discoveredList != null && optionValueList != null){
			Set set = new HashSet();
			if(optionValueList != null)
				processValues(kind, optionValueList, false, entryList, set);
			if(discoveredList != null)
				processValues(kind, discoveredList, true, entryList, set);
		}
		return (ICLanguageSettingEntry[])entryList.toArray(new ICLanguageSettingEntry[entryList.size()]);
	}
	
	private List processValues(int kind, List valuesList, boolean discovered, List entriesList, Set processedValuesSet){
		for(Iterator iter = valuesList.iterator(); iter.hasNext();){
			String value = (String)iter.next();
			if(processedValuesSet.add(value)){
				ICLanguageSettingEntry entry = createEntry(kind, value, discovered);
				if(entry != null)
					entriesList.add(entry);
			}
		}
		return entriesList;
	}
	
	private ICLanguageSettingEntry createEntry(int kind, String value, boolean discovered){
		ICLanguageSettingEntry entry = null;
		switch (kind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(value, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		case ICLanguageSettingEntry.MACRO:
			int index = value.indexOf('=');
			String macroName;
			String macroValue;
			if(index > 0){
				macroName = value.substring(index);
				macroValue = value.substring(index + 1, value.length());
			} else {
				macroName = value;
				macroValue = EMPTY_STRING;
			}
			entry = new CMacroEntry(macroName, macroValue, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		case ICLanguageSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(value, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		case ICLanguageSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(value, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(value, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			entry = new CLibraryFileEntry(value, discovered ? ICLanguageSettingEntry.BUILTIN | ICLanguageSettingEntry.READONLY : 0);
			break;
		}
		return entry;
		
	}
	
	private String optionPathToEntryValue(String path, boolean discovered) {
		//TODO:
		String result = path;
		if (path != null) { 
			IOptionPathConverter optionPathConverter = fTool.getOptionPathConverter();
			if (null!=optionPathConverter) {
				IPath platformPath = optionPathConverter.convertToPlatformLocation(path, null, null);
				if(platformPath != null)
					result = platformPath.toString();
			} else {
				
			}
		}
		return result;
	}

	
	private String toEntryPathValue(String path, boolean discovered){
		//TODO:
		return path;
	}
*/	
	public int getSupportedEntryKinds() {
		KindBasedStore store = getKindToOptionArrayStore();
		IKindBasedInfo infos[] = store.getContents();
		int kinds = 0;
		for(int i = 0; i < infos.length; i++){
			if(((IOption[])infos[i].getInfo()).length > 0)
				kinds |= infos[i].getKind(); 
		}
		return kinds;
	}
	
	private KindBasedStore getKindToOptionArrayStore(){
		initOptionStores();
		return fKindToOptionArrayStore;
	}

	private void initOptionStores(){
		if(!fOptionStoreInited){
			calculateKindToOptionArrayStore();
			calculateKindToUndefOptionArrayStore();
			fOptionStoreInited = true;
		}
	}

	private KindBasedStore getKindToUndefOptionArrayStore(){
		initOptionStores();
		return fKindToUndefOptionArrayStore;
	}

	private void calculateKindToOptionArrayStore(){
		fKindToOptionArrayStore.clear();
		IOption options[] = fTool.getOptions();
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			try {
				int type = option.getValueType();
				int entryKind = ManagedBuildManager.optionTypeToEntryKind(type);
				if(entryKind != 0){
					getOptionList(fKindToOptionArrayStore, entryKind).add(option);
				}
			} catch (BuildException e) {
			}
		}
		
		IKindBasedInfo infos[] = fKindToOptionArrayStore.getContents();
		IKindBasedInfo info;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			List list = (List)info.getInfo();
			if(list != null){
				IOption[] opts = (IOption[])list.toArray(new IOption[list.size()]);
				info.setInfo(opts);
			} else {
				info.setInfo(EMPTY_OPTION_ARRAY);
			}
		}
	}
	
	private void calculateKindToUndefOptionArrayStore(){
		fKindToUndefOptionArrayStore.clear();
		IOption options[] = fTool.getOptions();
		for(int i = 0; i < options.length; i++){
			IOption option = options[i];
			try {
				int type = option.getValueType();
				int entryKind = ManagedBuildManager.optionUndefTypeToEntryKind(type);
				if(entryKind != 0){
					getOptionList(fKindToUndefOptionArrayStore, entryKind).add(option);
				}
			} catch (BuildException e) {
			}
		}
		
		IKindBasedInfo infos[] = fKindToUndefOptionArrayStore.getContents();
		IKindBasedInfo info;
		for(int i = 0; i < infos.length; i++){
			info = infos[i];
			List list = (List)info.getInfo();
			if(list != null){
				IOption[] opts = (IOption[])list.toArray(new IOption[list.size()]);
				info.setInfo(opts);
			} else {
				info.setInfo(EMPTY_OPTION_ARRAY);
			}
		}
	}


	IOption[] getUndefOptionsForKind(int entryKind){
		KindBasedStore store = getKindToUndefOptionArrayStore();
		return (IOption[])store.get(entryKind);
	}

	IOption[] getOptionsForKind(int entryKind){
		KindBasedStore store = getKindToOptionArrayStore();
		return (IOption[])store.get(entryKind);
	}
	
/*	private IOption[] getOptionsForType(int type){
		Map map = getTypeToOptionArrayMap();
		return (IOption[])map.get(new Integer(type));
		
	}
*/	
	private List getOptionList(KindBasedStore store, int kind){
		List list = (List)store.get(kind);
		if(list == null){
			list = new ArrayList();
			store.put(kind, list);
		}
		return list;
	}

	public void setLanguageId(String id) {
		if(CDataUtil.objectsEqual(id, fInputType.getLanguageId(fTool))){
//			fInputType = fTool.getEdtableInputType(fInputType);
			obtainEditableInputType();
			fInputType.setLanguageIdAttribute(id);
		}
	}

	public String getId() {
		return fId;
	}

	public String getName() {
		String name;
		if(fInputType == null){
			name = fTool.getName();
			if(name == null){
				String[] exts = getSourceExtensions();
				if(exts.length != 0){
					name = CDataUtil.arrayToString(exts, ","); //$NON-NLS-1$
				} else {
					name = fTool.getId();
				}
			}
		} else {
			name = fInputType.getLanguageName(fTool); 
		}
		return name;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setName(String name) {
		// TODO Auto-generated method stub
	}
	
	public ITool getTool(){
		return fTool;
	}
	
	public IInputType getInputType() {
		return fInputType;
	}
	
	boolean isToolChainDiscoveryProfile(){
		return fInputType != null ? 
				((InputType)fInputType).getDiscoveryProfileIdAttribute() == null : 
					true;
	}
	
	String getDiscoveryProfileId(){
		if(fInputType != null)
			return  fInputType.getDiscoveryProfileId(fTool);
		IBuildObject bo = fTool.getParent();
		if(bo instanceof IToolChain)
			return ((IToolChain)bo).getScannerConfigDiscoveryProfileId();
		else if(bo instanceof IResourceInfo){
			IToolChain tCh = ((ResourceConfiguration)bo).getBaseToolChain();
			if(tCh != null)
				return tCh.getScannerConfigDiscoveryProfileId();
		}
		return null;
	}
	
	public IConfiguration getConfiguration(){
		return fTool.getParentResourceInfo().getParent();
	}

	public void setSourceContentTypeIds(String[] ids) {
		String[] headerIds = fInputType.getHeaderContentTypeIds();
		
		List newSrc = new ArrayList(ids.length);
		List newHeaders = new ArrayList(ids.length);
		for(int i = 0; i < ids.length; i++){
			String id = ids[i];
			int j = 0;
			for(; j < headerIds.length; j++){
				if(id.equals(headerIds[j])){
					newHeaders.add(id);
					break;
				}
			}
			if(j == headerIds.length){
				newSrc.add(id);
			}
		}
		
		String newSrcIds[] = (String[])newSrc.toArray(new String[newSrc.size()]);
		String newHeaderIds[] = (String[])newHeaders.toArray(new String[newHeaders.size()]);
		
		if(!Arrays.equals(newSrcIds, fInputType.getSourceContentTypeIds())){
//			fInputType = fTool.getEdtableInputType(fInputType);
			obtainEditableInputType();
			fInputType.setSourceContentTypeIds(newSrcIds);
		}

		if(!Arrays.equals(newHeaderIds, fInputType.getHeaderContentTypeIds())){
//			fInputType = fTool.getEdtableInputType(fInputType);
			obtainEditableInputType();
			fInputType.setHeaderContentTypeIds(newHeaderIds);
		}

	}

	public void setSourceExtensions(String[] exts) {
		// TODO Auto-generated method stub
		
	}
	
	void clearCachedData(){
		fKindToEntryStore.clear(); 
	}

	public boolean containsDiscoveredScannerInfo() {
		IResourceInfo rcInfo = fTool.getParentResourceInfo();
		if(rcInfo instanceof FolderInfo){
			return ((FolderInfo)rcInfo).containsDiscoveredScannerInfo();
		}
		return true;
	}
}
