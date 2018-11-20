/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private ListenerList<IPropertyChangeListener> listenerList = new ListenerList<>();
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

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	@Override
	public boolean contains(String name) {
		return cfg.getOption(name) != null;
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		Object[] listeners = listenerList.getListeners();
		if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) {
				IPropertyChangeListener l = (IPropertyChangeListener) listeners[i];
				l.propertyChange(pe);
			}
		}
	}

	@Override
	public boolean getBoolean(String name) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null
				&& (option.getType() == IConfigureOption.BIN || option.getType() == IConfigureOption.FLAGVALUE)) {
			return Boolean.parseBoolean(option.getValue());
		}
		// otherwise punt
		return getDefaultBoolean(name);
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	@Override
	public double getDefaultDouble(String name) {
		return 0;
	}

	@Override
	public float getDefaultFloat(String name) {
		return 0;
	}

	@Override
	public int getDefaultInt(String name) {
		return 0;
	}

	@Override
	public long getDefaultLong(String name) {
		return 0;
	}

	@Override
	public String getDefaultString(String name) {
		return EMPTY_STRING;
	}

	@Override
	public double getDouble(String name) {
		return 0;
	}

	@Override
	public float getFloat(String name) {
		return 0;
	}

	@Override
	public int getInt(String name) {
		return 0;
	}

	@Override
	public long getLong(String name) {
		return 0;
	}

	@Override
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

	@Override
	public boolean isDefault(String name) {
		return false;
	}

	@Override
	public boolean needsSaving() {
		return isdirty;
	}

	@Override
	public void putValue(String name, String value) {
		setValue(name, value);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.remove(listener);
	}

	protected void setDirty(boolean isdirty) {
		this.isdirty = isdirty;
	}

	@Override
	public void setDefault(String name, double value) {
	}

	@Override
	public void setDefault(String name, float value) {
	}

	@Override
	public void setDefault(String name, int value) {
	}

	@Override
	public void setDefault(String name, long value) {
	}

	@Override
	public void setDefault(String name, String defaultObject) {
	}

	@Override
	public void setDefault(String name, boolean value) {
	}

	@Override
	public void setToDefault(String name) {
	}

	@Override
	public void setValue(String name, double value) {
	}

	@Override
	public void setValue(String name, float value) {
	}

	@Override
	public void setValue(String name, int value) {
	}

	@Override
	public void setValue(String name, long value) {
	}

	@Override
	public void setValue(String name, String value) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null)
			option.setValue(value);
	}

	@Override
	public void setValue(String name, boolean value) {
		IConfigureOption option = cfg.getOption(name);
		if (option != null)
			option.setValue(Boolean.toString(value));
	}
}
