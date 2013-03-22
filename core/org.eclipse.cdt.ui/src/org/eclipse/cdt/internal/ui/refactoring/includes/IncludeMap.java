/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

class IncludeMap {
	private static final String TAG_CPP_ONLY = "cpp_only"; //$NON-NLS-1$
	private static final String TAG_FORCED_REPLACEMENT = "forced_replacement"; //$NON-NLS-1$
	private static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VALUE = "value"; //$NON-NLS-1$

	private final boolean forcedReplacement;
	private final boolean cppOnly;
	private final Map<IncludeInfo, List<IncludeInfo>> map;

	public IncludeMap(boolean forcedReplacement, boolean cppOnly) {
		this.forcedReplacement = forcedReplacement;
		this.cppOnly = cppOnly;
		this.map = new HashMap<IncludeInfo, List<IncludeInfo>>();
	}

	/**
	 * @param keysAndValues an array of keys and values: [key1, value1, key2, value2, ...].
	 *     Keys and values may be optionally surrounded by double quotes or angle brackets.
	 *     Angle brackets indicate a system include.  
	 */
	public IncludeMap(boolean forcedReplacement, boolean cppOnly, String[] keysAndValues) {
		if (keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException("More keys than values"); //$NON-NLS-1$
		this.forcedReplacement = forcedReplacement;
		this.cppOnly = cppOnly;
		this.map = new HashMap<IncludeInfo, List<IncludeInfo>>(keysAndValues.length / 2);
		for (int i = 0; i < keysAndValues.length;) {
			String key = keysAndValues[i++];
			addMapping(key, keysAndValues[i++]);
		}
	}

	/**
	 * Indicates that header file {@code to} should be used instead of {@code from}.
	 * 
	 * @param from The header file to be replaced.
	 * @param to The header file to be used.
	 */
	protected void addMapping(IncludeInfo from, IncludeInfo to) {
		List<IncludeInfo> list = map.get(from);
		if (list == null) {
			list = new ArrayList<IncludeInfo>(2);
			map.put(from, list);
		}
		list.add(to);
	}

	/**
	 * Indicates that header file {@code to} should be used instead of {@code from}.

	 * @param from The header file to be replaced. The header is represented by an include name
	 *     optionally surrounded by double quotes or angle brackets. Angle brackets indicate
	 *     a system include.
	 * @param to The header file to be used. The header is represented by an include name optionally
	 *     surrounded by double quotes or angle brackets. Angle brackets indicate a system include.
	 */
	public void addMapping(String from, String to) {
		addMapping(new IncludeInfo(from), new IncludeInfo(to));
	}

	/**
	 * Returns header files that should be used instead of the given one.
	 * 
	 * @param from The header file to be replaced. A system header has to match exactly.
	 *     A non-system header matches both, non-system and system headers.
	 * @return The list of header files ordered by decreasing preference. 
	 */
	public List<IncludeInfo> getMapping(IncludeInfo from) {
		List<IncludeInfo> list = map.get(from);
		if (list == null) {
			if (!from.isSystem())
				list = map.get(new IncludeInfo(from.getName(), true));
			if (list == null)
				return Collections.emptyList();
		}
		return list;
	}

	/**
	 * Returns header files that should be used instead of the given one.
	 * 
	 * @param from The header file to be replaced. A system header has to match exactly.
	 *     A non-system header matches both, non-system and system headers.
	 * @return The list of header files ordered by decreasing preference. 
	 */
	public List<IncludeInfo> getMapping(String from) {
		return getMapping(new IncludeInfo(from));
	}

	public IncludeInfo getPreferredMapping(IncludeInfo from) {
		List<IncludeInfo> list = getMapping(from);
		return list == null ? null : list.get(0);
	}

	public boolean isForcedReplacement() {
		return forcedReplacement;
	}

	public boolean isCppOnly() {
		return cppOnly;
	}

	// XXX Define a class containing two Includemaps, week and strong
	public void saveToMemento(IMemento memento) {
		memento.putBoolean(TAG_CPP_ONLY, cppOnly);
		memento.putBoolean(TAG_FORCED_REPLACEMENT, forcedReplacement);
		for (Entry<IncludeInfo, List<IncludeInfo>> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			for (IncludeInfo value : entry.getValue()) {
				IMemento mapping = memento.createChild(TAG_MAPPING);
				mapping.putString(TAG_KEY, key);
				mapping.putString(TAG_VALUE, value.toString());
			}
		}
	}

	public static IncludeMap fromMemento(IMemento memento) {
		Boolean cppOnly = memento.getBoolean(TAG_CPP_ONLY);
		Boolean forcedReplacement = memento.getBoolean(TAG_FORCED_REPLACEMENT);
		IncludeMap includeMap = new IncludeMap(cppOnly, forcedReplacement);
		for (IMemento mapping : memento.getChildren(TAG_MAPPING)) {
			includeMap.addMapping(mapping.getString(TAG_KEY), mapping.getString(TAG_VALUE));
		}
		return includeMap;
	}

	public static IncludeMap fromString(String str) {
		StringReader reader = new StringReader(str);
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return null;
		}
		return fromMemento(memento);
	}
}