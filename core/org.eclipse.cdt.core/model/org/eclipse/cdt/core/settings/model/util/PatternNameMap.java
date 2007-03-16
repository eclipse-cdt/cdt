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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.cdt.core.model.CoreModelUtil;

public class PatternNameMap {
	private static final char[] SPEC_CHARS = new char[]{'*', '?'}; 

	private Map fChildrenMap;
	private Map fPatternMap;
	private Collection fValues;

	private static class StringCharArray {
		private String fString;
		private char[] fCharArray;

		StringCharArray(String string){
			fString = string;
		}
		
		char[] getCharArray(){
			if(fCharArray == null){
				fCharArray = fString.toCharArray();
			}
			return fCharArray;
		}

		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof StringCharArray))
				return false;
			
			return fString.equals(((StringCharArray)obj).fString);
		}

		public int hashCode() {
			return fString.hashCode();
		}

		public String toString() {
			return fString;
		}
	}
	
	private class EmptyIterator implements Iterator{

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
		
	}
	
	private class ValuesCollection extends AbstractCollection {
		
		private class Iter implements Iterator {
			private Iterator fEntrySetIter;
			private Map.Entry fCur;

			Iter (Iterator entryIter){
				this.fEntrySetIter = entryIter;
			}
			public boolean hasNext() {
				return fEntrySetIter.hasNext();
			}

			public Object next() {
				fCur = (Map.Entry)fEntrySetIter.next();
				return fCur.getValue();
			}

			public void remove() {
				fEntrySetIter.remove();
				removePattern((String)fCur.getKey());
			}
			
		}

		public Iterator iterator() {
			return fChildrenMap != null ? (Iterator)new Iter(fChildrenMap.entrySet().iterator()) : (Iterator)new EmptyIterator();
		}

		public int size() {
			return PatternNameMap.this.size();
		}
		
		public void clear(){
			PatternNameMap.this.clear();
		}
		
		public boolean contains(Object o){
			return fChildrenMap != null ? fChildrenMap.containsValue(o) : false;
		}
	}

	public Object get(String name){
		return fChildrenMap != null ? fChildrenMap.get(name) : null;
	}
	
	public int size(){
		return fChildrenMap != null ? fChildrenMap.size() : 0;
	}
	
	public boolean hasPatterns(){
		return fPatternMap != null && fPatternMap.size() != 0;
	}
	
	public List getValues(String name){
		if(fChildrenMap == null)
			return null;
		
		Object val = fChildrenMap.get(name);
		if(hasPatterns()){
			List list;
			if(val != null){
				list = new ArrayList(3);
				list.add(val);
			} else {
				list = null;;
			}
			
			Map.Entry entry;
			StringCharArray strCA;
			char[] nameCharArray = name.toCharArray(); 
			for(Iterator iter = fPatternMap.entrySet().iterator(); iter.hasNext();){
				entry = (Map.Entry)iter.next();
				strCA = (StringCharArray)entry.getKey();
				if(CoreModelUtil.match(strCA.getCharArray(), nameCharArray, true)){
					if(list == null)
						list = new ArrayList(2);
					list.add(entry.getValue());
				}
			}
			return list;
		} else if (val != null){
			List list = new ArrayList(1);
			list.add(val);
			return list;
		}
		return null;
	}
	
	public Object put(String name, Object value){
		if(value == null)
			return remove(name);
		
		Object oldValue;
		if(fChildrenMap == null){
			fChildrenMap = new HashMap();
			oldValue = null;
		} else {
			oldValue = fChildrenMap.get(name);
		}
		
		fChildrenMap.put(name, value);
		
		if(isPatternName(name)){
			StringCharArray strCA = new StringCharArray(name);
			if(fPatternMap == null)
				fPatternMap = new HashMap();
			
			fPatternMap.put(strCA, value);
		}
		
		return oldValue;
	}
	
	public Object remove(String name){
		if(fChildrenMap != null){
			Object oldVal = fChildrenMap.remove(name);
			if(fChildrenMap.size() == 0){
				fChildrenMap = null;
				fPatternMap = null;
			} else {
				removePattern(name);
			}
			return oldVal;
		}
		return null;
	}
	
	private void removePattern(String name){
		if (fPatternMap != null){
			fPatternMap.remove(new StringCharArray(name));
			if(fPatternMap.size() == 0)
				fPatternMap = null;
		}
	}
	
	private static boolean hasSpecChars(String str){
		for(int i = 0; i < SPEC_CHARS.length; i++){
			if(str.indexOf(SPEC_CHARS[i]) != -1)
				return true;
		}
		return false;
	}

	public static boolean isPatternName(String str){
		//TODO: check escape chars
		return hasSpecChars(str);
	}
	
	public void clear(){
		fChildrenMap = null;
		fPatternMap = null;
	}
	
	public Collection values(){
		if(fValues == null)
			fValues = new ValuesCollection();
		return fValues;
	}
}

