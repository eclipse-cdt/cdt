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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryStore;
import org.eclipse.core.resources.IProject;

public class CLanguageSettingCache extends CDefaultLanguageData implements
		ICLanguageSetting, ICachedData {
	private ICResourceDescription fParent;
	protected EntryStore fResolvedEntriesStore;
	private String fCachedExtensions[];
	private boolean fContainsDiscoveredScannerInfo = true;

	public CLanguageSettingCache(CLanguageData base, CFolderDescriptionCache folderCache) {
		fId = base.getId();
		fParent = folderCache;
		copySettingsFrom(base);
	}

	public CLanguageSettingCache(CLanguageData base, CFileDescriptionCache fileCache) {
		fId = base.getId();
		fParent = fileCache;
		copySettingsFrom(base);
	}

	@Override
	protected void copySettingsFrom(CLanguageData data) {
		super.copySettingsFrom(data);
		fContainsDiscoveredScannerInfo = data.containsDiscoveredScannerInfo();
	}

	/*	public ICLanguageSettingEntry[] getResolvedSettingEntries() {
		// TODO Auto-generated method stub
		return getSettingEntries();
	}
*/
	@Override
	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		ICLanguageSettingEntry[] entries = getSettingEntries(kind);
		if(entries.length != 0){
			if(fResolvedEntriesStore == null){
				fResolvedEntriesStore = new EntryStore();
			}

			ICLanguageSettingEntry[] resolved = fResolvedEntriesStore.getEntries(kind);
			if(resolved.length == 0){
				resolved = CDataUtil.resolveEntries(entries, getConfiguration());
				fResolvedEntriesStore.storeEntries(kind, resolved);
			}

			entries = resolved;
		}
		return entries;
	}

	private IProject getProject(){
		return getConfiguration().getProjectDescription().getProject();
	}


	@Override
	public String[] getSourceExtensions() {
		if(fCachedExtensions == null ){
			String[] typeIds = getSourceContentTypeIds();
			String exts[] = null;
			if(typeIds != null && typeIds.length != 0){
				exts = CDataUtil.getExtensionsFromContentTypes(getProject(), typeIds);
			} else {
				exts = super.getSourceExtensions();
				if(exts != null && exts.length != 0)
					exts = exts.clone();
				else
					exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
			}

			if(exts == null)
				exts = CDefaultLanguageData.EMPTY_STRING_ARRAY;
			fCachedExtensions = exts;
		}

		if(fCachedExtensions.length != 0)
			return fCachedExtensions.clone();
		return fCachedExtensions;
	}

	@Override
	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
//		int kinds[] = KindBasedStore.getSupportedKinds();
//		List list = new ArrayList();
//		for(int i = 0; i < kinds.length; i++){
//			ICLanguageSettingEntry entries[] = fStore.getEntries(kinds[i]);
//			for(int j = 0; j < entries.length; j++){
//				list.add(entries[j]);
//			}
//		}
//		return (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
		return fStore.getEntries(kind);
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
//		int kinds[] = KindBasedStore.getSupportedKinds();
//		List list = new ArrayList();
//		for(int i = 0; i < kinds.length; i++){
//			ICLanguageSettingEntry entries[] = fStore.getEntries(kinds[i]);
//			for(int j = 0; j < entries.length; j++){
//				list.add(entries[j]);
//			}
//		}
//		return (ICLanguageSettingEntry[])list.toArray(new ICLanguageSettingEntry[list.size()]);
		return fStore.getEntriesList(kind);
	}


	@Override
	public void setLanguageId(String id) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setName(String name) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setSettingEntries(int kind, List<ICLanguageSettingEntry> entriesList) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setEntries(int kind, ICLanguageSettingEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public boolean supportsEntryKind(int kind) {
		return (getSupportedEntryKinds() & kind) == kind;
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return fParent.getConfiguration();
	}

	@Override
	public ICSettingContainer getParent() {
		return fParent;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	protected ICLanguageSettingEntry[] processStoredEntries(ICLanguageSettingEntry[] entries, int op) {
		for(int i = 0; i < entries.length; i++) {
			entries[i] = CDataUtil.getPooledEntry(entries[i]);
		}
		return entries;
	}

	@Override
	public boolean containsDiscoveredScannerInfo() {
		return fContainsDiscoveredScannerInfo;
	}
}
