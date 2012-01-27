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
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.core.runtime.IPath;

public class PathInfoToLangSettingsConverter {
	public static int getSupportedEntryKinds(IDiscoveredPathInfo info){
		if(info instanceof IPerFileDiscoveredPathInfo){
			return getSupportedEntryKinds((IPerFileDiscoveredPathInfo)info);
		}
		return ICLanguageSettingEntry.INCLUDE_PATH
			| ICLanguageSettingEntry.MACRO;
	}

	public static int getSupportedEntryKinds(IPerFileDiscoveredPathInfo info){
		return ICLanguageSettingEntry.INCLUDE_FILE
			| ICLanguageSettingEntry.INCLUDE_PATH
			| ICLanguageSettingEntry.MACRO
			| ICLanguageSettingEntry.MACRO_FILE;
	}

	public static ICLanguageSettingEntry[] entriesForKind(int kind, int flags, PathInfo info){
		switch (kind) {
		case ICLanguageSettingEntry.INCLUDE_PATH:
			ICLanguageSettingEntry[] incPaths = calculateEntries(kind, flags, info.getIncludePaths());
			IPath[] quotedPaths = info.getQuoteIncludePaths();
			if(quotedPaths.length != 0){
				if(incPaths.length != 0){
					ICLanguageSettingEntry quotedEntries[] = calculateEntries(kind, flags, quotedPaths);
					ICLanguageSettingEntry[] tmp = new ICLanguageSettingEntry[incPaths.length + quotedEntries.length];
					System.arraycopy(incPaths, 0, tmp, 0, incPaths.length);
					System.arraycopy(quotedEntries, 0, tmp, incPaths.length, quotedEntries.length);
					incPaths = tmp;
				} else {
					incPaths = calculateEntries(kind, flags, quotedPaths);
				}
			}
			return incPaths;
		case ICLanguageSettingEntry.MACRO:
			return calculateEntries(flags, info.getSymbols());
		case ICLanguageSettingEntry.MACRO_FILE:
			return calculateEntries(kind, flags, info.getMacroFiles());
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return calculateEntries(kind, flags, info.getIncludeFiles());
		}
		return new ICLanguageSettingEntry[0];
	}

	private static ICLanguageSettingEntry[] calculateEntries(int kind, int flags, IPath[] values){
		ICLanguageSettingEntry entries[] = new ICLanguageSettingEntry[values.length];
		for(int i = 0; i < values.length; i++){
			entries[i] = (ICLanguageSettingEntry)CDataUtil.createEntry(kind, values[i].toString(), null, null, flags);
		}
		return entries;
	}

	private static ICMacroEntry[] calculateEntries(int flags, Map<String, String> map){
		ICMacroEntry entries[] = new ICMacroEntry[map.size()];
		int num = 0;
		Set<Entry<String, String>> entrySet = map.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String name = entry.getKey();
			String value = entry.getValue();
			entries[num++] = CDataUtil.createCMacroEntry(name, value, flags);
		}
		return entries;
	}
}
