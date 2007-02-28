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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.util.EntryNameKey;

public class EntryListMap {
	private HashMap fMap = new HashMap();
	private ArrayList fList = new ArrayList();
	
	public EntryInfo getEntryInfo(ICLanguageSettingEntry entry){
		return (EntryInfo)fMap.get(new EntryNameKey(entry));
	}

	public void addEntryInfo(EntryInfo info){
		EntryNameKey key = info.getNameKey();
		EntryInfo old = (EntryInfo)fMap.remove(key);
		if(old != null)
			fList.remove(old);
		fMap.put(key, info);
		fList.add(info);
	}
	
	public Map getEntryInfoMap(){
		return (Map)fMap.clone();
	}
	
	public List getEntryInfoList(){
		return (List)fList.clone();
	}
	
	private class EntryIterator implements Iterator{
		Iterator fIter;
		Object fCurrent;
		EntryIterator (Iterator iter){
			fIter = iter;
		}
		public boolean hasNext() {
			return fIter.hasNext();
		}

		public Object next() {
			return fCurrent = fIter.next();
		}

		public void remove() {
			fMap.remove(((EntryInfo)fCurrent).getNameKey());
			fIter.remove();
		}
		
	}
	
	public Iterator getIterator(){
		return new EntryIterator(fList.iterator());
	}
	
	public EntryInfo[] getEntries(){
		return (EntryInfo[])fList.toArray(new EntryInfo[fList.size()]);
	}
	
	public void clear(){
		fMap.clear();
		fList.clear();
	}
	
	public int getSize(){
		return fList.size();
	}
	
	public EntryInfo getEntryInfo(int i){
		return (EntryInfo)fList.get(i);
	}
}
