/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 ********************************************************************************/
package org.eclipse.rse.internal.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * The PreferencesMapper provides an implementation of IPreferenceStore over a "lower level"
 * Preferences store. This is a relatively straightforward delegation.
 * 
 * This class is internal and may not be used or subclassed by users of RSE. This should be used
 * only within RSE as a means of exploiting field editors on preferences that are stored
 * in {@link Preferences}.
 * 
 * @see Preferences
 * @see IPreferenceStore
 */
public class PreferencesMapper implements IPreferenceStore {
	
	private Preferences preferences = null;
	private boolean listening = true;
	private Map listeners = new HashMap();
	
	private class MyPropertyChangeListener implements org.eclipse.core.runtime.Preferences.IPropertyChangeListener {

		private IPropertyChangeListener containedListener = null;
		
		public MyPropertyChangeListener(IPropertyChangeListener containedListener) {
			this.containedListener = containedListener;
		}
		
		public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			if (listening) {
				String property = event.getProperty();
				Object oldValue = event.getOldValue();
				Object newValue = event.getNewValue();
				PropertyChangeEvent newEvent = new PropertyChangeEvent(this, property, oldValue, newValue);
				containedListener.propertyChange(newEvent);
			}
		}
		
	}
	
	public PreferencesMapper(Preferences preferences) {
		this.preferences = preferences;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (!listeners.containsKey(listener)) {
			MyPropertyChangeListener l = new MyPropertyChangeListener(listener);
			listeners.put(listener, l);
			preferences.addPropertyChangeListener(l);
		}
	}

	public boolean contains(String name) {
		return preferences.contains(name);
	}

	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		for (Iterator z = listeners.keySet().iterator(); z.hasNext();) {
			IPropertyChangeListener listener = (IPropertyChangeListener) z.next();
			PropertyChangeEvent e = new PropertyChangeEvent(this, name, oldValue, newValue);
			listener.propertyChange(e);
		}
	}

	public boolean getBoolean(String name) {
		return preferences.getBoolean(name);
	}

	public boolean getDefaultBoolean(String name) {
		return preferences.getDefaultBoolean(name);
	}

	public double getDefaultDouble(String name) {
		return preferences.getDefaultDouble(name);
	}

	public float getDefaultFloat(String name) {
		return preferences.getDefaultFloat(name);
	}

	public int getDefaultInt(String name) {
		return preferences.getDefaultInt(name);
	}

	public long getDefaultLong(String name) {
		return preferences.getDefaultLong(name);
	}

	public String getDefaultString(String name) {
		return preferences.getDefaultString(name);
	}

	public double getDouble(String name) {
		return preferences.getDouble(name);
	}

	public float getFloat(String name) {
		return preferences.getFloat(name);
	}

	public int getInt(String name) {
		return preferences.getInt(name);
	}

	public long getLong(String name) {
		return preferences.getLong(name);
	}

	public String getString(String name) {
		return null;
	}

	public boolean isDefault(String name) {
		return preferences.isDefault(name);
	}

	public boolean needsSaving() {
		return preferences.needsSaving();
	}

	public void putValue(String name, String value) {
		listening = false;
		preferences.setValue(name, value);
		listening = true;
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		Object candidate = listeners.get(listener);
		if (candidate instanceof MyPropertyChangeListener) {
			MyPropertyChangeListener l = (MyPropertyChangeListener) candidate;
			preferences.removePropertyChangeListener(l);
		}
		listeners.remove(listener);
	}

	public void setDefault(String name, double value) {
		preferences.setDefault(name, value);
	}

	public void setDefault(String name, float value) {
		preferences.setDefault(name, value);
	}

	public void setDefault(String name, int value) {
		preferences.setDefault(name, value);
	}

	public void setDefault(String name, long value) {
		preferences.setDefault(name, value);
	}

	public void setDefault(String name, String defaultObject) {
		preferences.setDefault(name, defaultObject);
	}

	public void setDefault(String name, boolean value) {
		preferences.setDefault(name, value);
	}

	public void setToDefault(String name) {
		preferences.setToDefault(name);
	}

	public void setValue(String name, double value) {
		preferences.setValue(name, value);
	}

	public void setValue(String name, float value) {
		preferences.setValue(name, value);
	}

	public void setValue(String name, int value) {
		preferences.setValue(name, value);
	}

	public void setValue(String name, long value) {
		preferences.setValue(name, value);
	}

	public void setValue(String name, String value) {
		preferences.setValue(name, value);
	}

	public void setValue(String name, boolean value) {
		preferences.setValue(name, value);
	}

}
