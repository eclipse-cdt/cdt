/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import com.ibm.icu.text.Collator;

/**
 * A set of header file substitution rules.
 */
public class SymbolExportMap {
	private static final String TAG_SYMBOL_EXPORT_MAPS = "maps"; //$NON-NLS-1$
	private static final String TAG_SYMBOL_EXPORT_MAP = "map"; //$NON-NLS-1$
	private static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VALUE = "value"; //$NON-NLS-1$
	private static final Collator COLLATOR = Collator.getInstance();

	private final Map<String, Set<IncludeInfo>> map;

	public SymbolExportMap() {
		this.map = new HashMap<>();
	}

	/**
	 * @param keysAndValues an array of keys and values: [key1, value1, key2, value2, ...].
	 *     Keys and values may be optionally surrounded by double quotes or angle brackets.
	 *     Angle brackets indicate a system include.
	 */
	public SymbolExportMap(String[] keysAndValues) {
		if (keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException("More keys than values"); //$NON-NLS-1$
		this.map = new HashMap<>(keysAndValues.length / 2);
		for (int i = 0; i < keysAndValues.length;) {
			String key = keysAndValues[i++];
			addMapping(key, keysAndValues[i++]);
		}
	}

	public SymbolExportMap(SymbolExportMap other) {
		this.map = new HashMap<>(other.map.size());
		addAllMappings(other);
	}

	/**
	 * Indicates that the given symbol is exported by the given header.

	 * @param symbol The symbol represented by its fully qualified name.
	 * @param header The header file exporting the symbol.
	 */
	protected void addMapping(String symbol, IncludeInfo header) {
		if (symbol.equals(header))
			return; // Don't allow mapping to itself.
		Set<IncludeInfo> list = map.get(symbol);
		if (list == null) {
			list = new LinkedHashSet<>();
			map.put(symbol, list);
		}
		list.add(header);
	}

	/**
	 * Indicates that the given symbol is exported by the given header.

	 * @param symbol The symbol represented by its fully qualified name.
	 * @param header The header file exporting the symbol. The header is represented by an include
	 *     name optionally surrounded by double quotes or angle brackets. Angle brackets indicate
	 *     a system include.
	 */
	public void addMapping(String symbol, String header) {
		addMapping(symbol, new IncludeInfo(header));
	}

	/**
	 * Returns header files that should be used instead of the given one.
	 *
	 * @param from The header file to be replaced. A system header has to match exactly.
	 *     A non-system header matches both, non-system and system headers.
	 * @return The list of header files ordered by decreasing preference.
	 */
	public Set<IncludeInfo> getMapping(String from) {
		Set<IncludeInfo> list = map.get(from);
		if (list == null)
			return Collections.emptySet();
		return list;
	}

	/**
	 * Removes exporting headers for a given symbol.
	 *
	 * @param symbol the header file to remove exporting headers for
	 * @return the previous header associated with the symbol, or {@code null} if there were no
	 *     exporting headers.
	 */
	public Set<IncludeInfo> removeMapping(String symbol) {
		return map.remove(symbol);
	}

	/**
	 * Writes the map to a memento.
	 */
	public void saveToMemento(IMemento memento) {
		List<String> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys, COLLATOR);
		for (String key : keys) {
			List<IncludeInfo> values = new ArrayList<>(map.get(key));
			Collections.sort(values);
			for (IncludeInfo value : values) {
				IMemento mapping = memento.createChild(TAG_MAPPING);
				mapping.putString(TAG_KEY, key);
				mapping.putString(TAG_VALUE, value.toString());
			}
		}
	}

	public static SymbolExportMap fromMemento(IMemento memento) {
		SymbolExportMap includeMap = new SymbolExportMap();
		for (IMemento mapping : memento.getChildren(TAG_MAPPING)) {
			String key = mapping.getString(TAG_KEY);
			includeMap.addMapping(key, mapping.getString(TAG_VALUE));
		}
		return includeMap;
	}

	public void addAllMappings(SymbolExportMap other) {
		for (Entry<String, Set<IncludeInfo>> entry : other.map.entrySet()) {
			String source = entry.getKey();
			Set<IncludeInfo> otherTargets = entry.getValue();
			Set<IncludeInfo> targets = map.get(source);
			if (targets == null) {
				targets = new LinkedHashSet<>(otherTargets);
				map.put(source, targets);
			} else {
				targets.addAll(otherTargets);
			}
		}
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		ArrayList<String> symbols = new ArrayList<>(map.keySet());
		Collections.sort(symbols);
		for (String symbol : symbols) {
			buf.append('\n');
			buf.append(symbol);
			buf.append(" exported by "); //$NON-NLS-1$
			List<IncludeInfo> targets = new ArrayList<>(map.get(symbol));
			for (int i = 0; i < targets.size(); i++) {
				if (i > 0)
					buf.append(", "); //$NON-NLS-1$
				buf.append(targets.get(i));
			}
		}
		return buf.toString();
	}

	public Map<String, Set<IncludeInfo>> getMap() {
		return Collections.unmodifiableMap(map);
	}

	public static String serializeMaps(List<SymbolExportMap> maps) {
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_SYMBOL_EXPORT_MAPS);
		for (SymbolExportMap element : maps) {
			element.saveToMemento(memento.createChild(TAG_SYMBOL_EXPORT_MAP));
		}
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			CUIPlugin.log(e);
		}
		return writer.toString();
	}

	public static List<SymbolExportMap> deserializeMaps(String str) {
		StringReader reader = new StringReader(str);
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return Collections.emptyList();
		}

		List<SymbolExportMap> maps = new ArrayList<>();
		for (IMemento element : memento.getChildren(TAG_SYMBOL_EXPORT_MAP)) {
			maps.add(fromMemento(element));
		}
		return maps;
	}
}