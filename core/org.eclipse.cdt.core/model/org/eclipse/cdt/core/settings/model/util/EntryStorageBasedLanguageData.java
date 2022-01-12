/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;

public abstract class EntryStorageBasedLanguageData extends CDefaultLanguageData {

	public EntryStorageBasedLanguageData() {
		super();
	}

	public EntryStorageBasedLanguageData(String id, CLanguageData base) {
		super(id, base);
	}

	public EntryStorageBasedLanguageData(String id, String languageId, String[] ids, boolean isContentTypes) {
		super(id, languageId, ids, isContentTypes);
	}

	@Override
	public ICLanguageSettingEntry[] getEntries(int kind) {
		AbstractEntryStorage storage = getStorage(kind);
		if (storage != null) {
			List<ICLanguageSettingEntry> list = storage.getEntries(null);
			return list.toArray(new ICLanguageSettingEntry[list.size()]);
		}
		return new ICLanguageSettingEntry[0];
	}

	@Override
	public void setEntries(int kind, ICLanguageSettingEntry[] entries) {
		AbstractEntryStorage storage = getStorage(kind);
		if (storage != null) {
			storage.setEntries(entries);
		}
	}

	protected void setEntriesToStore(int kind, ICLanguageSettingEntry[] entries) {
		fStore.storeEntries(kind, entries);
	}

	protected ICLanguageSettingEntry[] getEntriesFromStore(int kind) {
		return fStore.getEntries(kind);
	}

	@Override
	protected EntryStore createStore() {
		return new EntryStore(false);
	}

	protected abstract AbstractEntryStorage getStorage(int kind);

}
