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
	private final boolean pragmaOnceSemantics;
	private final Map<String, String> significantMacros;
	private final String significantMacrosKey;

	/**
	 * Creates a file content key with the "#pragma once" semantics.
	 * @param location
	 */
	public FileContentKey(IIndexFileLocation location) {
		this.location = location;
		this.pragmaOnceSemantics = true;
		this.significantMacros = null;
		this.significantMacrosKey = null;
	}

	/**
	 * Creates a file content key.
	 * @param location the file location.
	 * @param hasPragmaOnceSemantics the boolean flag indicating whether the file may be parsed
	 *     in different variants or not.
	 * @param significantMacros the significant macros and their definitions, or <code>null</code>
	 * 	   if the set of significant macros is unknown. This parameter is ignored if
	 *      hasPragmaOnceSemantics is <code>true</code>.
	 */
	public FileContentKey(IIndexFileLocation location, boolean hasPragmaOnceSemantics,
			Map<String, String> significantMacros) {
		this.location = location;
		this.pragmaOnceSemantics = hasPragmaOnceSemantics;
		this.significantMacros = !pragmaOnceSemantics && significantMacros != null ?
				Collections.unmodifiableMap(significantMacros) : null;
		// Encode macros to a string to avoid expensive map comparisons later.
		this.significantMacrosKey = !pragmaOnceSemantics && significantMacros != null ?
				String.valueOf(StringMapEncoder.encode(significantMacros)) : null;
	}

	/**
	 * Creates a file content key that does not have the "#pragma once" semantics.
	 * @param location the file location.
	 * @param encodedSignificantMacros the significant macros encoded using
	 *     {@link StringMapEncoder#encode(Map)}.
	 */
	public FileContentKey(IIndexFileLocation location, char[] encodedSignificantMacros) {
		this.location = location;
		this.pragmaOnceSemantics = false;
		this.significantMacros = encodedSignificantMacros != null ?
				Collections.unmodifiableMap(StringMapEncoder.decodeMap(encodedSignificantMacros)) : null;
		this.significantMacrosKey = encodedSignificantMacros != null ?
				String.valueOf(encodedSignificantMacros) : null;
	}

	public IIndexFileLocation getLocation() {
		return location;
	}

	public Map<String, String> getSignificantMacros() {
		return significantMacros;
	}

	public boolean hasPragmaOnceSemantics() {
		return pragmaOnceSemantics;
	}

	public Map<String, String> selectSignificantMacros(Map<String, String> macroDictionary) {
		if (significantMacros == null || pragmaOnceSemantics) {
			return null;
		}
		return selectSignificantMacros(significantMacros.keySet(), macroDictionary);
	}

	/**
	 * Selects significant macros from a given macro dictionary.
	 * @param relevantMacroNames the names of the significant macros.
	 * @param macroDictionary macros and their definitions.
	 * @return Significant macros and their definitions. Undefined macros have <code>null</code> values
	 * 	   in the map. 
	 */
	public static Map<String, String> selectSignificantMacros(Set<String> relevantMacroNames,
			Map<String, String> macroDictionary) {
		if (relevantMacroNames.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> relevant = new HashMap<String, String>(relevantMacroNames.size());
		for (String key : relevantMacroNames) {
			relevant.put(key, macroDictionary.get(key));
		}
		return relevant;
	}

	@Override
	public int hashCode() {
		return location.hashCode() * 31 + (significantMacrosKey == null ? 0 : significantMacrosKey.hashCode());
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
		if (significantMacrosKey == null) {
			if (other.significantMacrosKey != null)
				return false;
		} else if (!significantMacrosKey.equals(other.significantMacrosKey)) {
			return false;
		}
        return pragmaOnceSemantics == other.pragmaOnceSemantics;
	}
}
