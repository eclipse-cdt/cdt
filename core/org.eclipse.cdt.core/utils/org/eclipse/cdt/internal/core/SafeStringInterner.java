/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interns Strings in a safe manner, checking for nulls first.
 * Does not guard against interning a String that has already been interned.
 *
 * @author crecoskie
 *
 */
public class SafeStringInterner {

	/**
	 * Interns the given String, safely checking for null first.
	 *
	 * @param string
	 * @return String
	 */
	public static String safeIntern(String string) {
		if (string != null) {
			return string.intern();
		}
		return string;
	}

	/**
	 * Interns the Strings in the given array, safely checking for null first.
	 *
	 * @param strArray
	 * @return String[]
	 */
	public static String[] safeIntern(String[] strArray) {
		if (strArray == null)
			return null;

		for (int i = 0; i < strArray.length; i++) {
			strArray[i] = safeIntern(strArray[i]);
		}

		return strArray;
	}

	/**
	 * Returns a new version of the map such that all string keys and values are interned.
	 * @return The map, after modification.
	 */
	@SuppressWarnings("unchecked")
	public static <T> HashMap<String, T> safeIntern(HashMap<String, T> map) {
		if (map == null || map.isEmpty()) {
			return map;
		}

		HashMap<String, T> tempMap = new HashMap<>(map);
		map.clear();
		for (String string : tempMap.keySet()) {
			T value = tempMap.get(string);

			if (value instanceof String) {
				value = (T) safeIntern((String) value);
			}

			map.put(safeIntern(string), value);
		}

		return map;
	}

	/**
	 * Returns a new version of the map such that all string keys and values are interned.
	 *
	 * @param <T>
	 * @param map
	 * @return The map, after modification.
	 */
	@SuppressWarnings("unchecked")
	public static <T> LinkedHashMap<String, T> safeIntern(LinkedHashMap<String, T> map) {
		if (map == null || map.isEmpty()) {
			return map;
		}

		LinkedHashMap<String, T> tempMap = new LinkedHashMap<>(map);
		map.clear();
		for (String string : tempMap.keySet()) {
			T value = tempMap.get(string);

			if (value instanceof String) {
				value = (T) safeIntern((String) value);
			}

			map.put(safeIntern(string), value);
		}

		return map;
	}

	/**
	 * Returns a new version of the map such that all string keys and values are interned.
	 *
	 * @param <T>
	 * @param map
	 * @return The map, after modification.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> safeIntern(Map<String, T> map) {
		if (map == null || map.isEmpty()) {
			return map;
		}

		HashMap<String, T> tempMap = new HashMap<>(map);
		map.clear();
		for (String string : tempMap.keySet()) {
			T value = tempMap.get(string);

			if (value instanceof String) {
				value = (T) safeIntern((String) value);
			}

			map.put(safeIntern(string), value);
		}

		return map;
	}

}
