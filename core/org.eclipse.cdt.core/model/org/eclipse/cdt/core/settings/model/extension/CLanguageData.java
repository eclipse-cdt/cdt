/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

public abstract class CLanguageData extends CDataObject {

	protected CLanguageData() {
	}

	//	public abstract CDataObject[] getChildrenOfKind(int kind);

	//	public abstract CDataObject getChildById(String id);

	@Override
	public final int getType() {
		return SETTING_LANGUAGE;
	}

	public abstract String getLanguageId();

	public abstract void setLanguageId(String id);

	public abstract String[] getSourceContentTypeIds();

	public abstract String[] getSourceExtensions();

	//	public abstract IContentType getHeaderContentType();

	//	public abstract String[] getHeaderExtensions();

	//	public abstract void removeEntry(ICLanguageSettingEntry entry);

	//	public abstract void addEntry(ICLanguageSettingEntry entry, int position);

	public abstract ICLanguageSettingEntry[] getEntries(int kind);

	public abstract void setEntries(int kind, ICLanguageSettingEntry entries[]);

	public abstract int getSupportedEntryKinds();

	public abstract void setSourceContentTypeIds(String ids[]);

	public abstract void setSourceExtensions(String exts[]);

	public boolean containsDiscoveredScannerInfo() {
		return true;
	}
}
