/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author jjohnstn
 *
 */
public class AutotoolsConfigurePrefStore implements IPreferenceStore {

	public final static String EMPTY_STRING = "";
	public final static String ALL_OPTIONS_ID = EMPTY_STRING;
	
	private static AutotoolsConfigurePrefStore instance = null;
	private ToolListElement selectedElement;
	private IAConfiguration cfg;
	private ListenerList listenerList = new ListenerList();
	private boolean isdirty;

	private AutotoolsConfigurePrefStore() {
		// private constructor
	}
	
	public static AutotoolsConfigurePrefStore getInstance() {
		if (instance == null)
			instance = new AutotoolsConfigurePrefStore();
		return instance;
	}
	
	public void setSelection(IAConfiguration cfg, ToolListElement element) {
		this.cfg = cfg;
		selectedElement = element;
	}
	
	/* (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return cfg.getOption(name) != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
		Object[] listeners = listenerList.getListeners();
		if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) 
		{
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) 
			{
				IPropertyChangeListener l = (IPropertyChangeListener)listeners[i];
				l.propertyChange( pe );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null && (option.getType() == IConfigureOption.BIN ||
				option.getType() == IConfigureOption.FLAGVALUE)) {
			return Boolean.parseBoolean(option.getValue());
		}
		// otherwise punt
		return getDefaultBoolean(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		if (name.equals(ALL_OPTIONS_ID) && selectedElement.getType() == IConfigureOption.TOOL) {
			return cfg.getToolParameters(selectedElement.getName());
		}
		IConfigureOption option = cfg.getOption(name);
		if (option != null) {
			return option.getValue();
		}
		// otherwise punt
		return getDefaultString(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return isdirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String value) {
		setValue(name, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.remove(listener);
	}

	protected void setDirty(boolean isdirty) {
		this.isdirty = isdirty;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String value) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null)
			option.setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean value) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null)
			option.setValue(Boolean.toString(value));
	}
}
