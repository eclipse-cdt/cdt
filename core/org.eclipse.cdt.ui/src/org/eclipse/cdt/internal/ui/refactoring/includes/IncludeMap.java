/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class IncludeMap {
	private final boolean forcedReplacement;
	private final Map<IncludeInfo, List<IncludeInfo>> map;
	private final boolean cppOnly;

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
}