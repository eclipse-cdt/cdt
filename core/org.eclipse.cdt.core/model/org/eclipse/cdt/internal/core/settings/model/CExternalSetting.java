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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CEntriesSet;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;

public class CExternalSetting implements ICExternalSetting {
	static final String ELEMENT_SETTING_INFO = "externalSetting";
//	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_EXTENSIONS = "extensions";
	private static final String ATTRIBUTE_CONTENT_TYPE_IDS = "contentTypes";
	private static final String ATTRIBUTE_LANGUAGE_IDS = "languages";
//	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String SEPARATOR = ":";

//	private EntryStore fEntryStore = new EntryStore();
	private KindBasedStore fStore = new KindBasedStore();
	private String[] fContentTypeIds;
	private String[] fLanguageIds;
	private String[] fExtensions;
//	private String fId;

	public CExternalSetting(ICStorageElement element){
//		fId = element.getAttribute(ATTRIBUTE_ID);
		String tmp = element.getAttribute(ATTRIBUTE_LANGUAGE_IDS);
		if(tmp != null)
			fLanguageIds = tmp.split(SEPARATOR);
		
		tmp = element.getAttribute(ATTRIBUTE_CONTENT_TYPE_IDS);
		if(tmp != null)
			fContentTypeIds = tmp.split(SEPARATOR);
		
		tmp = element.getAttribute(ATTRIBUTE_EXTENSIONS);
		if(tmp != null)
			fExtensions = tmp.split(ATTRIBUTE_EXTENSIONS);

		ICLanguageSettingEntry entries[] = LanguageSettingEntriesSerializer.loadEntries(element);

		initEntryStore(entries);
	}

	public CExternalSetting(ICExternalSetting base){
		fLanguageIds = base.getCompatibleLanguageIds();
		fContentTypeIds = base.getCompatibleContentTypeIds();
		fExtensions = base.getCompatibleExtensions();
		
//		fEntryStore = new EntryStore();
		initEntryStore(base.getEntries());
	}

	public CExternalSetting(ICExternalSetting base, ICLanguageSettingEntry entries[]){
		this(base);
		
		initEntryStore(entries);
	}

	public CExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICLanguageSettingEntry[] entries){
		if(languageIDs != null)
			fLanguageIds = (String[])languageIDs.clone();
		if(contentTypeIds != null)
			fContentTypeIds = (String[])contentTypeIds.clone();
		if(extensions != null)
			fExtensions = (String[])extensions.clone();
		
		initEntryStore(entries);
	}
	
	private void initEntryStore(ICLanguageSettingEntry entries[]){
		ICLanguageSettingEntry entry;
		for(int i = 0; i < entries.length; i++){
			entry = entries[i];
			
			addEntry(entry);
		}
		
//		trimToSize();
	}
	
	private void addEntry(ICLanguageSettingEntry entry){
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

	public ICLanguageSettingEntry[] getEntries(int kind) {
		CEntriesSet set = getEntriesSet(kind, false);
		if(set != null)
			return set.toArray();
		return new ICLanguageSettingEntry[0];
	}

//	public String getId(){
//		return fId;
//	}

	public ICLanguageSettingEntry[] getEntries() {
		List result = new ArrayList();
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for(int i = 0; i < kinds.length; i++){
			CEntriesSet list = getEntriesSet(kinds[i], false);
			if(list != null)
				result.addAll(Arrays.asList(list.toArray()));
		}
		
		return (ICLanguageSettingEntry[])result.toArray(new ICLanguageSettingEntry[result.size()]);
	}
	
	
	private String composeString(String array[]){
		StringBuffer buf = new StringBuffer(array[0]);
		for(int i = 1; i < array.length; i++){
			buf.append(SEPARATOR).append(array[i]);
		}
		return buf.toString();
	}
	
	public void serialize(ICStorageElement el){
		if(fLanguageIds != null && fLanguageIds.length != 0)
			el.setAttribute(ATTRIBUTE_LANGUAGE_IDS, composeString(fLanguageIds));
		
		if(fContentTypeIds != null && fContentTypeIds.length != 0)
			el.setAttribute(ATTRIBUTE_CONTENT_TYPE_IDS, composeString(fContentTypeIds));

		
		if(fExtensions != null && fExtensions.length != 0)
			el.setAttribute(ATTRIBUTE_EXTENSIONS, composeString(fExtensions));

		LanguageSettingEntriesSerializer.serializeEntries(getEntries(), el);
	}
}
