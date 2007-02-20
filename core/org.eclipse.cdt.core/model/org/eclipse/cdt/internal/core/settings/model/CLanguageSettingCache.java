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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;

public class CLanguageSettingCache extends CDefaultLanguageData implements
		ICLanguageSetting, ICachedData {
	private ICResourceDescription fParent;
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

/*	public ICLanguageSettingEntry[] getResolvedSettingEntries() {
		// TODO Auto-generated method stub
		return getSettingEntries();
	}
*/
	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		// TODO Auto-generated method stub
		return getSettingEntries(kind);
	}

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
	
	public List getSettingEntriesList(int kind) {
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
	

	public void setLanguageId(String id) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setName(String name) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setSettingEntries(int kind, List entriesList) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setEntries(int kind, ICLanguageSettingEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public boolean supportsEntryKind(int kind) {
		return (getSupportedEntryKinds() & kind) == kind;
	}

	public ICConfigurationDescription getConfiguration() {
		return fParent.getConfiguration();
	}

	public ICSettingContainer getParent() {
		return fParent;
	}

	public boolean isReadOnly() {
		return true;
	}

}
