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
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.List;

public interface ICLanguageSetting extends ICSettingObject {
	String[] getSourceContentTypeIds();

	String[] getSourceExtensions();

	//	IContentType getHeaderContentType();

	//	String[] getHeaderExtensions();

	/**
	 * @return language id. Note that that id can be {@code null}.
	 */
	String getLanguageId();

	//	ICLanguageSettingEntry[] getSettingEntries();

	//	void removeEntry(ICLanguageSettingEntry entry);

	//	void addEntry(ICLanguageSettingEntry entry, int position);

	ICLanguageSettingEntry[] getSettingEntries(int kind);

	List<ICLanguageSettingEntry> getSettingEntriesList(int kind);

	//	ICLanguageSettingEntry[] getResolvedSettingEntries();

	ICLanguageSettingEntry[] getResolvedSettingEntries(int kind);

	void setSettingEntries(int kind, ICLanguageSettingEntry[] entries);

	void setSettingEntries(int kind, List<ICLanguageSettingEntry> entriesList);

	//	void changeEntries(ICLanguageSettingEntryInfo[] added, ICLanguageSettingEntry[] removed);

	int getSupportedEntryKinds();

	boolean supportsEntryKind(int kind);

	void setLanguageId(String id);

	void setSourceContentTypeIds(String ids[]);

	void setSourceExtensions(String exts[]);
}
