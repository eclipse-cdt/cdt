/*****************************************************************
 * Copyright (c) 2012 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - DSF-GDB register format persistence (bug 395909)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Element format persistable for format settings
 */
class ElementFormatPersistable implements IPersistableElement, IAdaptable  {

	private HashMap<String, String> map = new HashMap<String, String>();
	private String factoryId;

	public ElementFormatPersistable(String factoryId) {
		this.factoryId = factoryId;
	}
	
	@Override
	public void saveState(IMemento memento) {
		HashMap<String, String> clone = null;
		synchronized (map) {
			clone = new HashMap<String, String>(map);
		}
		Iterator<Entry<String, String> > it = clone.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
            IMemento value = memento.createChild("element", entry.getKey()); //$NON-NLS-1$
            value.putString("format", entry.getValue()); //$NON-NLS-1$
		}
	}

	void restore(IMemento memento) {
		IMemento[] list = memento.getChildren("element"); //$NON-NLS-1$
		HashMap<String, String> clone = new HashMap<String, String>();
		for (int i = 0; i < list.length; i++) {
			clone.put(list[i].getID(), list[i].getString("format")); //$NON-NLS-1$
		}
		synchronized(map) {
			map.clear();
			map.putAll(clone);
		}
	}

	String getFormat(String key) {
		if (key == null)
			return null;
		synchronized (map) {
			return map.get(key);
		}
	}

	void setFormat(String key, String format) {
		synchronized (map) {
			if (format == null) {
				map.remove(key);
			} else {
				map.put(key, format);
			}
		}
	}

	@Override
	public String getFactoryId() {
		return factoryId;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
    	if (adapter.isInstance(this)) {
			return this;
    	}
		return null;
	}
}
