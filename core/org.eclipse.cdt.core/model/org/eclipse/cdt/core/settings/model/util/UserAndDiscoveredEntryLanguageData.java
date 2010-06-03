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
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;

public abstract class UserAndDiscoveredEntryLanguageData extends
		EntryStorageBasedLanguageData {
	private KindBasedStore<Set<String>> fDisabledNameSetStore;
	
	public UserAndDiscoveredEntryLanguageData() {
		super();
	}

	public UserAndDiscoveredEntryLanguageData(String id, CLanguageData base) {
		super(id, base);
	}

	@Override
	protected void copySettingsFrom(CLanguageData data) {
		super.copySettingsFrom(data);
		
		if(data instanceof UserAndDiscoveredEntryLanguageData){
			UserAndDiscoveredEntryLanguageData lData = (UserAndDiscoveredEntryLanguageData)data;
			if(lData.fDisabledNameSetStore != null){
				@SuppressWarnings("unchecked")
				KindBasedStore<Set<String>> clone = (KindBasedStore<Set<String>>) lData.fDisabledNameSetStore.clone();
				fDisabledNameSetStore = clone;
				int kinds[] = KindBasedStore.getLanguageEntryKinds();
				int kind;
				Set<String> set;
				for(int i = 0; i < kinds.length; i++){
					kind = kinds[i];
					set = fDisabledNameSetStore.get(kind);
					if(set != null){
						set = new HashSet<String>(set);
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

		@Override
		protected ICLanguageSettingEntry[] getDiscoveredEntries(
				Set<String> disabledNameSet) {
			return fLangData.getDiscoveredEntries(getKind(), disabledNameSet);
		}

		@Override
		protected ICLanguageSettingEntry[] getUserEntries() {
			return fLangData.getUserEntries(getKind());
		}

		@Override
		protected void setDisabledDiscoveredNames(Set<String> disabledNameSet) {
			fLangData.setDisabledDiscoveredNames(getKind(), disabledNameSet);
		}

		@Override
		protected void setUserEntries(ICLanguageSettingEntry[] entries) {
			fLangData.setUserEntries(getKind(), entries);
		}

		@Override
		protected boolean canDisableDiscoveredEntries() {
			return fLangData.canDisableDiscoveredEntries(getKind());
		}
	}
	
	@Override
	protected AbstractEntryStorage getStorage(int kind) {
		return new UserAndDiscoveredEntryLanguageDataEntryStorage(kind, this);
	}

	protected ICLanguageSettingEntry[] getDiscoveredEntries(int kind,
			Set<String> disabledNameSet){
		ICLanguageSettingEntry[] entries = getAllDiscoveredEntries(kind);
		Set<String> set = getDisabledSet(kind);
		if(set != null && set.size() != 0){
			disabledNameSet.addAll(set);
		}
		return entries;
	}
	
	protected void removeInexistent(ICLanguageSettingEntry[] entries, Set<String> set){
		Set<String> copy = new HashSet<String>(set);
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

	protected void setDisabledDiscoveredNames(int kind, Set<String> disabledNameSet){
		setDisabledSet(kind, disabledNameSet != null ? new HashSet<String>(disabledNameSet) : null);
	}
	
	protected Set<String> getDisabledSet(int kind){
		if(fDisabledNameSetStore != null){
			return fDisabledNameSetStore.get(kind);
		}
		return null;
	}
	
	protected void setDisabledSet(int kind, Set<String> set){
		if(set == null || set.size() == 0){
			if(fDisabledNameSetStore != null){
				fDisabledNameSetStore.put(kind, null);
			}
		} else {
			if(fDisabledNameSetStore == null)
				fDisabledNameSetStore = new KindBasedStore<Set<String>>();
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

	@Override
	protected ICLanguageSettingEntry[] getEntriesToCopy(int kind,
			CLanguageData data) {
		return ((UserAndDiscoveredEntryLanguageData)data).getEntriesFromStore(kind);
	}
}
