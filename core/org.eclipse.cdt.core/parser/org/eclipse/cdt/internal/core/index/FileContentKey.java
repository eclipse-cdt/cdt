/*******************************************************************************
 * Copyright (c) 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IFileContentKey;
import org.eclipse.cdt.core.index.IIndexFileLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A key that uniquely determines the preprocessed contents of a file. 
 */
public class FileContentKey implements IFileContentKey {
	private final IIndexFileLocation location;
	private final Map<String, String> relevantMacros;
	private final String relevantMacrosKey;
	private final String includeGuardMacro;

	/**
	 * @param location the file location.
	 * @param relevantMacros the relevant macros and their definitions, or <code>null</code> if
	 *     the set of relevant macros is unknown.
	 * @param includeGuardMacro the include guard macro.
	 */
	public FileContentKey(IIndexFileLocation location, Map<String, String> relevantMacros,
			String includeGuardMacro) {
		this.location = location;
		this.relevantMacros = relevantMacros != null ?
				Collections.unmodifiableMap(relevantMacros) : null;
		// Encode macros to a string to avoid expensive map comparisons later.
		this.relevantMacrosKey = relevantMacros != null ?
				String.valueOf(StringMapEncoder.encode(relevantMacros)) : null;
		this.includeGuardMacro = includeGuardMacro;
	}

	/**
	 * @param location the file location.
	 * @param encodedRelevantMacros the relevant macros encoded using
	 *     {@link StringMapEncoder#encode(Map)}.
	 * @param includeGuardMacro the include guard macro.
	 */
	public FileContentKey(IIndexFileLocation location, char[] encodedRelevantMacros,
			String includeGuardMacro) {
		this.location = location;
		this.relevantMacros = encodedRelevantMacros != null ?
				Collections.unmodifiableMap(StringMapEncoder.decodeMap(encodedRelevantMacros)) : null;
		this.relevantMacrosKey = encodedRelevantMacros != null ?
				String.valueOf(encodedRelevantMacros) : null;
		this.includeGuardMacro = includeGuardMacro;
	}

	public IIndexFileLocation getLocation() {
		return location;
	}

	public Map<String, String> getRelevantMacros() {
		return relevantMacros;
	}

	public String getIncludeGuardMacro() {
		return includeGuardMacro;
	}

	public Map<String, String> selectRelevantMacros(Map<String, String> macroDictionary) {
		if (relevantMacros == null) {
			return null;
		}
		return selectRelevantMacros(relevantMacros.keySet(), includeGuardMacro, macroDictionary);
	}

	/**
	 * Selects relevant macros from a given macro dictionary.
	 * @param relevantMacroNames the names of the relevant macros.
	 * @param includeGuardMacro the name of the include guard macro.
	 * @param macroDictionary macros and their definitions.
	 * @return Relevant macros and their definitions. Undefined macros have <code>null</code> values
	 * 	   in the map. 
	 */
	public static Map<String, String> selectRelevantMacros(Set<String> relevantMacroNames,
			String includeGuardMacro, Map<String, String> macroDictionary) {
		if (relevantMacroNames.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> relevant = new HashMap<String, String>(relevantMacroNames.size());
		for (String key : relevantMacroNames) {
			String value = key.equals(includeGuardMacro) ? null : macroDictionary.get(key);
			relevant.put(key, value);
		}
		return relevant;
	}

	@Override
	public int hashCode() {
		return location.hashCode() * 31 + (relevantMacrosKey == null ? 0 : relevantMacrosKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileContentKey other = (FileContentKey) obj;
		if (!location.equals(other.location)) {
			return false;
		}
		if (relevantMacrosKey == null) {
			if (other.relevantMacrosKey != null)
				return false;
		} else if (!relevantMacrosKey.equals(other.relevantMacrosKey)) {
			return false;
		}
		if (includeGuardMacro == null) {
		    return other.includeGuardMacro == null;
		}
        return includeGuardMacro.equals(other.includeGuardMacro);
	}
}
