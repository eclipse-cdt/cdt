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

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.index.IFileContentKey;
import org.eclipse.cdt.core.index.IIndexFileLocation;

/**
 * A key that uniquely determines the preprocessed contents of a file. 
 */
public class FileContentKey implements IFileContentKey {
	private final IIndexFileLocation location;
	private final String significantMacrosKey;
	private volatile Map<String, String> significantMacros;

	/**
	 * Creates a file content key with the "#pragma once" semantics.
	 * @param location the file location.
	 */
	public FileContentKey(IIndexFileLocation location) {
		this.location = location;
		this.significantMacrosKey = null;
	}

	/**
	 * Creates a file content key that does not have the "#pragma once" semantics.
	 * @param location the file location.
	 * @param significantMacros the significant macros and their definitions, or <code>null</code>
	 * 	   if the set of significant macros is unknown.
	 */
	public FileContentKey(IIndexFileLocation location, Map<String, String> significantMacros) {
		this.location = location;
		// Encode macros to a string to avoid expensive map comparisons later.
		this.significantMacrosKey = significantMacros != null ?
				String.valueOf(StringMapEncoder.encode(significantMacros)) : null;
		if (significantMacros != null) {
			this.significantMacros = Collections.unmodifiableMap(significantMacros);
		}
	}

	/**
	 * Creates a file content key that does not have the "#pragma once" semantics.
	 * @param location the file location.
	 * @param encodedSignificantMacros the significant macros encoded using
	 *     {@link StringMapEncoder#encode(Map)}.
	 */
	public FileContentKey(IIndexFileLocation location, char[] encodedSignificantMacros) {
		this.location = location;
		this.significantMacrosKey =
				encodedSignificantMacros != null ? String.valueOf(encodedSignificantMacros) : null;
	}

	public IIndexFileLocation getLocation() {
		return location;
	}

	public Map<String, String> getSignificantMacros() {
		if (significantMacros == null && significantMacrosKey != null) {
			significantMacros =
					Collections.unmodifiableMap(StringMapEncoder.decodeMap(significantMacrosKey.toCharArray()));
		}
		return significantMacros;
	}

	@Override
	public int hashCode() {
		return location.hashCode() * 31 +
				(significantMacrosKey == null ? 0 : significantMacrosKey.hashCode());
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
        return true;
	}
}
