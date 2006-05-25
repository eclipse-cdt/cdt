/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a symbol definition with possible multiple values
 * example:
 * 		LOG_LEVEL
 * 		LOG_LEVEL = 2
 * 		LOG_LEVEL = LOG_BASE + 1
 * @author vhirsl
 */
public class SymbolEntry {
	private static final String UNSPECIFIED_VALUE = "1";	//$NON-NLS-1$
	private String name;
	private Map values;	// Values can be either in the active (selected) group or in the removed group 
	
//	public SymbolEntry(String name) {
//		this.name = name;
//	}

	public SymbolEntry(String name, String value) {
		this(name, value, true);
	}
	public SymbolEntry(String name, String value, boolean active) {
		this.name = name;
		if (values == null) {
			values = new LinkedHashMap(1);
		}
		values.put(value, Boolean.valueOf(active));
	}
	public SymbolEntry(SymbolEntry se) {
		name = se.name;
		// deep copy
		values = new LinkedHashMap(se.values.size());
		for (Iterator i = se.values.keySet().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			Boolean value = (Boolean) se.values.get(key);
			values.put(key, Boolean.valueOf(value.booleanValue()));
		}
	}

	public boolean add(String value) {
		return add(value, true);
	}
	public boolean add(String value, boolean active) {
		boolean rc = false;
		if (!values.containsKey(value)) {
			values.put(value, Boolean.valueOf(active));
			rc = true;
		}
		return rc;
	}
	public void replace(String value, boolean active) {
		values.put(value, Boolean.valueOf(active));
	}

//	private void addAll(SymbolEntry se) {
//		values.putAll(se.values);
//	}
	
	public void remove(String value) {
		values.remove(value);
	}
	public void removeAll() {
		values = null;
	}
	
	public List getActive() {
		return get(true, true, true);
	}
	public List getActiveRaw() {
		return get(false, true, true);
	}
	
	public List getRemoved() {
		return get(true, true, false);
	}
	public List getRemovedRaw() {
		return get(false, true, false);
	}
	
	public List getAll() {
		return get(true, false, true /*don't care*/);
	}
	public List getAllRaw() {
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
	private List get(boolean format, boolean subset, boolean active) {
		List rv = new ArrayList(values.size());
		for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			if (subset && ((Boolean) values.get(val)).booleanValue() != active)
				continue;
			if (format) {
				rv.add(name + "=" + (val == null ? UNSPECIFIED_VALUE : val));//$NON-NLS-1$
			}
			else {
				rv.add(name + (val == null ? "" : "=" + val));//$NON-NLS-1$ //$NON-NLS-2$
			}				
		}
		return rv;
	}
	/**
	 * Returns only value part of all active entries
	 * @return List
	 */
	public List getValuesOnly(boolean active) {
		List rv = new ArrayList(values.size());
		for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			if (((Boolean) values.get(val)).booleanValue() == active) {
				rv.add(val == null ? UNSPECIFIED_VALUE : val);
			}
		}
		return rv;
	}
	
	public int numberOfValues() {
		return values.size();
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer(name);
		buffer.append(':');
		for (Iterator i = values.keySet().iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			buffer.append('\t');
			buffer.append((val == null) ? "null" : val);//$NON-NLS-1$
			if (((Boolean) values.get(val)).booleanValue() == true) {
				buffer.append("(active)");//$NON-NLS-1$
			}
			buffer.append('\n');
		}
		return buffer.toString();
	}
}
