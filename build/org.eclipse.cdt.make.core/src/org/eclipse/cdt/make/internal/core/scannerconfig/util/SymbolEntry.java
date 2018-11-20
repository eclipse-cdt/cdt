/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.SafeStringInterner;

/**
 * Represents a symbol definition with possible multiple values
 * example:
 * 		LOG_LEVEL
 * 		LOG_LEVEL = 2
 * 		LOG_LEVEL = LOG_BASE + 1
 * @author vhirsl
 */
public class SymbolEntry {
	private static final String UNSPECIFIED_VALUE = "1"; //$NON-NLS-1$
	private String name;
	private Map<String, Boolean> values; // Values can be either in the active (selected) group or in the removed group

	public SymbolEntry(String name, String value, boolean active) {
		this.name = SafeStringInterner.safeIntern(name);
		if (values == null) {
			values = new LinkedHashMap<>(1);
		}
		values.put(SafeStringInterner.safeIntern(value), Boolean.valueOf(active));
	}

	public boolean add(String value, boolean active) {
		Boolean old = values.put(SafeStringInterner.safeIntern(value), Boolean.valueOf(active));
		return old == null || old.booleanValue() != active;
	}

	public void remove(String value) {
		values.remove(value);
	}

	public void removeAll() {
		values = null;
	}

	public List<String> getActive() {
		return get(true, true, true);
	}

	public List<String> getActiveRaw() {
		return get(false, true, true);
	}

	public List<String> getRemoved() {
		return get(true, true, false);
	}

	public List<String> getRemovedRaw() {
		return get(false, true, false);
	}

	public List<String> getAll() {
		return get(true, false, true /*don't care*/);
	}

	public List<String> getAllRaw() {
		return get(false, false, true /*don't care*/);
	}

	/**
	 * Utility function to retrieve values as a set.
	 *
	 * @param format - false = raw
	 * @param subset - false = all
	 * @param active - false = removed
	 * @return List
	 */
	private List<String> get(boolean format, boolean subset, boolean active) {
		List<String> rv = new ArrayList<>(values.size());
		for (String val : values.keySet()) {
			if (subset && (values.get(val)).booleanValue() != active)
				continue;
			if (format) {
				rv.add(name + "=" + (val == null ? UNSPECIFIED_VALUE : val));//$NON-NLS-1$
			} else {
				rv.add(name + (val == null ? "" : "=" + val));//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return rv;
	}

	/**
	 * Returns only value part of all active entries
	 * @return List
	 */
	public List<String> getValuesOnly(boolean active) {
		List<String> rv = new ArrayList<>(values.size());
		for (Object element : values.keySet()) {
			String val = (String) element;
			if ((values.get(val)).booleanValue() == active) {
				rv.add(val == null ? UNSPECIFIED_VALUE : val);
			}
		}
		return rv;
	}

	public int numberOfValues() {
		return values.size();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(name);
		buffer.append(':');
		for (String val : values.keySet()) {
			buffer.append('\t');
			buffer.append((val == null) ? "null" : val);//$NON-NLS-1$
			if ((values.get(val)).booleanValue() == true) {
				buffer.append("(active)");//$NON-NLS-1$
			}
			buffer.append('\n');
		}
		return SafeStringInterner.safeIntern(buffer.toString());
	}
}
