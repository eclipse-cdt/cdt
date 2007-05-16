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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;

public abstract class UserAndDiscoveredEntryLanguageData extends
		EntryStorageBasedLanguageData {
	private KindBasedStore fDisabledNameSetStore;
	
	public UserAndDiscoveredEntryLanguageData() {
		super();
	}

	public UserAndDiscoveredEntryLanguageData(String id, CLanguageData base) {
		super(id, base);
	}

	protected void copySettingsFrom(CLanguageData data) {
		super.copySettingsFrom(data);
		
		if(data instanceof UserAndDiscoveredEntryLanguageData){
			UserAndDiscoveredEntryLanguageData lData = (UserAndDiscoveredEntryLanguageData)data;
			if(lData.fDisabledNameSetStore != null){
				fDisabledNameSetStore = (KindBasedStore)lData.fDisabledNameSetStore.clone();
				int kinds[] = KindBasedStore.getLanguageEntryKinds();
				int kind;
				Set set;
				for(int i = 0; i < kinds.length; i++){
					kind = kinds[i];
					set = (Set)fDisabledNameSetStore.get(kind);
					if(set != null){
						set = new HashSet(set);
						fDisabledNameSetStore.put(kind, set);
					}
				}
			}
		}
	}

	public UserAndDiscoveredEntryLanguageData(String id, String languageId,
			String[] ids, boolean isContentTypes) {
		super(id, languageId, ids, isContentTypes);
	}

	public static class UserAndDiscoveredEntryLanguageDataEntryStorage extends UserAndDiscoveredEntryStorage {
		private UserAndDiscoveredEntryLanguageData fLangData;
		public UserAndDiscoveredEntryLanguageDataEntryStorage(int kind, UserAndDiscoveredEntryLanguageData lData) {
			super(kind);
			fLangData = lData;
		}

		protected ICLanguageSettingEntry[] getDiscoveredEntries(
				Set disabledNameSet) {
			return fLangData.getDiscoveredEntries(getKind(), disabledNameSet);
		}

		protected ICLanguageSettingEntry[] getUserEntries() {
			return fLangData.getUserEntries(getKind());
		}

		protected void setDisabledDiscoveredNames(Set disabledNameSet) {
			fLangData.setDisabledDiscoveredNames(getKind(), disabledNameSet);
		}

		protected void setUserEntries(ICLanguageSettingEntry[] entries) {
			fLangData.setUserEntries(getKind(), entries);
		}

		protected boolean canDisableDiscoveredEntries() {
			return fLangData.canDisableDiscoveredEntries(getKind());
		}
	}
	
	protected AbstractEntryStorage getStorage(int kind) {
		return new UserAndDiscoveredEntryLanguageDataEntryStorage(kind, this);
	}

	protected ICLanguageSettingEntry[] getDiscoveredEntries(int kind,
			Set disabledNameSet){
		ICLanguageSettingEntry[] entries = getAllDiscoveredEntries(kind);
		Set set = getDisabledSet(kind);
		if(set != null && set.size() != 0){
			disabledNameSet.addAll(set);
		}
		return entries;
	}
	
	protected void removeInexistent(ICLanguageSettingEntry[] entries, Set set){
		Set copy = new HashSet(set);
		for(int i = 0; i < entries.length; i++){
			copy.remove(entries[i].getName());
		}
		
		if(copy.size() != 0){
			set.removeAll(copy);
		}
	}

	protected ICLanguageSettingEntry[] getUserEntries(int kind) {
		return getEntriesFromStore(kind);
	}

	protected void setDisabledDiscoveredNames(int kind, Set disabledNameSet){
		setDisabledSet(kind, disabledNameSet != null ? new HashSet(disabledNameSet) : null);
	}
	
	protected Set getDisabledSet(int kind){
		if(fDisabledNameSetStore != null){
			return (Set)fDisabledNameSetStore.get(kind);
		}
		return null;
	}
	
	protected void setDisabledSet(int kind, Set set){
		if(set == null || set.size() == 0){
			if(fDisabledNameSetStore != null){
				fDisabledNameSetStore.put(kind, null);
			}
		} else {
			if(fDisabledNameSetStore == null)
				fDisabledNameSetStore = new KindBasedStore();
			fDisabledNameSetStore.put(kind, set);
		}
	}

	protected abstract ICLanguageSettingEntry[] getAllDiscoveredEntries(int kind);
	
	protected void setUserEntries(int kind, ICLanguageSettingEntry[] entries) {
		setEntriesToStore(kind, entries);
	}
	
	protected boolean canDisableDiscoveredEntries(int kind) {
		return true;
	}

	protected ICLanguageSettingEntry[] getEntriesToCopy(int kind,
			CLanguageData data) {
		return ((UserAndDiscoveredEntryLanguageData)data).getEntriesFromStore(kind);
	}
}
