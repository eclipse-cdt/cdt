/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.ui.IMemento;

/**
 * A {@link IDialogSettings} based {@link ISettingsStore}.
 * 
 * Setting Store based on IMemento. IMemento documentations says only alpha numeric
 * values may be used as keys. Therefore the implementation converts dots (.) into
 * child elements of the memento.
 * 
 * @author Michael Scharf
 */
class SettingsStore implements ISettingsStore {

	private static final String KEYS = "_keys_"; //$NON-NLS-1$
	final private Map fMap=new HashMap();
	public SettingsStore(IMemento memento) {
		if(memento==null)
			return;
		// load all keys ever used from the memento
		String keys=memento.getString(KEYS);
		if(keys!=null) {
			String[] keyNames=keys.split(","); //$NON-NLS-1$
			for (int i = 0; i < keyNames.length; i++) {
				String key=keyNames[i];
				if(!KEYS.equals(key)) {
					// get the dot separated elements
					String[] path=key.split("\\."); //$NON-NLS-1$
					IMemento m=memento;
					// iterate over all but the last segment and get the children...
					for(int iPath=0; m!=null && iPath+1<path.length; iPath++) {
						m=m.getChild(path[iPath]);
					}
					if(m!=null) {
						// cache the value in the map
						fMap.put(key,m.getString(path[path.length-1]));
					}
				}
			}
		}
	}

	public String get(String key) {
		return get(key,null);
	}
	public String get(String key, String defaultValue) {
		String value = (String) fMap.get(key);
		if ((value == null) || (value.equals(""))) //$NON-NLS-1$
			return defaultValue;

		return value;
	}

	public void put(String key, String value) {
		if(!key.matches("^[\\w.]+$")) //$NON-NLS-1$
			throw new IllegalArgumentException("Key '"+key+"' is not alpha numeric or '.'!"); //$NON-NLS-1$ //$NON-NLS-2$
		// null values remove the key from the map
		if ((value == null) || (value.equals(""))) //$NON-NLS-1$
			fMap.remove(key);
		else
			fMap.put(key, value);
	}
	/**
	 * Save the state into memento.
	 * 
	 * @param memento Memento to save state into.
	 */
	public void saveState(IMemento memento) {
		String[] keyNames=(String[]) fMap.keySet().toArray(new String[fMap.size()]);
		Arrays.sort(keyNames);
		StringBuffer buffer=new StringBuffer();
		for (int i = 0; i < keyNames.length; i++) {
			String key=keyNames[i];
			String[] path=key.split("\\."); //$NON-NLS-1$
			IMemento m=memento;
			// iterate over all but the last segment and get the children...
			for(int iPath=0; iPath+1<path.length; iPath++) {
				IMemento child=m.getChild(path[iPath]);
				// if the child does not exist, create it
				if(child==null)
					child=m.createChild(path[iPath]);
				m=child;
			}
			// use the last element in path as key of the child memento
			m.putString(path[path.length-1], (String) fMap.get(key));
			// construct the string for the keys
			if(i>0)
				buffer.append(","); //$NON-NLS-1$
			buffer.append(key);
		}
		// save the keys we have used.
		memento.putString(KEYS, buffer.toString());
	}
}
