/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


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
	private Set values;
	private String preferredValue;	// the first added value unless otherwise specified
	
	public SymbolEntry(String name) {
		this.name = name;
	}
	public SymbolEntry(String name, String value) {
		this(name);
		if (values == null) {
			values = new HashSet();
		}
		values.add(value);
	}
	public SymbolEntry(String name, String value, boolean preferred) {
		this(name, value);
		if (preferred) {
			preferredValue = value;
		}
	}

	public boolean add(String value) {
		if (values == null) {
			values = new HashSet();
		}
		if (preferredValue == null) {
			preferredValue = value;
		}
		return values.add(value);
	}
	public boolean add(String value, boolean preferred) {
		boolean rc = add(value);
		if (preferred) {
			preferredValue = value;
		}
		return rc;
	}
	public boolean addAll(SymbolEntry se) {
		return values.addAll(se.values);
	}
	
	public void removeAll() {
		values = null;
		preferredValue = null;
	}
	
	public String getPrefered() {
		return name+ "=" + (preferredValue == null ? UNSPECIFIED_VALUE : preferredValue);//$NON-NLS-1$
	}
	public String getPreferedRaw() {
		return name + (preferredValue == null ? "" : "=" + preferredValue);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public Set getAllButPreferred() {
		if (values == null)
			return null;
		Set rv = new HashSet(values.size());
		for (Iterator i = values.iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			if (val.equals(preferredValue))
				continue;
			rv.add(name + "=" + (val == null ? UNSPECIFIED_VALUE : val));//$NON-NLS-1$
		}
		return rv;
	}
	public Set getAllButPreferredRaw() {
		if (values == null)
			return null;
		Set rv = new HashSet(values.size());
		for (Iterator i = values.iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			if (val.equals(preferredValue))
				continue;
			rv.add(name + (val == null ? "" : "=" + val));//$NON-NLS-1$ //$NON-NLS-2$
		}
		return rv;
	}
	public Set getAll() {
		if (values == null)
			return null;
		Set rv = new HashSet(values.size());
		for (Iterator i = values.iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			rv.add(name + "=" + (val == null ? UNSPECIFIED_VALUE : val));//$NON-NLS-1$
		}
		return rv;
	}
	public Set getAllRaw() {
		if (values == null)
			return null;
		Set rv = new HashSet(values.size());
		for (Iterator i = values.iterator(); i.hasNext(); ) {
			String val = (String) i.next();
			rv.add(name + (val == null ? "" : "=" + val));//$NON-NLS-1$ //$NON-NLS-2$
		}
		return rv;
	}
	
}