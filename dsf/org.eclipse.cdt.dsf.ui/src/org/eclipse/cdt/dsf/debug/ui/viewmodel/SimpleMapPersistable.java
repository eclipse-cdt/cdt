/*****************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Generic persistable for storing a map of simple values.
 * <br>
 * Currently supported value types are {@link Integer} and {@link String}.
 *
 * @since 2.5
 */
public class SimpleMapPersistable<V> implements IPersistableElement, IAdaptable {

	private static final String KEY_TYPE = "type"; //$NON-NLS-1$
	private static final String KEY_NAME = "name"; //$NON-NLS-1$
	private static final String KEY_VALUE = "value"; //$NON-NLS-1$

	private Class<V> fType;
	private Map<String, V> fValues = new TreeMap<>();

	@SuppressWarnings("unchecked")
	public SimpleMapPersistable(IMemento memento) throws CoreException {
		IMemento type = memento.getChild(KEY_TYPE);
		if (type == null) {
			throw new CoreException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"Missing key for type.", null)); //$NON-NLS-1$
		}

		try {
			fType = (Class<V>) Class.forName(type.getTextData());
		} catch (ClassNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					e.getMessage(), e));
		}

		IMemento[] list = memento.getChildren(KEY_NAME);
		Map<String, V> values = new TreeMap<>();
		for (IMemento elem : list) {
			values.put(elem.getID(), getValue(elem));
		}

		synchronized (fValues) {
			// We should not assign 'values' directly to 'fValues'
			// if we want synchronization to work.  Instead, we must use
			// the same map as before for 'fValues'
			fValues.clear();
			fValues.putAll(values);
		}
	}

	public SimpleMapPersistable(Class<V> type) {
		fType = type;
	}

	@Override
	public void saveState(IMemento memento) {
		Map<String, V> values = null;
		synchronized (fValues) {
			values = new TreeMap<>(fValues);
		}

		IMemento type = memento.createChild(KEY_TYPE);
		synchronized (fType) {
			type.putTextData(fType.getName());
		}
		for (Map.Entry<String, V> entry : values.entrySet()) {
			IMemento value = memento.createChild(KEY_NAME, entry.getKey());
			putValue(value, entry.getValue());
		}
	}

	private void putValue(IMemento memento, Object value) {
		if (value instanceof String) {
			memento.putString(KEY_VALUE, (String) value);
		} else if (value instanceof Integer) {
			memento.putInteger(KEY_VALUE, (Integer) value);
		} else {
			assert false;
		}
	}

	@SuppressWarnings("unchecked")
	private V getValue(IMemento memento) {
		synchronized (fType) {
			if (String.class.equals(fType)) {
				return (V) memento.getString(KEY_VALUE);
			} else if (Integer.class.equals(fType)) {
				return (V) memento.getInteger(KEY_VALUE);
			} else {
				assert false;
			}
		}
		return null;
	}

	public V getValue(String key) {
		if (key == null)
			return null;
		synchronized (fValues) {
			return fValues.get(key);
		}
	}

	public void setValue(String key, V value) {
		synchronized (fValues) {
			if (value == null) {
				fValues.remove(key);
			} else {
				fValues.put(key, value);
			}
		}
	}

	@Override
	public String getFactoryId() {
		return SimpleMapPersistableFactory.getFactoryId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isInstance(this)) {
			return (T) this;
		}
		return null;
	}
}
