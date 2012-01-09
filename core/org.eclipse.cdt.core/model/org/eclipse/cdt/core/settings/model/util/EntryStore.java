/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICMacroEntry;
import org.eclipse.cdt.core.settings.model.ICMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class EntryStore {
	private KindBasedStore<ArrayList<ICLanguageSettingEntry>> fStore = new KindBasedStore<ArrayList<ICLanguageSettingEntry>>();
	private boolean fPreserveReadOnly;
	
	public EntryStore(){
		this(false);
	}

	public EntryStore(boolean preserveReadOnly){
		fPreserveReadOnly = preserveReadOnly;
	}
	
	@SuppressWarnings("unchecked")
	public EntryStore(EntryStore base, boolean preserveReadOnly){
		for(int kind : KindBasedStore.getLanguageEntryKinds()){
			ArrayList<ICLanguageSettingEntry> list = fStore.get(kind);
			if(list != null)
				fStore.put(kind, (ArrayList<ICLanguageSettingEntry>) list.clone());
		}
		fPreserveReadOnly = preserveReadOnly;
	}

	public ICLanguageSettingEntry[] getEntries(){
		List<ICLanguageSettingEntry> result = new ArrayList<ICLanguageSettingEntry>();
		List<ICLanguageSettingEntry> list;
		for(int k: KindBasedStore.getLanguageEntryKinds()){
			list = fStore.get(k);
			if(list != null)
				result.addAll(list);
		}
		return result.toArray(new ICLanguageSettingEntry[result.size()]);
	}
	
	public boolean containsEntriesList(int kind){
		List<ICLanguageSettingEntry> list = getEntriesList(kind, false);
		return list != null;
	}

	public ICLanguageSettingEntry[] getEntries(int kind){
		List<ICLanguageSettingEntry> list = getEntriesList(kind);
		if(list == null)
			list = new ArrayList<ICLanguageSettingEntry>(0);
		switch(kind){
		case ICSettingEntry.INCLUDE_PATH:
			return list.toArray(new ICIncludePathEntry[list.size()]);
		case ICSettingEntry.INCLUDE_FILE:
			return list.toArray(new ICIncludeFileEntry[list.size()]);
		case ICSettingEntry.MACRO:
			return list.toArray(new ICMacroEntry[list.size()]);
		case ICSettingEntry.MACRO_FILE:
			return list.toArray(new ICMacroFileEntry[list.size()]);
		case ICSettingEntry.LIBRARY_PATH:
			return list.toArray(new ICLibraryPathEntry[list.size()]);
		case ICSettingEntry.LIBRARY_FILE:
			return list.toArray(new ICLibraryFileEntry[list.size()]);
		default:
			throw new IllegalArgumentException();
		}
	}

	public List<ICLanguageSettingEntry> getEntriesList(int kind){
		List<ICLanguageSettingEntry> list = getEntriesList(kind, false);
		if(list != null)
			return new ArrayList<ICLanguageSettingEntry>(list);
		return new ArrayList<ICLanguageSettingEntry>(0);
	}

	private void setEntriesList(int kind, ArrayList<ICLanguageSettingEntry> list){
		fStore.put(kind, list);
	}
	
	private ArrayList<ICLanguageSettingEntry> getEntriesList(int kind, boolean create){
		ArrayList<ICLanguageSettingEntry> list = fStore.get(kind);
		if(list == null && create){
			fStore.put(kind, list = new ArrayList<ICLanguageSettingEntry>());
		}
		return list;
	}

//	public void storeEntries(int kind, List list){
//		fStore.put(kind, list);
//	}
	
	public void addEntry(int pos, ICLanguageSettingEntry entry){
		List<ICLanguageSettingEntry> list = getEntriesList(entry.getKind(), true);
		if(pos >= list.size())
			list.add(entry);
		else 
			list.add(pos ,entry);
	}

	public void addEntries(ICLanguageSettingEntry[] entries){
		for(int i = 0; i < entries.length; i++){
			addEntry(entries[i]);
		}
	}

	public void storeEntries(int kind, ICLanguageSettingEntry[] entries){
		storeEntries(kind, 
				entries != null ? 
						Arrays.asList(entries) : 
						new ArrayList<ICLanguageSettingEntry>());
	}

	public void storeEntries(int kind, List<ICLanguageSettingEntry> list){
		ArrayList<ICLanguageSettingEntry> newList = new ArrayList<ICLanguageSettingEntry>(list);
//		newList.addAll(Arrays.asList(entries));
		if(fPreserveReadOnly){
			List<ICLanguageSettingEntry> oldList = getEntriesList(kind, false);
			if(oldList != null){
				Set<ICLanguageSettingEntry> ro = getReadOnlySet(oldList);
				ro.removeAll(newList);
				for(ICLanguageSettingEntry o : oldList){
					if(ro.contains(o))
						newList.add(o);
				}
			}
		}
		setEntriesList(kind, newList);
	}
	
	private Set<ICLanguageSettingEntry> getReadOnlySet(List<ICLanguageSettingEntry> entries){
		Set<ICLanguageSettingEntry> set = new HashSet<ICLanguageSettingEntry>();
		for(ICLanguageSettingEntry entry : entries){
			if(entry.isReadOnly())
				set.add(entry);
		}
		return set;
	}
	
	public void addEntry(ICLanguageSettingEntry entry){
		List<ICLanguageSettingEntry> list = getEntriesList(entry.getKind(), true);
		list.add(entry);
	}
	
	public void trimToSize(){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for(int i = 0; i < kinds.length; i++){
			ArrayList<ICLanguageSettingEntry> list = fStore.get(kinds[i]);
			if(list != null)
				list.trimToSize();
		}
	}
	
/*	public void addEntries(int kind, ICLanguageSettingEntry[] entries){
		List list = getEntriesList(kind, true);
		if(list.size() == 0){
			for(int i = 0; i < entries.length; i++){
				list.add(entries[i]);
			}
		} else {
			Map map = createNameToEntryMap(list);
			for(int i = 0; i < entries.length; i++){
				rtrtrtrrtrt;
			}
		}
			
	}
	
	private Map createNameToEntryMap(List entries){
		Map map = new HashMap();
		
		for(Iterator iter = entries.iterator(); iter.hasNext();){
			ICLanguageSettingEntry entry = (ICLanguageSettingEntry)iter.next();
			map.put(entry.getName(), entry);
		}
		return map;
	}
*/	
}
