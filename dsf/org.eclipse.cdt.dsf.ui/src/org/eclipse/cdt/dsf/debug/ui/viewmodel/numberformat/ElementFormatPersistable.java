/*****************************************************************
 * Copyright (c) 2012, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - initial API and implementation
 *     Marc Khouzam (Ericsson) - Create generic element format persistence (bug 439624) 
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Element format persistable for format settings
 */
public class ElementFormatPersistable implements IPersistableElement, IAdaptable  {

	private static final String ELEMENT_TAG_ID = "element"; //$NON-NLS-1$
	private static final String FORMAT_TAG_ID  = "format";  //$NON-NLS-1$
	
	/** Mapping of element key to individual format */
	private HashMap<String, String> fMap = new HashMap<String, String>();

	public ElementFormatPersistable() {
	}
	
	@Override
	public void saveState(IMemento memento) {
		HashMap<String, String> clone = null;
		synchronized (fMap) {
			clone = new HashMap<String, String>(fMap);
		}
		Iterator<Entry<String, String> > it = clone.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
            IMemento value = memento.createChild(ELEMENT_TAG_ID, entry.getKey());
            value.putString(FORMAT_TAG_ID, entry.getValue());
		}
	}

	public void restore(IMemento memento) {
		IMemento[] list = memento.getChildren(ELEMENT_TAG_ID);
		HashMap<String, String> clone = new HashMap<String, String>();
		for (int i = 0; i < list.length; i++) {
			clone.put(list[i].getID(), list[i].getString(FORMAT_TAG_ID));
		}
		synchronized(fMap) {
			fMap.clear();
			fMap.putAll(clone);
		}
	}

	public String getFormat(String key) {
		if (key == null)
			return null;
		synchronized (fMap) {
			return fMap.get(key);
		}
	}

	public void setFormat(String key, String format) {
		synchronized (fMap) {
			if (format == null) {
				fMap.remove(key);
			} else {
				fMap.put(key, format);
			}
		}
	}

	@Override
	public String getFactoryId() {
		return ElementFormatPersistableFactory.FACTORY_ID;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
    	if (adapter.isInstance(this)) {
			return this;
    	}
		return null;
	}
}
