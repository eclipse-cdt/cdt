/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
