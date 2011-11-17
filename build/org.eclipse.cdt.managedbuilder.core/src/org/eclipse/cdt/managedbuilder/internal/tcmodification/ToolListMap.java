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
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;

public class ToolListMap implements Cloneable {
	private HashMap<ITool, List<ITool>> fMap;
	private CollectionEntrySet fCollectionEntrySet;

	public ToolListMap(){
		fMap = new HashMap<ITool, List<ITool>>();
	}

//	public class ValueIter {
//		private Map fIterMap;
//
//		public ValueIter() {
//			fIterMap = new HashMap(fMap);
//			for(Iterator iter = fIterMap.entrySet().iterator(); iter.hasNext();){
//				Map.Entry entry = (Map.Entry)iter.next();
//				Collection c = (Collection)entry.getValue();
//				entry.setValue(c.iterator());
//			}
//		}
//
//		public Iterator get(Object key){
//			Iterator iter = (Iterator)fIterMap.get(key);
//			if(iter != null && !iter.hasNext()){
//				fIterMap.remove(key);
//				return null;
//			}
//			return iter;
//		}
//	}

	public class CollectionEntry {
		private Map.Entry<ITool, List<ITool>> fEntry;

		CollectionEntry(Map.Entry<ITool, List<ITool>> entry){
			fEntry = entry;
		}

		public ITool getKey(){
			return fEntry.getKey();
		}

		public List<ITool> getValue(){
			return fEntry.getValue();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;

			if(obj == null)
				return false;

			if(!(obj instanceof CollectionEntry))
				return false;

			return fEntry.equals(((CollectionEntry)obj).fEntry);
		}

		@Override
		public int hashCode() {
			return fEntry.hashCode();
		}
	}

	private class CollectionEntrySet extends AbstractSet<CollectionEntry> {
		private Set<Entry<ITool, List<ITool>>> fMapEntrySet;

		private class Iter implements Iterator<CollectionEntry> {
			private Iterator<Entry<ITool, List<ITool>>> fIter;

			private Iter(){
				fIter = fMapEntrySet.iterator();
			}
			@Override
			public boolean hasNext() {
				return fIter.hasNext();
			}

			@Override
			public CollectionEntry next() {
				return new CollectionEntry(fIter.next());
			}

			@Override
			public void remove() {
				fIter.remove();
			}

		}

		private CollectionEntrySet(){
			fMapEntrySet = fMap.entrySet();
		}

		@Override
		public Iterator<CollectionEntry> iterator() {
			return new Iter();
		}

		@Override
		public int size() {
			return fMapEntrySet.size();
		}
	}


	public void add(ITool key, ITool value){
		List<ITool> l = get(key, true);
		l.add(value);
	}

	public List<ITool> removeAll(ITool key){
		return fMap.remove(key);
	}

	public List<ITool> get(ITool key, boolean create){
		List<ITool> l = fMap.get(key);
		if(l == null && create){
			l = newList(1);
			fMap.put(key, l);
		}

		return l;
	}

	public List<ITool> valuesToCollection(List<ITool> c){
		if(c == null)
			c = newList(20);

		for (List<ITool> l : fMap.values()) {
			c.addAll(l);
		}

		return c;
	}

//	public List<ITool> getValues(){
//		return valuesToCollection(null);
//	}

//	public ITool[] getValuesArray(Class clazz){
//		List<ITool> list = getValues();
//		ITool[] result = (ITool[])Array.newInstance(clazz, list.size());
//		return list.toArray(result);
//	}

	protected List<ITool> newList(int size){
		return new ArrayList<ITool>(size);
	}

	@SuppressWarnings("unchecked")
	protected List<ITool> cloneList(List<ITool> l){
		return (List<ITool>)((ArrayList<ITool>)l).clone();
	}

	public List<ITool> putValuesToCollection(List<ITool> c){
		for (CollectionEntry entry : collectionEntrySet()) {
			List<ITool> l = entry.getValue();
			c.addAll(l);
		}
		return c;
	}

	public void remove(ITool key, ITool value){
		List<ITool> c = get(key, false);
		if(c != null){
			if(c.remove(value) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}

	public ITool get(ITool key, int num){
		List<ITool> l = get(key, false);
		if(l != null){
			return l.get(num);
		}
		return null;
	}

	public ITool remove(ITool key, int num){
		List<ITool> l = get(key, false);
		if(l != null){
			ITool result = null;
			if(l.size() > num){
				result = l.remove(num);
			}

			return result;
		}
		return null;
	}

	public ITool removeLast(ITool key){
		List<ITool> l = get(key, false);
		if(l != null){
			ITool result = null;
			if(l.size() > 0){
				result = l.remove(l.size() - 1);
			}
			return result;
		}
		return null;
	}

	public void removeAll(ITool key, List<ITool> values){
		List<ITool> c = get(key, false);
		if(c != null){
			if(c.removeAll(values) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}

	public void clearEmptyLists(){
		for(Iterator<Entry<ITool, List<ITool>>> iter = fMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry<ITool, List<ITool>> entry = iter.next();
			if((entry.getValue()).size() == 0)
				iter.remove();
		}
	}

	public Set<CollectionEntry> collectionEntrySet(){
		if(fCollectionEntrySet == null)
			fCollectionEntrySet = new CollectionEntrySet();
		return fCollectionEntrySet;
	}

//	public void difference(ListMap map){
//		for(Iterator<Entry<ITool, List<ITool>>> iter = map.fMap.entrySet().iterator(); iter.hasNext(); ){
//			Map.Entry<ITool, List<ITool>> entry = iter.next();
//			List<ITool> thisC = fMap.get(entry.getKey());
//			if(thisC != null){
//				if(thisC.removeAll(entry.getValue()) && thisC == null){
//					fMap.remove(entry.getKey());
//				}
//			}
//		}
//	}

//	public ValueIter valueIter(){
//		return new ValueIter();
//	}

//	protected Collection createCollection(Object key){
//		return new ArrayList(1);
//	}

	@Override
	public Object clone() {
		try {
			ToolListMap clone = (ToolListMap)super.clone();
			@SuppressWarnings("unchecked")
			HashMap<ITool, List<ITool>> clone2 = (HashMap<ITool, List<ITool>>)fMap.clone();
			clone.fMap = clone2;
			for (Entry<ITool, List<ITool>> entry : clone.fMap.entrySet()) {
				entry.setValue(cloneList(entry.getValue()));
			}
		} catch (CloneNotSupportedException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

//	protected Map getMap(boolean create){
//		if(fMap == null && create)
//			fMap = createMap();
//		return fMap;
//	}
//
//	protected Map createMap(){
//		return new HashMap();
//	}
}