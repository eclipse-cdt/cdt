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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.ICStorageElement;

public class MapStorageElement implements ICStorageElement {
	private HashMap fMap;
	private String fName;
	private MapStorageElement fParent;
	private static final String CHILDREN_KEY = "?children?";
	private static final String NAME_KEY = "?name?";
	private static final String VALUE_KEY = "?value?";
	private List fChildren = new ArrayList();
	private String fValue;

	public MapStorageElement(String name, MapStorageElement parent){
		fName = name;
		fParent = parent;
		fMap = new HashMap();
	}

	public MapStorageElement(Map map, MapStorageElement parent){
		fName = (String)map.get(getMapKey(NAME_KEY));
		fValue = (String)map.get(getMapKey(VALUE_KEY));
		fMap = new HashMap(map);
		fParent = parent;
		
		String children = (String)map.get(getMapKey(CHILDREN_KEY));
		if(children != null){
			List childrenStrList = decodeList(children);
			int size = childrenStrList.size();
			if(size != 0){
				for(int i = 0; i < size; i++){
					Map childMap = decodeMap((String)childrenStrList.get(i));
					MapStorageElement child = createChildElement(childMap);
					fChildren.add(child);
				}
			}
		}
	}
	
	protected MapStorageElement createChildElement(Map childMap){
		return new MapStorageElement(childMap, this);
	}
	
	protected String getMapKey(String name){
		return name;
	}
	
	public Map toStringMap(){
		Map map = (Map)fMap.clone();
		if(fName != null)
			map.put(getMapKey(NAME_KEY), fName);
		else
			map.remove(getMapKey(NAME_KEY));
		
		if(fValue != null)
			map.put(getMapKey(VALUE_KEY), fValue);
		else
			map.remove(getMapKey(VALUE_KEY));
		
		int size = fChildren.size();
		if(size != 0){
			List childrenStrList = new ArrayList(size);
			for(int i = 0; i < size; i++){
				MapStorageElement child = (MapStorageElement)fChildren.get(i);
				Map childStrMap = child.toStringMap();
				String str = encodeMap(childStrMap);
				childrenStrList.add(str);
			}
			
			String childrenStr = encodeList(childrenStrList);
			map.put(getMapKey(CHILDREN_KEY), childrenStr);
		} else {
			map.remove(getMapKey(CHILDREN_KEY));
		}
		
		return map;
	}
	
	protected boolean isSystemKey(String key){
		return key.indexOf('?') == 0 && key.lastIndexOf('?') == key.length() - 1;
	}

	public void clear() {
		fMap.clear();
	}

	public ICStorageElement createChild(String name) {
		MapStorageElement child = createChildElement(name);
		fChildren.add(child);
		return child;
	}
	
	protected MapStorageElement createChildElement(String name){
		return new MapStorageElement(name, this); 
	}

	public String getAttribute(String name) {
		Object o = fMap.get(getMapKey(name));
		if(o instanceof String)
			return (String)o;
		return null;
	}

	public ICStorageElement[] getChildren() {
		return (MapStorageElement[])fChildren.toArray(new MapStorageElement[fChildren.size()]);
	}

	public String getName() {
		return fName;
	}

	public ICStorageElement getParent() {
		return fParent;
	}

	public void removeChild(ICStorageElement child){
		fChildren.remove(child);
		if(child instanceof MapStorageElement){
			((MapStorageElement)child).removed();
		}
	}
	
	private void removed() {
		fParent = null;
	}

	public void removeAttribute(String name) {
		fMap.remove(getMapKey(name));
	}

	public void setAttribute(String name, String value) {
		fMap.put(getMapKey(name), value);
	}
	
	public static Map decodeMap(String value) {
		List list = decodeList(value);
		Map map = new HashMap();
		char escapeChar = '\\';

		for(int i = 0; i < list.size(); i++){
			StringBuffer line = new StringBuffer((String)list.get(i));
			int lndx = 0;
			while (lndx < line.length()) {
				if (line.charAt(lndx) == '=') {
					if (line.charAt(lndx - 1) == escapeChar) {
						// escaped '=' - remove '\' and continue on.
						line.deleteCharAt(lndx - 1);
					} else {
						break;
					}
				}
				lndx++;
			}
			map.put(line.substring(0, lndx), line.substring(lndx + 1));
		}
		
		return map;

	}
	
	public static List decodeList(String value) {
		List list = new ArrayList();
		if (value != null) {
			StringBuffer envStr = new StringBuffer(value);
			String escapeChars = "|\\"; //$NON-NLS-1$
			char escapeChar = '\\';
			try {
				while (envStr.length() > 0) {
					int ndx = 0;
					while (ndx < envStr.length()) {
						if (escapeChars.indexOf(envStr.charAt(ndx)) != -1) {
							if (envStr.charAt(ndx - 1) == escapeChar) { 
								// escaped '|' - remove '\' and continue on.
								envStr.deleteCharAt(ndx - 1);
								if (ndx == envStr.length()) {
									break;
								}
							}
							if (envStr.charAt(ndx) == '|')
								break;
						}
						ndx++;
					}
					StringBuffer line = new StringBuffer(envStr.substring(0, ndx));
/*					int lndx = 0;
					while (lndx < line.length()) {
						if (line.charAt(lndx) == '=') {
							if (line.charAt(lndx - 1) == escapeChar) {
								// escaped '=' - remove '\' and continue on.
								line.deleteCharAt(lndx - 1);
							} else {
								break;
							}
						}
						lndx++;
					}
*/
					list.add(line.toString());
					envStr.delete(0, ndx + 1);
				}
			} catch (StringIndexOutOfBoundsException e) {
			}
		}
		return list;
	}
	
	public static String encodeMap(Map values) {
		List list = new ArrayList();
		Iterator entries = values.entrySet().iterator();
		StringBuffer str = new StringBuffer();
		while (entries.hasNext()) {
			Entry entry = (Entry)entries.next();
			str.append(escapeChars((String)entry.getKey(), "=|\\", '\\')); //$NON-NLS-1$
			str.append("="); //$NON-NLS-1$
			str.append(escapeChars((String)entry.getValue(), "|\\", '\\')); //$NON-NLS-1$
			list.add(str.toString());
			str.delete(0, str.length());
		}
		return encodeList(list);
	}
	
	public static String encodeList(List values) {
		StringBuffer str = new StringBuffer();
		Iterator entries = values.iterator();
		while (entries.hasNext()) {
			String entry = (String)entries.next();
			str.append(escapeChars(entry, "|\\", '\\')); //$NON-NLS-1$
//			str.append("="); //$NON-NLS-1$
//			str.append(escapeChars((String)entry.getValue(), "|\\", '\\')); //$NON-NLS-1$
			str.append("|"); //$NON-NLS-1$
		}
		return str.toString();
	}

	public static String escapeChars(String string, String escapeChars, char escapeChar) {
		StringBuffer str = new StringBuffer(string);
		for (int i = 0; i < str.length(); i++) {
			if (escapeChars.indexOf(str.charAt(i)) != -1) {
				str.insert(i, escapeChar);
				i++;
			}
		}
		return str.toString();
	}

	public String getValue() {
		return fValue;
	}

	public void setValue(String value) {
		fValue = value;
	}

	public ICStorageElement importChild(ICStorageElement el)
			throws UnsupportedOperationException {
		// TODO
		throw new UnsupportedOperationException();
	}

	public String[] getAttributeNames() {
		List list = new ArrayList(fMap.size());
		for(Iterator iter = fMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			if(!isSystemKey(key)){
				list.add(key);
			}
		}
		
		return (String[])list.toArray(new String[list.size()]);
	}
}
