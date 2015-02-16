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

		// print the first level of the properties map only
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(new HashMap<String, Object>(settings));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return settings.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getBooleanProperty(java.lang.String)
	 */
	@Override
	public final boolean getBooleanProperty(String key) {
		Object value = getProperty(key);
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getLongProperty(java.lang.String)
	 */
	@Override
	public final long getLongProperty(String key) {
		Object value = getProperty(key);
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getIntProperty(java.lang.String)
	 */
	@Override
	public final int getIntProperty(String key) {
		Object value = getProperty(key);
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getStringProperty(java.lang.String)
	 */
	@Override
	public final String getStringProperty(String key) {
		Object value = getProperty(key);
		return value instanceof String ? (String)value :
					(value != null ? value.toString() : null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getStringProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public String getStringProperty(String key, String defaultValue) {
		String value = getStringProperty(key);
		return value != null ? value : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getFloatProperty(java.lang.String)
	 */
	@Override
	public final float getFloatProperty(String key) {
		Object value = getProperty(key);
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#getDoubleProperty(java.lang.String)
	 */
	@Override
	public final double getDoubleProperty(String key) {
		Object value = getProperty(key);
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperties(java.util.Map)
	 */
	@Override
	public final void setProperties(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// Change the properties only if they have changed really
		if (this.settings.equals(properties)) {
			return;
		}

		// Clear out all old properties
		this.settings.clear();
		// Apply everything from the given properties
		this.settings.putAll(properties);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#addProperties(java.util.Map)
	 */
	@Override
	public final void addProperties(Map<String, ?> properties) {
		// Apply everything from the given properties
		this.settings.putAll(properties);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, boolean)
	 */
	@Override
	public final boolean setProperty(String key, boolean value) {
		boolean oldValue = getBooleanProperty(key);
		if (oldValue != value) {
			return setProperty(key, Boolean.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, long)
	 */
	@Override
	public final boolean setProperty(String key, long value) {
		long oldValue = getLongProperty(key);
		if (oldValue != value) {
			return setProperty(key, Long.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, int)
	 */
	@Override
	public final boolean setProperty(String key, int value) {
		int oldValue = getIntProperty(key);
		if (oldValue != value) {
			return setProperty(key, Integer.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, float)
	 */
	@Override
	public final boolean setProperty(String key, float value) {
		float oldValue = getFloatProperty(key);
		if (oldValue != value) {
			return setProperty(key, Float.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, double)
	 */
	@Override
	public final boolean setProperty(String key, double value) {
		double oldValue = getDoubleProperty(key);
		if (oldValue != value) {
			return setProperty(key, Double.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setProperty(String key, Object value) {
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
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#clearProperties()
	 */
	@Override
	public final void clearProperties() {
		settings.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return settings.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(String key) {
		Assert.isNotNull(key);
	    return settings.containsKey(key);
	}
}
