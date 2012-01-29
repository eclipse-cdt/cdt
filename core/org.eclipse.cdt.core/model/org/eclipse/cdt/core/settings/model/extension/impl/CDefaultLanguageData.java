/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension.impl;

import java.util.Arrays;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryStore;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.internal.core.settings.model.CLanguageSettingCache;

public class CDefaultLanguageData extends CLanguageData {
	@Deprecated /** not used anymore */
	protected final static int OP_COPY = 1;
	@Deprecated /** not used anymore */
	protected final static int OP_SET = 2;

	protected String fName;
	protected String fId;
	protected String fLanguageId;
	protected int fSupportedKinds;
	protected String fSourceContentTypeIds[];
	protected String fSourceExts[];
//	protected IContentType fHeaderContentType;
//	protected String fHeaderExts[];
	protected EntryStore fStore;
	public final static String[] EMPTY_STRING_ARRAY = new String[0];
//	protected CConfigurationData fCfg;
//	protected CResourceData fRcData;
//	private CDataFacroty fFactory;
	protected boolean fIsModified;


//	public CDefaultLanguageData(CConfigurationData cfg, CResourceData rcData, CDataFacroty factory) {
//		fCfg = cfg;
//		fRcData = rcData;
//		if(factory == null)
//			factory = new CDataFacroty();
//		fFactory = factory;
//	}

	protected CDefaultLanguageData(){
		fStore = createStore();
	}

	public CDefaultLanguageData(String id,
			String languageId,
			String ids[],
			boolean isContentTypes) {
		fId = id;
		fLanguageId = languageId;

		if(isContentTypes)
			fSourceContentTypeIds = ids.clone();
		else
			fSourceExts = ids.clone();

		fStore = createStore();
	}


	public CDefaultLanguageData(String id, CLanguageData base) {
		fId = id;
		copySettingsFrom(base);
	}

	protected void copySettingsFrom(CLanguageData data){
		fName = data.getName();
		fLanguageId = data.getLanguageId();
		fSupportedKinds = data.getSupportedEntryKinds();
		fSourceContentTypeIds = data.getSourceContentTypeIds();
		fSourceExts = data.getSourceExtensions();
//		fHeaderContentType = data.getHeaderContentType();
//		fHeaderExts = data.getHeaderExtensions();
		fStore = createStore(data);
	}

	protected EntryStore createStore(CLanguageData data){
		EntryStore store = createStore();
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for (int kind : kinds) {
			ICLanguageSettingEntry entries[] = getEntriesToCopy(kind, data);
			entries = processStoredEntries(entries, OP_COPY);
			store.storeEntries(kind, entries);

		}
		return store;
	}

	protected ICLanguageSettingEntry[] getEntriesToCopy(int kind, CLanguageData lData){
		return lData.getEntries(kind);
	}

	/**
	 * This method is overridden in {@link CLanguageSettingCache} to ensure the entries are cached with {@link CDataUtil} pool.
	 */
	protected ICLanguageSettingEntry[] processStoredEntries(ICLanguageSettingEntry[] entries, int op){
		return entries;
	}

	protected EntryStore createStore(){
		return new EntryStore(true);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public boolean isValid() {
		return getId() != null;
	}

	@Override
	public String getLanguageId() {
		return fLanguageId;
	}

	@Override
	public void setLanguageId(String id) {
		if(CDataUtil.objectsEqual(id, fLanguageId))
			return;

		fLanguageId = id;

		setModified(true);
	}

//	public IContentType getHeaderContentType() {
//		return fHeaderContentType;
//	}

//	public String[] getHeaderExtensions() {
//		return fHeaderExts;
//	}
/*
	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
		return fStore.getEntries(kind);
		List list = new ArrayList();

		if((kinds & ICLanguageSettingEntry.INCLUDE_PATH) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.INCLUDE_PATH, list);
		} else if((kinds & ICLanguageSettingEntry.INCLUDE_FILE) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.INCLUDE_FILE, list);
		} else if((kinds & ICLanguageSettingEntry.MACRO) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.MACRO, list);
		} else if((kinds & ICLanguageSettingEntry.MACRO_FILE) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.MACRO_FILE, list);
		} else if((kinds & ICLanguageSettingEntry.LIBRARY_PATH) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.LIBRARY_PATH, list);
		} else if((kinds & ICLanguageSettingEntry.LIBRARY_FILE) != 0) {
			addLanguageEntries(ICLanguageSettingEntry.LIBRARY_FILE, list);
		}

		return (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);

	}

	private List addLanguageEntries(int kind, List list){
		ICLanguageSettingEntry entries[] = fStore.getEntries(kind);
		for(int i = 0; i < entries.length; i++){
			list.add(entries[i]);
		}
		return list;
	}
*/

	@Override
	public String[] getSourceContentTypeIds() {
		if(fSourceContentTypeIds != null)
			return fSourceContentTypeIds;
		return EMPTY_STRING_ARRAY;
	}

	@Override
	public String[] getSourceExtensions() {
		if(fSourceExts != null)
			return fSourceExts;
		return EMPTY_STRING_ARRAY;
	}

	@Override
	public int getSupportedEntryKinds() {
		return fSupportedKinds;
	}

	@Override
	public void setEntries(int kind, ICLanguageSettingEntry entries[]) {
		entries = processStoredEntries(entries, OP_SET);
		fStore.storeEntries(kind, entries);

		setModified(true);
	}

	@Override
	public ICLanguageSettingEntry[] getEntries(int kind) {
		return fStore.getEntries(kind);
	}

	@Override
	public void setSourceContentTypeIds(String[] ids) {
		if(Arrays.equals(ids, fSourceContentTypeIds))
			return;

		fSourceContentTypeIds = ids != null ?
				(String[])ids.clone() : null;

		setModified(true);
	}

	@Override
	public void setSourceExtensions(String[] exts) {
		if(Arrays.equals(exts, fSourceExts))
			return;

		fSourceExts = exts != null ?
				(String[])exts.clone() : null;

		setModified(true);
	}

	public boolean isModified(){
		return fIsModified;
	}

	public void setModified(boolean modified){
		fIsModified = modified;
	}

}
