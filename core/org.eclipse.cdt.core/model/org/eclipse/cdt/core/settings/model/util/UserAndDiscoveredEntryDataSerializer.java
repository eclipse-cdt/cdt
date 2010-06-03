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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;

public class UserAndDiscoveredEntryDataSerializer extends CDataSerializer {
	protected static final String DISABLED_DISCOVERED_ENTRIES = "disabledDiscoveredEntries"; //$NON-NLS-1$
	protected static final String KIND = "kind"; //$NON-NLS-1$
	protected static final String VALUE = "value"; //$NON-NLS-1$

	private static UserAndDiscoveredEntryDataSerializer fInstance;

	public static CDataSerializer getDefault(){
		if(fInstance == null)
			fInstance = new UserAndDiscoveredEntryDataSerializer();
		return fInstance;
	}

	@Override
	public void loadEntries(CLanguageData data, ICStorageElement el) {
		UserAndDiscoveredEntryLanguageData lData = (UserAndDiscoveredEntryLanguageData)data;

		List<ICSettingEntry> entries = LanguageSettingEntriesSerializer.loadEntriesList(el);
		EntryStore store = new EntryStore();
		store.addEntries(entries.toArray(new ICLanguageSettingEntry[entries.size()]));
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		ICLanguageSettingEntry[] sortedEntries;
		for (int kind : kinds) {
			if(store.containsEntriesList(kind)){
				sortedEntries = store.getEntries(kind);
				lData.setEntriesToStore(kind, sortedEntries);
			}
		}

		ICStorageElement[] children = el.getChildren();
		for (ICStorageElement child : children) {
			String name = child.getName();
			if(DISABLED_DISCOVERED_ENTRIES.equals(name)){
				loadDisabledEntriesInfo(lData, child);
			}
		}
	}

	protected void loadDisabledEntriesInfo(UserAndDiscoveredEntryLanguageData lData, ICStorageElement el){
		ICStorageElement[] children = el.getChildren();
		for (ICStorageElement child : children) {
			String name = child.getName();
			if(NAME.equals(name)){
				String tmp = child.getAttribute(KIND);
				int kind = LanguageSettingEntriesSerializer.stringToKind(tmp);
				if(kind != 0){
					tmp = child.getAttribute(VALUE);
					if(tmp != null){
						Set<String> set = lData.getDisabledSet(kind);
						if(set == null)
							set = new HashSet<String>();
						set.add(tmp);
						lData.setDisabledSet(kind, set);
					}
				}
			}
		}
	}

	@Override
	public void storeEntries(CLanguageData data, ICStorageElement el) {
		UserAndDiscoveredEntryLanguageData lData = (UserAndDiscoveredEntryLanguageData)data;
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		ICLanguageSettingEntry[] sortedEntries;
		for (int kind : kinds) {
			sortedEntries = lData.getEntriesFromStore(kind);
			if(sortedEntries != null && sortedEntries.length != 0){
				LanguageSettingEntriesSerializer.serializeEntries(sortedEntries, el);
			}
		}

		ICStorageElement disabledNamesEl = el.createChild(DISABLED_DISCOVERED_ENTRIES);

		storeDisabledEntriesInfo(lData, disabledNamesEl);
	}

	protected void storeDisabledEntriesInfo(UserAndDiscoveredEntryLanguageData lData, ICStorageElement el){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for (int kind : kinds) {
			Set<String> set = lData.getDisabledSet(kind);
			if(set != null && set.size() != 0){
				for (String string : set) {
					ICStorageElement child = el.createChild(NAME);
					child.setAttribute(KIND, LanguageSettingEntriesSerializer.kindToString(kind));
					child.setAttribute(VALUE, string);
				}
			}
		}
	}

}