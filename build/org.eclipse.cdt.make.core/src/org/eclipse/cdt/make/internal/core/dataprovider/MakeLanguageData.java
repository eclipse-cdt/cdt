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
package org.eclipse.cdt.make.internal.core.dataprovider;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.util.UserAndDiscoveredEntryLanguageData;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.PathInfoToLangSettingsConverter;

public class MakeLanguageData extends UserAndDiscoveredEntryLanguageData {
	private PathInfo fDiscoveredCache;
	
	public MakeLanguageData() {
		super();
	}

	public MakeLanguageData(String id, CLanguageData base) {
		super(id, base);
	}

	public MakeLanguageData(String id, String name, String languageId, int kinds, String[] ids,
			boolean isContentTypes) {
		super(id, languageId, ids, isContentTypes);
		fName = name;
		fSupportedKinds = kinds;
	}

	@Override
	protected void copySettingsFrom(CLanguageData data) {
		super.copySettingsFrom(data);
		if(data instanceof MakeLanguageData){
			fDiscoveredCache = ((MakeLanguageData)data).fDiscoveredCache;
		}
	}

	@Override
	protected ICLanguageSettingEntry[] getAllDiscoveredEntries(int kind) {
		if(fDiscoveredCache != null){
			int roFlag = canDisableDiscoveredEntries(kind) ? 0 : ICLanguageSettingEntry.READONLY;
			return PathInfoToLangSettingsConverter.entriesForKind(kind, 
					roFlag 
					| ICLanguageSettingEntry.BUILTIN
					| ICLanguageSettingEntry.RESOLVED,
					fDiscoveredCache);
		}
		return null;
	}
	
	protected void setDiscoveredInfo(PathInfo info){
		fDiscoveredCache = info;
	}
}
