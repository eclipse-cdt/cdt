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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;

public class PathComparator implements Comparator<IPath> {
	public static PathComparator INSTANCE = new PathComparator();
//	public static final SortedSet EMPTY_SET = Collections.unmodifiableSortedSet(new TreeSet(INSTANCE));
//	public static final SortedMap EMPTY_MAP = Collections.unmodifiableSortedMap(new TreeMap(INSTANCE));
	
	private PathComparator(){
	}
	
	public int compare(IPath arg0, IPath arg1) {
		if(arg0 == arg1)
			return 0;
		
		IPath path1 = arg0;
		IPath path2 = arg1;
		
		int length1 = path1.segmentCount();
		int length2 = path2.segmentCount();
		int i = 0;
		for(; i < length1; i++){
			if(i >= length2){
				//path2 is a prefix of path1 
				return 1;
			}
			int comparison = path1.segment(i).compareTo(path2.segment(i));
			if(comparison == 0)
				continue;
			//
			return comparison;
		}
		
		if(length1 == length2){
			//equal
			return 0;
		}
		//path1 is a prefix of path2 
		return -1;
	}

	public static IPath getNext(IPath path){
		if(path.segmentCount() == 0)
			return null;
		String newLast = path.lastSegment() + '\0';
		return path.removeLastSegments(1).append(newLast);
	}
	
	public static IPath getFirstChild(IPath path){
		return path.append("\0"); //$NON-NLS-1$
	}

	public static SortedMap<IPath, PerTypeSetStorage> getChildPathMap(SortedMap<IPath, PerTypeSetStorage> map, IPath path, boolean includeThis, boolean copy){
		IPath start = includeThis ? path : getFirstChild(path); 
		IPath next = getNext(path);
		SortedMap<IPath, PerTypeSetStorage> result = next != null ? map.subMap(start, next) : map.tailMap(start);
		if(copy)
			result = new TreeMap<IPath, PerTypeSetStorage>(result);
		return result;
	}

	public static SortedSet<IPath> getChildPathSet(SortedSet<IPath> set, IPath path, boolean includeThis, boolean copy){
		IPath start = includeThis ? path : getFirstChild(path); 
		IPath next = getNext(path);
		SortedSet<IPath> result = next != null ? set.subSet(start, next) : set.tailSet(start);
		if(copy)
			result = new TreeSet<IPath>(result);
		return result;
	}
	
	public static SortedSet<IPath> getDirectChildPathSet(SortedSet<IPath> set, IPath path){
		//all children
		SortedSet<IPath> children = getChildPathSet(set, path, false, false);
		SortedSet<IPath> result = new TreeSet<IPath>(INSTANCE);
		for (IPath childPath : children) {
			result.add(childPath);
			children = children.tailSet(getNext(childPath));
		}
		
		return result;
	}
	
	public static SortedMap<IPath,PerTypeSetStorage> getDirectChildPathMap(SortedMap<IPath, PerTypeSetStorage> map, IPath path){
		//all children
		SortedMap<IPath,PerTypeSetStorage> children = getChildPathMap(map, path, false, false);
		SortedMap<IPath,PerTypeSetStorage> result = new TreeMap<IPath, PerTypeSetStorage>(INSTANCE);
		for(Iterator<Map.Entry<IPath,PerTypeSetStorage>> iter = children.entrySet().iterator(); iter.hasNext(); iter = children.entrySet().iterator()){
			Map.Entry<IPath,PerTypeSetStorage> entry = iter.next();
			IPath childPath = entry.getKey();
			result.put(childPath, entry.getValue());
			
			children = children.tailMap(getNext(childPath));//getChildPathMap(children, getNext(childPath), true, false);
		}
		
		return result;
	}

}
