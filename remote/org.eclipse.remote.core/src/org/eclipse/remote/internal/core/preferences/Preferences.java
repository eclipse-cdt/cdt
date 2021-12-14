/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.remote.internal.core.RemoteCorePlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Convenience class to facilitate using the new {@link IEclipsePreferences} story. Adapted from
 * org.eclipse.debug.internal.core.Preferences.
 * 
 * @since 5.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class Preferences {

	private static final IScopeContext[] contexts = new IScopeContext[] { DefaultScope.INSTANCE, InstanceScope.INSTANCE };

	private static final int DEFAULT_CONTEXT = 0;
	private static final int INSTANCE_CONTEXT = 1;

	private static final String fQualifier = RemoteCorePlugin.getUniqueIdentifier();

	/**
	 * Adds the given preference listener to the {@link DefaultScope} and the {@link InstanceScope}
	 * 
	 * @param listener
	 */
	public static void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).addPreferenceChangeListener(listener);
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).addPreferenceChangeListener(listener);
	}

	/**
	 * Returns whether the named preference is know in the preference store.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean contains(String name) {
		return (contexts[INSTANCE_CONTEXT].getNode(fQualifier).get(name, null) != null || contexts[DEFAULT_CONTEXT].getNode(
				fQualifier).get(name, null) != null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static boolean getBoolean(String key) {
		return Platform.getPreferencesService().getBoolean(fQualifier, key, false, null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static byte[] getByteArray(String key) {
		return Platform.getPreferencesService().getByteArray(fQualifier, key, null, null);
	}

	/**
	 * Returns the default boolean value stored in the {@link DefaultScope} for
	 * the given key or the specified default value if the key does not appear
	 * in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the boolean value set in the {@link DefaultScope} for the given
	 *         key, or the specified default value.
	 */
	public static synchronized boolean getDefaultBoolean(String key, boolean defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getBoolean(key, defaultvalue);
	}

	/**
	 * Returns the default byte array value stored in the {@link DefaultScope} for the given key or the specified default value if
	 * the key does not
	 * appear in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the byte array value set in the {@link DefaultScope} for the
	 *         given key, or the specified default value.
	 */
	public static synchronized byte[] getDefaultByteArray(String key, byte[] defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getByteArray(key, defaultvalue);
	}

	/**
	 * Returns the default double value stored in the {@link DefaultScope} for
	 * the given key or the specified default value if the key does not appear
	 * in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the double value set in the {@link DefaultScope} for the given
	 *         key, or the specified default value.
	 */
	public static synchronized double getDefaultDouble(String key, double defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getDouble(key, defaultvalue);
	}

	/**
	 * Returns the default float value stored in the {@link DefaultScope} for
	 * the given key or the specified default value if the key does not appear
	 * in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the float value set in the {@link DefaultScope} for the given
	 *         key, or the specified default value.
	 */
	public static synchronized float getDefaultFloat(String key, float defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getFloat(key, defaultvalue);
	}

	/**
	 * Returns the default integer value stored in the {@link DefaultScope} for
	 * the given key or the specified default value if the key does not appear
	 * in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the integer value set in the {@link DefaultScope} for the given
	 *         key, or the specified default value.
	 */
	public static synchronized int getDefaultInt(String key, int defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getInt(key, defaultvalue);
	}

	/**
	 * Returns the default long value stored in the {@link DefaultScope} for the
	 * given key or the specified default value if the key does not appear in
	 * the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the long value set in the {@link DefaultScope} for the given key,
	 *         or the specified default value.
	 */
	public static synchronized long getDefaultLong(String key, long defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).getLong(key, defaultvalue);
	}

	/**
	 * Returns the default string value stored in the {@link DefaultScope} for
	 * the given key or the specified default value if the key does not appear
	 * in the {@link DefaultScope}
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the string value set in the {@link DefaultScope} for the given
	 *         key, or the specified default value.
	 */
	public static synchronized String getDefaultString(String key, String defaultvalue) {
		return contexts[DEFAULT_CONTEXT].getNode(fQualifier).get(key, defaultvalue);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static double getDouble(String key) {
		return Platform.getPreferencesService().getDouble(fQualifier, key, 0.0, null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param fQualifier
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static float getFloat(String key) {
		return Platform.getPreferencesService().getFloat(fQualifier, key, 0.0f, null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param fQualifier
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static int getInt(String key) {
		return Platform.getPreferencesService().getInt(fQualifier, key, 0, null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static long getLong(String key) {
		return Platform.getPreferencesService().getLong(fQualifier, key, 0L, null);
	}

	/**
	 * Returns the value in the preference store for the given key. If the key
	 * is not defined then return the default value. Use the canonical scope
	 * lookup order for finding the preference value.
	 * 
	 * @param key
	 * @param defaultvalue
	 * 
	 * @return the value of the preference or the given default value
	 */
	public static String getString(String key) {
		return Platform.getPreferencesService().getString(fQualifier, key, null, null);
	}

	/**
	 * Returns true if the named preference has the default value.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isDefault(String name) {
		String defVal = contexts[DEFAULT_CONTEXT].getNode(fQualifier).get(name, null);
		if (defVal != null) {
			String val = contexts[INSTANCE_CONTEXT].getNode(fQualifier).get(name, null);
			return (val != null && val.equals(defVal));
		}
		return false;
	}

	/**
	 * Removes the given preference listener from the {@link DefaultScope} and
	 * the {@link InstanceScope}
	 * 
	 * @param listener
	 */
	public static void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).removePreferenceChangeListener(listener);
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).removePreferenceChangeListener(listener);
	}

	/**
	 * Save the preferences for the given plugin identifier. It should be noted
	 * that all pending preference changes will be flushed with this method.
	 */
	public static synchronized void savePreferences() {
		try {
			contexts[DEFAULT_CONTEXT].getNode(fQualifier).flush();
			contexts[INSTANCE_CONTEXT].getNode(fQualifier).flush();
		} catch (BackingStoreException bse) {
			RemoteCorePlugin.log(bse);
		}
	}

	/**
	 * Sets a boolean preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setBoolean(String key, boolean value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putBoolean(key, value);
	}

	/**
	 * Sets a byte array preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setByteArray(String key, byte[] value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putByteArray(key, value);
	}

	/**
	 * Sets a boolean in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultBoolean(String key, boolean value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putBoolean(key, value);
	}

	/**
	 * Sets a byte array in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultByteArray(String key, byte[] value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putByteArray(key, value);
	}

	/**
	 * Sets a double in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultDouble(String key, double value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putDouble(key, value);
	}

	/**
	 * Sets a float in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultFloat(String key, float value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putFloat(key, value);
	}

	/**
	 * Sets a integer in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultInt(String key, int value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putInt(key, value);
	}

	/**
	 * Sets a long in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultLong(String key, long value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).putLong(key, value);
	}

	/**
	 * Sets a string in the {@link DefaultScope}
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the new value
	 */
	public static synchronized void setDefaultString(String key, String value) {
		contexts[DEFAULT_CONTEXT].getNode(fQualifier).put(key, value);
	}

	/**
	 * Sets a double preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setDouble(String key, double value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putDouble(key, value);
	}

	/**
	 * Sets a float preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setFloat(String key, float value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putFloat(key, value);
	}

	/**
	 * Sets a integer preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setInt(String key, int value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putInt(key, value);
	}

	/**
	 * Sets a long preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setLong(String key, long value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).putLong(key, value);
	}

	/**
	 * Sets a string preference in the {@link InstanceScope}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static synchronized void setString(String key, String value) {
		contexts[INSTANCE_CONTEXT].getNode(fQualifier).put(key, value);
	}

	/**
	 * Sets the given preference to its default value. This is done by removing
	 * any set value from the {@link InstanceScope}. Has no effect if the given
	 * key is <code>null</code>.
	 * 
	 * @param key
	 *            the key for the preference
	 */
	public static synchronized void setToDefault(String key) {
		if (key != null) {
			contexts[INSTANCE_CONTEXT].getNode(fQualifier).remove(key);
		}
	}

	/**
	 * Constructor
	 */
	private Preferences() {
		// no direct instantiation
	}
}
