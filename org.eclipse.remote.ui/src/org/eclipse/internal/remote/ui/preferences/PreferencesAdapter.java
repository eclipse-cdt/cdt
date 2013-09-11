/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.internal.remote.ui.preferences;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.internal.remote.core.preferences.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Adapts {@link org.eclipse.core.runtime.IEclipsePreferences} to {@link org.eclipse.jface.preference.IPreferenceStore}
 * 
 * @since 3.0
 */
public class PreferencesAdapter implements IPreferenceStore {

	/**
	 * Property change listener. Listens for events of type
	 * {@link org.eclipse.core.runtime.IEclipsePreferences.PreferenceChangeEvent} and fires a
	 * {@link org.eclipse.jface.util.PropertyChangeEvent} on the adapter with arguments from the received event.
	 */
	private class PreferenceChangeListener implements IPreferenceChangeListener {

		/*
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener# propertyChange
		 * (org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
		 */
		public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
			firePropertyChangeEvent(event.getKey(), event.getOldValue(), event.getNewValue());
		}
	}

	/** Listeners on the adapter */
	private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);

	/** Listener on the adapted Preferences */
	private final PreferenceChangeListener fListener = new PreferenceChangeListener();

	/** True iff no events should be forwarded */
	private boolean fSilent;

	/** True if any preferences have changed */
	private boolean fNeedsSaving = false;

	/**
	 * Initialize with the given Preferences.
	 * 
	 * @param preferences
	 *            The preferences to wrap.
	 * @since 4.0
	 */
	public PreferencesAdapter() {
		Preferences.addPreferenceChangeListener(fListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(String name) {
		return Preferences.contains(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		fNeedsSaving = true;
		if (!fSilent) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
			Object[] listeners = fListeners.getListeners();
			for (Object listener : listeners) {
				((IPropertyChangeListener) listener).propertyChange(event);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String name) {
		return Preferences.getBoolean(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDefaultBoolean(String name) {
		return Preferences.getDefaultBoolean(name, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDefaultDouble(String name) {
		return Preferences.getDefaultDouble(name, 0.0);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getDefaultFloat(String name) {
		return Preferences.getDefaultFloat(name, 0.0f);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDefaultInt(String name) {
		return Preferences.getDefaultInt(name, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getDefaultLong(String name) {
		return Preferences.getDefaultLong(name, 0L);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultString(String name) {
		return Preferences.getDefaultString(name, ""); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDouble(String name) {
		return Preferences.getDouble(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getFloat(String name) {
		return Preferences.getFloat(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInt(String name) {
		return Preferences.getInt(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLong(String name) {
		return Preferences.getLong(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String name) {
		return Preferences.getString(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDefault(String name) {
		return Preferences.isDefault(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean needsSaving() {
		return fNeedsSaving;
	}

	/**
	 * {@inheritDoc}
	 */
	public void putValue(String name, String value) {
		try {
			fSilent = true;
			Preferences.setString(name, value);
		} finally {
			fSilent = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, double value) {
		Preferences.setDefaultDouble(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, float value) {
		Preferences.setDefaultFloat(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, int value) {
		Preferences.setDefaultInt(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, long value) {
		Preferences.setDefaultLong(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, String defaultObject) {
		Preferences.setDefaultString(name, defaultObject);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, boolean value) {
		Preferences.setDefaultBoolean(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setToDefault(String name) {
		Preferences.setToDefault(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, double value) {
		Preferences.setDouble(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, float value) {
		Preferences.setFloat(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, int value) {
		Preferences.setInt(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, long value) {
		Preferences.setLong(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, String value) {
		Preferences.setString(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, boolean value) {
		Preferences.setBoolean(name, value);
	}
}
