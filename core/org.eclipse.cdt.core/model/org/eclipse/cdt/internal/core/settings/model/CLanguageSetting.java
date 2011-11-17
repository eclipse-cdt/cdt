/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryStore;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;

public class CLanguageSetting extends CDataProxy implements
		ICLanguageSetting {

	CLanguageSetting(CLanguageData data, CDataProxyContainer parent, CConfigurationDescription cfg) {
		super(data, parent, cfg);
	}

	@Override
	public final int getType() {
		return ICSettingBase.SETTING_LANGUAGE;
	}

//	public IContentType getHeaderContentType() {
//		CLanguageData data = getCLanguageData(false);
//		return data.getHeaderContentType();
//	}

	@Override
	public String getLanguageId() {
		CLanguageData data = getCLanguageData(false);
		return data.getLanguageId();
	}

	@Override
	public void setLanguageId(String id){
		CLanguageData data = getCLanguageData(true);
		data.setLanguageId(id);
	}

	private CLanguageData getCLanguageData(boolean write){
		return (CLanguageData)getData(write);
	}

//TODO:	public ICLanguageSettingEntry[] getSettingEntries() {
//		return getSettingEntries(ICLanguageSettingEntry.ALL);
//	}

	@Override
	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
		CLanguageData data = getCLanguageData(false);
		return data.getEntries(kind);
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
		CLanguageData data = getCLanguageData(false);
		ICLanguageSettingEntry entries[] = data.getEntries(kind);
		if (entries!=null)
			return new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
		return new ArrayList<ICLanguageSettingEntry>();
	}

	@Override
	public String[] getSourceContentTypeIds() {
		CLanguageData data = getCLanguageData(false);
		String ids[] = data.getSourceContentTypeIds();
		if(ids != null)
			return ids;
		return CDefaultLanguageData.EMPTY_STRING_ARRAY;
	}

	@Override
	public int getSupportedEntryKinds() {
		CLanguageData data = getCLanguageData(false);
		return data.getSupportedEntryKinds();
	}

	@Override
	public boolean supportsEntryKind(int kind) {
		return (getSupportedEntryKinds() & kind) == kind;
	}

/*	public String[] getHeaderExtensions() {
		CLanguageData data = getCLanguageData(false);
		IContentType type = data.getHeaderContentType();
		String[] exts;
		if(type != null) {
			exts = getContentTypeFileSpecs(type);
		} else {
			exts = data.getHeaderExtensions();
			if(exts != null)
				exts = (String[])exts.clone();
			else
				exts = new String[0];
		}

		return exts;
	}
*/
	@Override
	public String[] getSourceExtensions() {
		CLanguageData data = getCLanguageData(false);
		return CDataUtil.getSourceExtensions(getProject(), data);
//		String[] exts = null;
//		String[] typeIds = data.getSourceContentTypeIds();
//		if(typeIds != null && typeIds.length != 0){
//			exts = CProjectDescriptionManager.getInstance().getExtensionsFromContentTypes(getProject(), typeIds);
//		} else {
//			exts = data.getSourceExtensions();
//			if(exts != null && exts.length != 0)
//				exts = (String[])exts.clone();
//			else
//				exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
//		}
//
//		if(exts == null)
//			exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
//		return exts;
	}

/*
	private Map fillNameToEntryMap(ICLanguageSettingEntry entries[], Map map){
		if(map == null)
			map = new HashMap();

		for(int i = 0; i < entries.length; i++){
			ICLanguageSettingEntry entry = entries[i];
			map.put(entry.getName(), entry);
		}
		return map;
	}


	private class SettingChangeInfo implements ICSettingsChangeInfo {
		CLanguageData fData;
		ICLanguageSettingEntry fNewEntries[];
		int fKind;
		ICLanguageSettingEntryInfo fAddedInfo[];
		ICLanguageSettingEntry fRemoved[];

		SettingChangeInfo(int kind, ICLanguageSettingEntry newEntries[], CLanguageData data){
			fNewEntries = newEntries;
			fData = data;
			fKind = kind;
		}

		SettingChangeInfo(int kind, ICLanguageSettingEntryInfo addedEntriesInfo[], ICLanguageSettingEntry removed[], CLanguageData data){
			fAddedInfo = addedEntriesInfo;
			fRemoved = removed;
			fData = data;
			fKind = kind;
		}

		public ICLanguageSettingEntryInfo[] getAddedEntriesInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		public ICLanguageSettingEntry[] getEntries() {
			if(fNewEntries == null){
				ICLanguageSettingEntry oldEntries[] = fData.getSettingEntries(fKind);
				List list = new ArrayList();
				for(int i = 0; i < oldEntries.length; i++){
					ICLanguageSettingEntry entry = oldEntries[i];
					if(entry.getKind() != fKind)
						continue;

					list.add(entry);
				}


			}
			return fNewEntries;
		}

		public int getKind() {
			return fKind;
		}

		public ICLanguageSettingEntry[] getRemovedEntries() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void changeEntries(ICLanguageSettingEntryInfo[] added, ICLanguageSettingEntry[] removed) {
		CLanguageData data = getCLanguageData(true);
		Map map = null;
		if(added != null && added.length > 0){
			map = sortEntries(added, true, map);
		}
		if(removed != null && removed.length > 0){
			map = sortEntries(removed, false, map);
		}

		if(map != null){
			for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				int kind = ((Integer)entry.getKey()).intValue();
				List lists[] = (List[])entry.getValue();
				List aList = lists[0];
				List rList = lists[1];
				ICLanguageSettingEntry sortedAdded[] = aList != null ?
						(ICLanguageSettingEntry[])aList.toArray(new ICLanguageSettingEntry[aList.size()])
							: null;
				ICLanguageSettingEntry sortedRemoved[] = rList != null ?
						(ICLanguageSettingEntry[])rList.toArray(new ICLanguageSettingEntry[rList.size()])
							: null;

				data.changeEntries(kind, sortedAdded, sortedRemoved);
			}
		}
	}

	private Map sortEntries(ICLanguageSettingEntry entries[], boolean added, Map map){
		if(map == null)
			map = new HashMap();

		int index = added ? 0 : 1;
		for(int i = 0; i < entries.length; i++){
			ICLanguageSettingEntry entry = entries[i];
			if(entry != null){
				Integer iKind = new Integer(entry.getKind());
				List[] addedRemovedListArr = (List[])map.get(iKind);
				if(addedRemovedListArr == null){
					addedRemovedListArr = new List[2];
					map.put(iKind, addedRemovedListArr);
				}
				List list = addedRemovedListArr[index];
				if(list == null){
					list = new ArrayList();
					addedRemovedListArr[index] = list;
				}
				list.add(entry);
			}
		}
		return map;
	}
*/
	@Override
	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		CLanguageData data = getCLanguageData(true);
		EntryStore store = new EntryStore();
