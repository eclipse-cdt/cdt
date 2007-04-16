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

import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class CEntriesSet {
	private HashMap fEntriesMap = new HashMap();

	public CEntriesSet(){
	}

	public CEntriesSet(List list){
		setEntries(list);
	}

	public CEntriesSet(ICSettingEntry entries[]){
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
	public ICSettingEntry[] toArray() {
		return (ICSettingEntry[])fEntriesMap.values().toArray(new ICSettingEntry[fEntriesMap.size()]);
	}
	
	protected Object getKey(ICSettingEntry entry){
		return entry;
	}
	
	public ICSettingEntry addEntry(ICSettingEntry entry) {
		return (ICSettingEntry)fEntriesMap.put(getKey(entry), entry);
	}

	public void clear() {
		fEntriesMap.clear();
	}
	
	public void setEntries(List list) {
		clear();
		for(Iterator iter = list.iterator(); iter.hasNext();){
			Object obj = iter.next();
			if(obj instanceof ICSettingEntry){
				ICSettingEntry entry = (ICSettingEntry)obj;
				addEntry(entry);
			}
		}
	}

	public void setEntries(ICSettingEntry[] entries) {
		clear();
		for(int i = 0; i < entries.length; i++){
			ICSettingEntry entry = entries[i];
			if(entry != null){
				addEntry(entry);
			}
		}
	}
	
}
