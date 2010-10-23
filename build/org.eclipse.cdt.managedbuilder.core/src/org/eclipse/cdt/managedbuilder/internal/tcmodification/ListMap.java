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

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;

public class ListMap implements Cloneable {
	private HashMap fMap;
	private CollectionEntrySet fCollectionEntrySet;

	public ListMap(){
		fMap = new HashMap();
	}
	
	public class ValueIter {
		private Map fIterMap; 
		
		public ValueIter() {
			fIterMap = new HashMap(fMap);
			for(Iterator iter = fIterMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				Collection c = (Collection)entry.getValue();
				entry.setValue(c.iterator());
			}
		}
		
		public Iterator get(Object key){
			Iterator iter = (Iterator)fIterMap.get(key);
			if(iter != null && !iter.hasNext()){
				fIterMap.remove(key);
				return null;
			}
			return iter;
		}
	}
	
	public class CollectionEntry {
		private Map.Entry fEntry;
		
		CollectionEntry(Map.Entry entry){
			fEntry = entry;
		}
		
		public Object getKey(){
			return fEntry.getKey();
		}
		
		public List getValue(){
			return (List)fEntry.getValue();
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
	
	private class CollectionEntrySet extends AbstractSet {
		private Set fMapEntrySet;

		private class Iter implements Iterator {
			private Iterator fIter;
			
			private Iter(){
				fIter = fMapEntrySet.iterator();
			}
			public boolean hasNext() {
				return fIter.hasNext();
			}

			public Object next() {
				return new CollectionEntry((Map.Entry)fIter.next());
			}

			public void remove() {
				fIter.remove();
			}
			
		}

		private CollectionEntrySet(){
			fMapEntrySet = fMap.entrySet();
		}

		@Override
		public Iterator iterator() {
			return new Iter();
		}

		@Override
		public int size() {
			return fMapEntrySet.size();
		}
	}


	public void add(Object key, Object value){
		List l = get(key, true);
		l.add(value);
	}
	
	public List removeAll(Object key){
		return (List)fMap.remove(key);
	}

	public List get(Object key, boolean create){
		List l = (List)fMap.get(key);
		if(l == null && create){
			l = newList(1);
			fMap.put(key, l);
		}
		
		return l;
	}
	
	public Collection valuesToCollection(Collection c){
		if(c == null)
			c = newList(20);
		
		for(Iterator iter = fMap.values().iterator(); iter.hasNext(); ){
			List l = (List)iter.next();
			c.addAll(l);
		}
		
		return c;
	}
	
	public List getValues(){
		return (List)valuesToCollection(null);
	}

	public Object[] getValuesArray(Class clazz){
		List list = getValues();
		Object[] result = (Object[])Array.newInstance(clazz, list.size());
		return list.toArray(result);
	}

	protected List newList(int size){
		return new ArrayList(size);
	}

	protected List cloneList(List l){
		return (List)((ArrayList)l).clone();
	}
	
	public Collection putValuesToCollection(Collection c){
		for(Iterator iter = collectionEntrySet().iterator(); iter.hasNext(); ){
			List l = ((CollectionEntry)iter.next()).getValue();
			c.addAll(l);
		}
		return c;
	}

	public void remove(Object key, Object value){
		Collection c = get(key, false);
		if(c != null){
			if(c.remove(value) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}

	public Object get(Object key, int num){
		List l = get(key, false);
		if(l != null){
			return l.get(num);
		}
		return null;
	}

	public Object remove(Object key, int num){
		List l = get(key, false);
		if(l != null){
			Object result = null;
			if(l.size() > num){
				result = l.remove(num);
			}
			
			return result;
		}
		return null;
	}

	public Object removeLast(Object key){
		List l = get(key, false);
		if(l != null){
			Object result = null;
			if(l.size() > 0){
				result = l.remove(l.size() - 1);
			}
			return result;
		}
		return null;
	}

	public void removeAll(Object key, Collection values){
		Collection c = get(key, false);
		if(c != null){
			if(c.removeAll(values) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}
	
	public void clearEmptyLists(){
		for(Iterator iter = fMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			if(((List)entry.getValue()).size() == 0)
				iter.remove();
		}
	}

	public Set collectionEntrySet(){
		if(fCollectionEntrySet == null)
			fCollectionEntrySet = new CollectionEntrySet();
		return fCollectionEntrySet;
	}

	public void difference(ListMap map){
		for(Iterator iter = map.fMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			Collection thisC = (Collection)fMap.get(entry.getKey());
			if(thisC != null){
				if(thisC.removeAll((Collection)entry.getValue()) && thisC == null){
					fMap.remove(entry.getKey());
				}
			}
		}
	}
	
	public ValueIter valueIter(){
		return new ValueIter();
	}

//	protected Collection createCollection(Object key){
//		return new ArrayList(1);
//	}

	@Override
	public Object clone() {
		try {
			ListMap clone = (ListMap)super.clone();
			clone.fMap = (HashMap)fMap.clone();
			for(Iterator iter = clone.fMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				entry.setValue(cloneList((List)entry.getValue()));
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