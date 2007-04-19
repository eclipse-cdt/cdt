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
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.settings.model.util.CEntriesSet;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;

public final class CExternalSetting implements ICExternalSetting {
//	private EntryStore fEntryStore = new EntryStore();
	private KindBasedStore fStore = new KindBasedStore(false);
	private String[] fContentTypeIds;
	private String[] fLanguageIds;
	private String[] fExtensions;
//	private String fId;

	public CExternalSetting(ICExternalSetting base){
		fLanguageIds = base.getCompatibleLanguageIds();
		fContentTypeIds = base.getCompatibleContentTypeIds();
		fExtensions = base.getCompatibleExtensions();
		
//		fEntryStore = new EntryStore();
		initEntryStore(base.getEntries());
	}

	public CExternalSetting(ICExternalSetting base, ICSettingEntry entries[]){
		this(base);
		
		initEntryStore(entries);
	}

	public CExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICSettingEntry[] entries){
		if(languageIDs != null)
			fLanguageIds = (String[])languageIDs.clone();
		if(contentTypeIds != null)
			fContentTypeIds = (String[])contentTypeIds.clone();
		if(extensions != null)
			fExtensions = (String[])extensions.clone();
		
		initEntryStore(entries);
	}
	
	private void initEntryStore(ICSettingEntry entries[]){
		ICSettingEntry entry;
		for(int i = 0; i < entries.length; i++){
			entry = entries[i];
			
			addEntry(entry);
		}
		
//		trimToSize();
	}
	
	private void addEntry(ICSettingEntry entry){
		getEntriesSet(entry.getKind(), true).addEntry(entry);
	}
	
/*	private void trimToSize(){
		int kinds[] = KindBasedStore.getSupportedKinds();
		for(int i = 0; i < kinds.length; i++){
			CEntriesSet set = getEntriesSet(kinds[i], false);
			if(set != null)
				set.trimToSize();
		}
	}
*/	
	private CEntriesSet getEntriesSet(int kind, boolean create){
		CEntriesSet set = (CEntriesSet)fStore.get(kind);
		if(set == null && create){
			set = new CEntriesSet();
			fStore.put(kind, set);
		}
		return set;
	}

	public String[] getCompatibleContentTypeIds() {
		if(fContentTypeIds != null)
			return (String[])fContentTypeIds.clone();
		return null;
	}

	public String[] getCompatibleExtensions() {
		if(fExtensions != null)
			return (String[])fExtensions.clone();
		return null;
	}

	public String[] getCompatibleLanguageIds() {
		if(fLanguageIds != null)
			return (String[])fLanguageIds.clone();
		return null;
	}

	public ICSettingEntry[] getEntries(int kind) {
		CEntriesSet set = getEntriesSet(kind, false);
		if(set != null)
			return set.toArray();
		return new ICSettingEntry[0];
	}

//	public String getId(){
//		return fId;
//	}

	public ICSettingEntry[] getEntries() {
		List result = new ArrayList();
		int kinds[] = KindBasedStore.getAllEntryKinds();
		for(int i = 0; i < kinds.length; i++){
			CEntriesSet list = getEntriesSet(kinds[i], false);
			if(list != null)
				result.addAll(Arrays.asList(list.toArray()));
		}
		
		return (ICSettingEntry[])result.toArray(new ICSettingEntry[result.size()]);
	}
}