//		KindBasedStore nameSetStore = new KindBasedStore();
		int eKind;
		if(entries != null){
			if(entries.length != 0){
				for(int i = 0; i < entries.length; i++){
					ICLanguageSettingEntry entry = entries[i];
					eKind = entry.getKind();
					if((kind & eKind) != 0 && (data.getSupportedEntryKinds() & eKind) != 0){
						store.addEntry(entry);
					}
				}
			} else {
				int kinds[] = KindBasedStore.getLanguageEntryKinds();
				for(int i = 0; i < kinds.length; i++){
					if((kinds[i] & kind) != 0){
						store.storeEntries(kinds[i], new ICLanguageSettingEntry[0]);
					}
				}
			}
		}

		setSettingEntries(kind, data, store);
	}

	private int[] flagsToArray(int flags){
		int arr[] = new int[32];
		int num = 0;
		for(int i = 1; i != 0; i = i << 1){
			if((flags & i) != 0)
				arr[num++] = i;
		}
		if(num == arr.length)
			return arr;
		else if(num == 0)
			return new int[0];
		int result[] = new int[num];
		System.arraycopy(arr, 0, result, 0, num);
		return result;
	}

	@Override
	public void setSettingEntries(int kind, List<ICLanguageSettingEntry> list) {
		CLanguageData data = getCLanguageData(true);
		EntryStore store = new EntryStore();
//		KindBasedStore nameSetStore = new KindBasedStore();
		int eKind;

		if(list != null){
			if(list.size() != 0){
				for(ICLanguageSettingEntry entry : list){
					eKind = entry.getKind();
					if((kind & eKind) != 0 && (data.getSupportedEntryKinds() & eKind) != 0){
						store.addEntry(entry);
					}
				}
			} else {
				int kinds[] = KindBasedStore.getLanguageEntryKinds();
				for(int k : kinds){
					if((k & kind) != 0){
						store.storeEntries(k, new ICLanguageSettingEntry[0]);
					}
				}
			}
		}

		setSettingEntries(kind, data, store);
	}

	private void setSettingEntries(int kind, CLanguageData data, EntryStore store){
		int oredk = getSupportedEntryKinds();
		int kinds[] = flagsToArray(oredk);

//		int kinds[] = KindBasedStore.getSupportedKinds();
		for(int i = 0; i < kinds.length; i++){
			ICLanguageSettingEntry sortedEntries[] = store.containsEntriesList(kinds[i]) ? store.getEntries(kinds[i]) : null;
			if((kind & kinds[i]) != 0){
				data.setEntries(kinds[i], sortedEntries);
				if(sortedEntries == null)
					CExternalSettingsManager.getInstance().restoreDefaults(this, kind);
			}
		}
	}

/*	private boolean shouldAdd(ICLanguageSettingEntry entry){
		int kind = entry.getKind();
		Set set = (Set)store.get(kind);
		if(set == null){
			set = new HashSet();
			store.put(kind, set);
		}
		return set.add(entry.getName());
	}
*/
//TODO:	public ICLanguageSettingEntry[] getResolvedSettingEntries() {
		// TODO Auto-generated method stub
//		return getSettingEntries();
//	}

	@Override
	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		ICLanguageSettingEntry entries[] = getSettingEntries(kind);
		entries = CDataUtil.resolveEntries(entries, getConfiguration());
		return entries;
	}

	@Override
	public void setSourceContentTypeIds(String[] ids) {
		CLanguageData data = getCLanguageData(true);

		data.setSourceContentTypeIds(ids);
	}

	@Override
	public void setSourceExtensions(String[] exts) {
		CLanguageData data = getCLanguageData(true);

		data.setSourceExtensions(exts);
	}

}
