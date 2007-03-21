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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;

public class CSettingEntryFactory {
	private static final HashSet EMPTY_SET = new HashSet(0);
	
	private KindBasedStore fStore = new KindBasedStore(false);
	
	private HashMap getNameMap(int kind, boolean create){
		HashMap map = (HashMap)fStore.get(kind);
		if(map == null && create){
			map = new HashMap();
			fStore.put(kind, map);
		}
		return map;
	}

	private HashMap getValueMap(String name, boolean create){
		HashMap nameMap = getNameMap(ICSettingEntry.MACRO, create);
		if(nameMap != null){
			return getMap(nameMap, name, create);
		}
		return null;
	}

	private HashMap getFlagMap(int kind, String name, String value, IPath[] exclusionPatters, boolean create){
		switch(kind){
		case ICSettingEntry.MACRO:
			HashMap valueMap = getValueMap(name, create);
			if(valueMap != null){
				return getMap(valueMap, name, create);
			}
			return null;
		case ICSettingEntry.SOURCE_PATH:
		case ICSettingEntry.OUTPUT_PATH:
			HashMap excPatternMap = getExclusionPatternsMap(kind, name, create);
			if(excPatternMap != null){
				HashSet setKey = exclusionPatters == null || exclusionPatters.length == 0 ? EMPTY_SET : new HashSet(Arrays.asList(exclusionPatters)); 
				return getMap(excPatternMap, setKey, create);
			}
			return null;
		default:
			HashMap nameMap = getNameMap(kind, create);
			if(nameMap != null){
				return getMap(nameMap, name, create);
			}
			return null;
		}
	}
	
	private HashMap getExclusionPatternsMap(int kind, String name, boolean create){
		HashMap nameMap = getNameMap(kind, create);
		if(nameMap != null){
			return getMap(nameMap, name, create);
		}
		return null;
	}
	
	private static HashMap getMap(HashMap container, Object key, boolean create){
		HashMap map = (HashMap)container.get(key);
		if(map == null && create){
			map = new HashMap();
			container.put(key, map);
		}
		return map;
	}

	public ICSettingEntry getEntry(ICSettingEntry entry){
		switch(entry.getKind()){
		case ICSettingEntry.OUTPUT_PATH:
		case ICSettingEntry.SOURCE_PATH:
			return getEntry(entry.getKind(), entry.getName(), null, ((ICExclusionPatternPathEntry)entry).getExclusionPatterns(), entry.getFlags(), entry, true);
		default:
			return getLanguageSettingEntry((ICLanguageSettingEntry)entry); 
		}
	}
	
	public ICLanguageSettingEntry getLanguageSettingEntry(ICLanguageSettingEntry lEntry){
		return (ICLanguageSettingEntry)getEntry(lEntry.getKind(), lEntry.getName(), lEntry.getValue(), null, lEntry.getFlags(), lEntry, true);
	}

	public ICSettingEntry getEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, boolean create){
		return getEntry(kind, name, value, exclusionPatterns, flags, null, create);
	}

	private ICSettingEntry getEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, ICSettingEntry baseEntry, boolean create){
		HashMap flagMap = getFlagMap(kind, name, value, exclusionPatterns, create);
		if(flagMap != null){
			Integer iFlags = new Integer(flags);
			ICSettingEntry entry = (ICSettingEntry)flagMap.get(iFlags);
			if(entry == null && create){
				entry = baseEntry != null ? baseEntry : CDataUtil.createEntry(kind, name, value, exclusionPatterns, flags);
				flagMap.put(iFlags, entry);
			}
			return entry;
		}
		return null;
	}
	
	public void clear(){
		fStore.clear();
	}
}
