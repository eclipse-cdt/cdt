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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
	private KindBasedStore fStore = new KindBasedStore();
	private boolean fPreserveReadOnly;
	
	public EntryStore(){
		this(false);
	}

	public EntryStore(boolean preserveReadOnly){
		fPreserveReadOnly = preserveReadOnly;
	}
	
	public EntryStore(EntryStore base, boolean preserveReadOnly){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			ArrayList list = (ArrayList)fStore.get(kind);
			if(list != null)
				fStore.put(kind, (ArrayList)list.clone());
		}
		fPreserveReadOnly = preserveReadOnly;
	}

	public ICLanguageSettingEntry[] getEntries(){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		List result = new ArrayList();
		List list;
		for(int i = 0; i < kinds.length; i++){
			list = (List)fStore.get(kinds[i]);
			if(list != null)
				result.addAll(list);
		}
		
		return (ICLanguageSettingEntry[])result.toArray(new ICLanguageSettingEntry[result.size()]);
	}
	
	public boolean containsEntriesList(int kind){
		List list = getEntriesList(kind, false);
		return list != null;
	}

	public ICLanguageSettingEntry[] getEntries(int kind){
		List list = getEntriesList(kind);
//		if(list != null){
		if(list == null)
			list = new ArrayList(0);
		switch(kind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return (ICLanguageSettingEntry[])list.toArray(new ICIncludePathEntry[list.size()]);
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return (ICLanguageSettingEntry[])list.toArray(new ICIncludeFileEntry[list.size()]);
		case ICLanguageSettingEntry.MACRO:
			return (ICLanguageSettingEntry[])list.toArray(new ICMacroEntry[list.size()]);
		case ICLanguageSettingEntry.MACRO_FILE:
			return (ICLanguageSettingEntry[])list.toArray(new ICMacroFileEntry[list.size()]);
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return (ICLanguageSettingEntry[])list.toArray(new ICLibraryPathEntry[list.size()]);
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return (ICLanguageSettingEntry[])list.toArray(new ICLibraryFileEntry[list.size()]);
		default:
			throw new IllegalArgumentException();
		}
//		}
//		return null;
	}

	public List getEntriesList(int kind){
		List list = getEntriesList(kind, false);
		if(list != null)
			return new ArrayList(list);
		return new ArrayList(0);
	}

	private void setEntriesList(int kind, List list){
		fStore.put(kind, list);
	}
	
	private List getEntriesList(int kind, boolean create){
		List list = (List)fStore.get(kind);
		if(list == null && create){
			fStore.put(kind, list = new ArrayList());
		}
		return list;
	}

//	public void storeEntries(int kind, List list){
//		fStore.put(kind, list);
//	}
	
	public void addEntry(int pos, ICLanguageSettingEntry entry){
		List list = getEntriesList(entry.getKind(), true);
		if(pos >= list.size())
			list.add(entry);
		else 
			list.add(pos ,entry);
	}

	public void storeEntries(int kind, ICLanguageSettingEntry[] entries){
		List newList = new ArrayList(entries.length);
		newList.addAll(Arrays.asList(entries));
		if(fPreserveReadOnly){
			List oldList = getEntriesList(kind, false);
			if(oldList != null){
				Set ro = getReadOnlySet(oldList);
				ro.removeAll(newList);
				for(Iterator iter = oldList.iterator(); iter.hasNext();){
					Object o = iter.next();
					if(ro.contains(o))
						newList.add(o);
				}
			}
		}
		setEntriesList(kind, newList);
	}
	
	private Set getReadOnlySet(List entries){
		Set set = new HashSet();
		for(Iterator iter = entries.iterator(); iter.hasNext();){
			ICSettingEntry entry = (ICSettingEntry)iter.next();
			if(entry.isReadOnly())
				set.add(entry);
		}
		return set;
	}
	
	public void addEntry(ICLanguageSettingEntry entry){
		List list = getEntriesList(entry.getKind(), true);
		list.add(entry);
	}
	
	public void trimToSize(){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		for(int i = 0; i < kinds.length; i++){
			ArrayList list = (ArrayList)fStore.get(kinds[i]);
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
