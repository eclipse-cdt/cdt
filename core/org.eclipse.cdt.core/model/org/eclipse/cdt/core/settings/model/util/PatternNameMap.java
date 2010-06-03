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
package org.eclipse.cdt.core.settings.model.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import org.eclipse.cdt.core.model.CoreModelUtil;

public class PatternNameMap {
	private static final char[] SPEC_CHARS = new char[]{'*', '?'}; 
	static final String DOUBLE_STAR_PATTERN = "**";  //$NON-NLS-1$

	private Map<String, PathSettingsContainer> fChildrenMap;
	private Map<StringCharArray, PathSettingsContainer> fPatternMap;
	private Collection<PathSettingsContainer> fValues;
	private boolean fContainsDoubleStar;

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

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(!(obj instanceof StringCharArray))
				return false;
			
			return fString.equals(((StringCharArray)obj).fString);
		}

		@Override
		public int hashCode() {
			return fString.hashCode();
		}

		@Override
		public String toString() {
			return fString;
		}
	}
	
	private class EmptyIterator implements Iterator<PathSettingsContainer>{

		public boolean hasNext() {
			return false;
		}

		public PathSettingsContainer next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new IllegalStateException();
		}
		
	}
	
	private class ValuesCollection extends AbstractCollection<PathSettingsContainer> {
		
		private class Iter implements Iterator<PathSettingsContainer> {
			private Iterator<Entry<String, PathSettingsContainer>> fEntrySetIter;
			private Entry<String, PathSettingsContainer> fCur;

			Iter (Iterator<Entry<String, PathSettingsContainer>> entryIter){
				this.fEntrySetIter = entryIter;
			}
			public boolean hasNext() {
				return fEntrySetIter.hasNext();
			}

			public PathSettingsContainer next() {
				fCur = fEntrySetIter.next();
				return fCur.getValue();
			}

			public void remove() {
				fEntrySetIter.remove();
				String name = fCur.getKey();
				if(DOUBLE_STAR_PATTERN.equals(name)){
					fContainsDoubleStar = false;
				} else {
					removePattern(name);
				}
			}
		}

		@Override
		public Iterator<PathSettingsContainer> iterator() {
			return fChildrenMap != null ? new Iter(fChildrenMap.entrySet().iterator()) : new EmptyIterator();
		}

		@Override
		public int size() {
			return PatternNameMap.this.size();
		}
		
		@Override
		public void clear(){
			PatternNameMap.this.clear();
		}
		
		@Override
		public boolean contains(Object o){
			return fChildrenMap != null ? fChildrenMap.containsValue(o) : false;
		}
	}

	public /* PathSettingsContainer */ Object get(String name){
		return fChildrenMap != null ? fChildrenMap.get(name) : null;
	}
	
	public int size(){
		return fChildrenMap != null ? fChildrenMap.size() : 0;
	}
	
	public boolean isEmpty(){
		return fChildrenMap == null || fChildrenMap.isEmpty();
	}
	
	public boolean hasPatterns(){
		return fContainsDoubleStar || hasPatternsMap();
	}

	public boolean hasPatternsMap(){
		return (fPatternMap != null && fPatternMap.size() != 0);
	}

	public List<PathSettingsContainer> getValues(String name){
		if(fChildrenMap == null)
			return null;
		
		PathSettingsContainer val = fChildrenMap.get(name);
		if(hasPatternsMap()){
			List<PathSettingsContainer> list;
			if(val != null){
				list = new ArrayList<PathSettingsContainer>(3);
				list.add(val);
			} else {
				list = null;
			}
			
			Map.Entry<PatternNameMap.StringCharArray,PathSettingsContainer> entry;
			StringCharArray strCA;
			char[] nameCharArray = name.toCharArray(); 
			for(Iterator<Map.Entry<PatternNameMap.StringCharArray,PathSettingsContainer>> iter = fPatternMap.entrySet().iterator(); iter.hasNext();){
				entry = iter.next();
				strCA = entry.getKey();
				if(CoreModelUtil.match(strCA.getCharArray(), nameCharArray, true)){
					if(list == null)
						list = new ArrayList<PathSettingsContainer>(2);
					list.add(entry.getValue());
				}
			}
			return list;
		} else if (val != null){
			List<PathSettingsContainer> list = new ArrayList<PathSettingsContainer>(1);
			list.add(val);
			return list;
		}
		return null;
	}
	
	public boolean containsDoubleStar(){
		return fContainsDoubleStar;
	}
	
	public /* PathSettingsContainer */ Object put(String name, /* PathSettingsContainer */Object value){
		return put(name, (PathSettingsContainer)value);
	}
	
	private PathSettingsContainer put(String name, PathSettingsContainer value){
		if(value == null)
			return (PathSettingsContainer)remove(name);
		
		PathSettingsContainer oldValue;
		if(fChildrenMap == null){
			fChildrenMap = new HashMap<String, PathSettingsContainer>();
			oldValue = null;
		} else {
			oldValue = fChildrenMap.get(name);
		}
		
		fChildrenMap.put(name, value);
		
		if(DOUBLE_STAR_PATTERN.equals(name)){
			fContainsDoubleStar = true;
		} else if(isPatternName(name)){
			StringCharArray strCA = new StringCharArray(name);
			if(fPatternMap == null)
				fPatternMap = new HashMap<StringCharArray, PathSettingsContainer>();
			
			fPatternMap.put(strCA, value);
		}
		
		return oldValue;
	}
	
	public /* PathSettingsContainer */ Object remove(String name){
		if(fChildrenMap != null){
			PathSettingsContainer oldVal = fChildrenMap.remove(name);
			if(fChildrenMap.size() == 0){
				fChildrenMap = null;
				fPatternMap = null;
				fContainsDoubleStar = false;
			} else if(DOUBLE_STAR_PATTERN.equals(name)){
				fContainsDoubleStar = false;
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
		fContainsDoubleStar = false;
	}
	
	public Collection<PathSettingsContainer> values(){
		if(fValues == null)
			fValues = new ValuesCollection();
		return fValues;
	}
}

