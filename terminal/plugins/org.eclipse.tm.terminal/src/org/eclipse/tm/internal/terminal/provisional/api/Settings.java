/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Terminal connection settings implementation.
 */
public class Settings extends PlatformObject implements ISettings {
	/**
	 * A map of settings. The keys are always strings, the value might be any object.
	 */
	private Map<String, Object> settings = new HashMap<String, Object>();

	/**
	 * Constructor.
	 */
	public Settings() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equals = super.equals(obj);
		if (!equals && obj instanceof Settings) {
			return settings.equals(((Settings)obj).settings);
		}
		return equals;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return settings.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();

		// print the first level of the settings map only
		buffer.append("settings={"); //$NON-NLS-1$
		for (String key : settings.keySet()) {
			buffer.append(key);
			buffer.append("="); //$NON-NLS-1$

			Object value = settings.get(key);
			if (value instanceof Map || value instanceof ISettings) {
				buffer.append("{...}"); //$NON-NLS-1$
			} else {
				buffer.append(value);
			}

			buffer.append(", "); //$NON-NLS-1$
		}
		if (buffer.toString().endsWith(", ")) { //$NON-NLS-1$
			buffer.deleteCharAt(buffer.length() - 1);
			buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append("}"); //$NON-NLS-1$

		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getAll()
	 */
	@Override
	public Map<String, Object> getAll() {
		return Collections.unmodifiableMap(new HashMap<String, Object>(settings));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		return settings.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getBoolean(java.lang.String)
	 */
	@Override
	public final boolean getBoolean(String key) {
		Object value = get(key);
		if (value instanceof Boolean) {
			return ((Boolean)value).booleanValue();
		}
		if (value instanceof String) {
			String val = ((String)value).trim();
			return "TRUE".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val) ||  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"JA".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getLong(java.lang.String)
	 */
	@Override
	public final long getLong(String key) {
		Object value = get(key);
		try {
			if (value instanceof Long) {
				return ((Long)value).longValue();
			}
			if (value instanceof Number) {
				return ((Number)value).longValue();
			}
			if (value != null) {
				return Long.decode(value.toString()).longValue();
			}
		} catch (Exception e) {
			/* ignored on purpose */
		}

		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getInt(java.lang.String)
	 */
	@Override
	public final int getInt(String key) {
		Object value = get(key);
		try {
			if (value instanceof Integer) {
				return ((Integer)value).intValue();
			}
			if (value instanceof Number) {
				return ((Number)value).intValue();
			}
			if (value != null) {
				return Integer.decode(value.toString()).intValue();
			}
		} catch (Exception e) {
			/* ignored on purpose */
		}

		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getString(java.lang.String)
	 */
	@Override
	public final String getString(String key) {
		Object value = get(key);
		return value instanceof String ? (String)value : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getString(java.lang.String, java.lang.String)
	 */
	@Override
	public String getString(String key, String defaultValue) {
		String value = getString(key);
		return value != null ? value : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getFloat(java.lang.String)
	 */
	@Override
	public final float getFloat(String key) {
		Object value = get(key);
		try {
			if (value instanceof Float) {
				return ((Float)value).floatValue();
			}
			if (value instanceof Number) {
				return ((Number)value).floatValue();
			}
			if (value != null) {
				return Float.parseFloat(value.toString());
			}
		} catch (Exception e) {
			/* ignored on purpose */
		}

		return Float.NaN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#getDouble(java.lang.String)
	 */
	@Override
	public final double getDouble(String key) {
		Object value = get(key);
		try {
			if (value instanceof Double) {
				return ((Double)value).doubleValue();
			}
			if (value instanceof Number) {
				return ((Number)value).doubleValue();
			}
			if (value != null) {
				return Double.parseDouble(value.toString());
			}
		} catch (Exception e) {
			/* ignored on purpose */
		}

		return Double.NaN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.util.Map)
	 */
	@Override
	public final void set(Map<String, Object> settings) {
		Assert.isNotNull(settings);

		// Change the settings only if they have changed really
		if (this.settings.equals(settings)) {
			return;
		}

		// Clear out all old settings
		this.settings.clear();
		// Apply everything from the given settings
		this.settings.putAll(settings);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#addAll(java.util.Map)
	 */
	@Override
	public final void addAll(Map<String, ?> settings) {
		// Apply everything from the given settings
		this.settings.putAll(settings);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, boolean)
	 */
	@Override
	public final boolean set(String key, boolean value) {
		boolean oldValue = getBoolean(key);
		if (oldValue != value) {
			return set(key, Boolean.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, long)
	 */
	@Override
	public final boolean set(String key, long value) {
		long oldValue = getLong(key);
		if (oldValue != value) {
			return set(key, Long.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, int)
	 */
	@Override
	public final boolean set(String key, int value) {
		int oldValue = getInt(key);
		if (oldValue != value) {
			return set(key, Integer.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, float)
	 */
	@Override
	public final boolean set(String key, float value) {
		float oldValue = getFloat(key);
		if (oldValue != value) {
			return set(key, Float.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, double)
	 */
	@Override
	public final boolean set(String key, double value) {
		double oldValue = getDouble(key);
		if (oldValue != value) {
			return set(key, Double.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#set(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean set(String key, Object value) {
		Assert.isNotNull(key);

		Object oldValue = settings.get(key);
		if ((oldValue == null && value != null) || (oldValue != null && !oldValue.equals(value))) {
			if (value != null) {
				settings.put(key, value);
			} else {
				settings.remove(key);
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#clear()
	 */
	@Override
	public final void clear() {
		settings.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return settings.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettings#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(String key) {
		Assert.isNotNull(key);
	    return settings.containsKey(key);
	}
}
