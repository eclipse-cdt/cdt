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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

public class CEntriesSet {
	private HashMap fEntriesMap = new HashMap();

	public CEntriesSet(){
	}

	public CEntriesSet(List list){
		setEntries(list);
	}

	public CEntriesSet(ICLanguageSettingEntry entries[]){
		setEntries(entries);
	}

	public int size() {
		return fEntriesMap.size();
	}

/*	public ICLanguageSettingEntry removeEntry(String name) {
		ICLanguageSettingEntry entry = (ICLanguageSettingEntry)fMap.remove(name);
		if(entry != null)
			fList.remove(entry);
		return entry;
	}
*/
	public ICLanguageSettingEntry[] toArray() {
		return (ICLanguageSettingEntry[])fEntriesMap.values().toArray(new ICLanguageSettingEntry[fEntriesMap.size()]);
	}
	
	protected Object getKey(ICLanguageSettingEntry entry){
		return entry;
	}
	
	public ICLanguageSettingEntry addEntry(ICLanguageSettingEntry entry) {
		return (ICLanguageSettingEntry)fEntriesMap.put(getKey(entry), entry);
	}

	public void clear() {
		fEntriesMap.clear();
	}
	
	public void setEntries(List list) {
		clear();
		for(Iterator iter = list.iterator(); iter.hasNext();){
			Object obj = iter.next();
			if(obj instanceof ICLanguageSettingEntry){
				ICLanguageSettingEntry entry = (ICLanguageSettingEntry)obj;
				addEntry(entry);
			}
		}
	}

	public void setEntries(ICLanguageSettingEntry[] entries) {
		clear();
		for(int i = 0; i < entries.length; i++){
			ICLanguageSettingEntry entry = entries[i];
			if(entry != null){
				addEntry(entry);
			}
		}
	}
	
}
