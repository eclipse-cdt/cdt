/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.preferences;

import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferenceFilter;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Helper class to handle scoped Eclipse preferences for plug-in's. Scoped
 * preferences means a given preference context plus the default preferences
 * scope.
 * <p>
 * On changes a {@link PreferenceChangeEvent} is sent to inform all listeners of the change.
 *
 * @see IEclipsePreferences
 * @see IEclipsePreferences.PreferenceChangeEvent
 * @see IEclipsePreferences.IPreferenceChangeListener
 */
public class ScopedEclipsePreferences {
	/**
	 * The preferences scope qualifier.
	 */
	private final String qualifier;

	/**
	 * The default scope preference node.
	 */
	protected final IEclipsePreferences defaultPrefs;

	/**
	 * The context scope preference node.
	 */
	protected final IEclipsePreferences contextScopePrefs;

	/**
	 * The registered preference change listeners.
	 */
	private final ListenerList listeners = new ListenerList();

	/**
	 * Constructor.
	 * <p>
	 * Initialize the scoped preferences with a new instance scope for the given qualifier. The default
	 * scope is determined by calling <code>DefaultScope().getNode(qualifier)</code>.
	 *
	 * @param qualifier The qualifier for the preferences (in example the unique identifier of a plugin). Must not be <code>null</code>.
	 */
	public ScopedEclipsePreferences(String qualifier) {
		this(InstanceScope.INSTANCE, qualifier);
	}

	/**
	 * Constructor.
	 * <p>
	 * Initialize the scoped preferences with the given scope. The default scope
	 * is determined by calling <code>DefaultScope().getNode(qualifier)</code>.
	 *
	 * @param context The preference scope context. Must not be <code>null</code>.
	 * @param qualifier The qualifier for the preferences (in example the unique identifier of a plugin). Must not be <code>null</code>.
	 */
	public ScopedEclipsePreferences(IScopeContext context, String qualifier) {
		Assert.isNotNull(context);
		Assert.isNotNull(qualifier);
		this.qualifier = qualifier;
		defaultPrefs = DefaultScope.INSTANCE.getNode(getQualifier());
		contextScopePrefs = context.getNode(getQualifier());
	}

	/**
	 * Returns the qualifier that is used to get the preferences.
	 * For plugin preferences, this is the unique identifier of the plugin.
	 */
	protected final String getQualifier() {
		return qualifier;
	}

	/**
	 * Exports the preferences to the stream.
	 * <p>
	 * <b>Note:</b> The stream will be closed after the export.
	 *
	 * @param stream The stream to where preferences and defaults should be exported.
	 */
	public void exportPreferences(OutputStream stream) {
		Assert.isNotNull(stream);
		try {
			IPreferenceFilter filter = new IPreferenceFilter() {
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.preferences.IPreferenceFilter#getScopes()
				 */
				@Override
				public String[] getScopes() {
					return new String[] { InstanceScope.SCOPE };
				}
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.preferences.IPreferenceFilter#getMapping(java.lang.String)
				 */
				@Override
				public Map getMapping(String scope) {
					return null;
				}
			};

			Platform.getPreferencesService().exportPreferences(contextScopePrefs, new IPreferenceFilter[] { filter }, stream);
			stream.close();
		}
		catch (Exception e) {
		}
	}

	/**
	 * Check whether a key is set or not.
	 *
	 * @param key The key to check.
	 * @return <code>null</code> if the key does not exist.
	 */
	public boolean containsKey(String key) {
		return Platform.getPreferencesService().getString(getQualifier(), key, null, null) != null;
	}

	/**
	 * Get a String preference value.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key or the default value if not set.
	 */
	public final String getString(String key) {
		return Platform.getPreferencesService().getString(getQualifier(), key, null, null);
	}

	/**
	 * Get a boolean preference value.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key or the default value if not set.
	 */
	public final boolean getBoolean(String key) {
		return Platform.getPreferencesService().getBoolean(getQualifier(), key, false, null);
	}

	/**
	 * Get an int preference value.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key or the default value if not set.
	 */
	public final int getInt(String key) {
		return Platform.getPreferencesService().getInt(getQualifier(), key, 0, null);
	}

	/**
	 * Get a long preference value.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key or the default value if not set.
	 */
	public final long getLong(String key) {
		return Platform.getPreferencesService().getLong(getQualifier(), key, 0, null);
	}

	/**
	 * Get a default String preference value.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key or <code>null</code>.
	 */
	public final String getDefaultString(String key) {
		return defaultPrefs.get(key, null);
	}

	/**
	 * Get a default boolean preference value.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key or <code>null</code>.
	 */
	public final boolean getDefaultBoolean(String key) {
		return defaultPrefs.getBoolean(key, false);
	}

	/**
	 * Get a default int preference value.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key or <code>null</code>.
	 */
	public final int getDefaultInt(String key) {
		return defaultPrefs.getInt(key, 0);
	}

	/**
	 * Get a default long preference value.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key or <code>null</code>.
	 */
	public final long getDefaultLong(String key) {
		return defaultPrefs.getLong(key, 0);
	}

	/**
	 * Set a String preference value. If the value is <code>null</code> or is equal to
	 * the default value, the entry will be removed.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key.
	 */
	public void putString(String key, String value) {
		String defValue = defaultPrefs.get(key, null);
		String instValue = getString(key);
		if (value == null || value.equals(defValue)) {
			contextScopePrefs.remove(key);
			flushAndNotify(contextScopePrefs, key, instValue, defValue);
		}
		else if (!value.equals(instValue)) {
			contextScopePrefs.put(key, value);
			flushAndNotify(contextScopePrefs, key, instValue, value);
		}
	}

	/**
	 * Set a boolean preference value. If the value is equal the default value,
	 * the entry will be removed.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key.
	 */
	public void putBoolean(String key, boolean value) {
		boolean defValue = defaultPrefs.getBoolean(key, false);
		boolean instValue = getBoolean(key);
		if (value == defValue) {
			contextScopePrefs.remove(key);
			flushAndNotify(contextScopePrefs, key, Boolean.toString(instValue), Boolean.toString(defValue));
		}
		else if (value != instValue) {
			contextScopePrefs.putBoolean(key, value);
			flushAndNotify(contextScopePrefs, key, Boolean.toString(instValue), Boolean.toString(value));
		}
	}

	/**
	 * Set an int preference value. If the value is equal to the default value,
	 * the entry will be removed.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed. The old
	 * and new values are string representation in base 10.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key.
	 */
	public void putInt(String key, int value) {
		int defValue = defaultPrefs.getInt(key, 0);
		int instValue = getInt(key);
		if (value == defValue) {
			contextScopePrefs.remove(key);
			flushAndNotify(contextScopePrefs, key, Integer.toString(instValue), Integer.toString(defValue));
		}
		else if (value != instValue) {
			contextScopePrefs.putInt(key, value);
			flushAndNotify(contextScopePrefs, key, Integer.toString(instValue), Integer.toString(value));
		}
	}

	/**
	 * Set a long preference value. If the given value is equal to the default
	 * value, the entry will be removed.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed. The old
	 * and new values are string representation in base 10.
	 *
	 * @param key The preference key.
	 * @return The value of the preference key.
	 */
	public void putLong(String key, long value) {
		long defValue = defaultPrefs.getLong(key, 0);
		long instValue = getLong(key);
		if (value == defValue) {
			contextScopePrefs.remove(key);
			flushAndNotify(contextScopePrefs, key, Long.toString(instValue), Long.toString(defValue));
		}
		else if (value != instValue) {
			contextScopePrefs.putLong(key, value);
			flushAndNotify(contextScopePrefs, key, Long.toString(instValue), Long.toString(value));
		}
	}

	/**
	 * Set a default String preference value. If the given value is <code>null</code>,
	 * the entry will be removed.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key.
	 */
	public void putDefaultString(String key, String value) {
		String defValue = defaultPrefs.get(key, null);
		if (value == null) {
			defaultPrefs.remove(key);
			flushAndNotify(defaultPrefs, key, defValue, null);
		}
		else if (!value.equals(defValue)) {
			defaultPrefs.put(key, value);
			flushAndNotify(defaultPrefs, key, defValue, value);
		}
	}

	/**
	 * Set a default boolean preference value.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key.
	 */
	public void putDefaultBoolean(String key, boolean value) {
		boolean defValue = defaultPrefs.getBoolean(key, false);
		if (value != defValue) {
			defaultPrefs.putBoolean(key, value);
			flushAndNotify(defaultPrefs, key, Boolean.toString(defValue), Boolean.toString(value));
		}
	}

	/**
	 * Set a default int preference value.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed. The old
	 * and new values are string representation in base 10.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key.
	 */
	public void putDefaultInt(String key, int value) {
		int defValue = defaultPrefs.getInt(key, 0);
		if (value != defValue) {
			defaultPrefs.putInt(key, value);
			flushAndNotify(defaultPrefs, key, Integer.toString(defValue), Integer.toString(value));
		}
	}

	/**
	 * Set a default long preference value.
	 * <p>
	 * A {@link PreferenceChangeEvent} is fired, if the value has changed. The old
	 * and new values are string representation in base 10.
	 *
	 * @param key The preference key.
	 * @return The default value of the preference key.
	 */
	public void putDefaultLong(String key, long value) {
		long defValue = defaultPrefs.getLong(key, 0);
		if (value != defValue) {
			defaultPrefs.putLong(key, value);
			flushAndNotify(defaultPrefs, key, Long.toString(defValue), Long.toString(value));
		}
	}

	/**
	 * Write back the changes to the store and notify all listeners about the changed key.
	 *
	 * @param node The preference node which has changed. Must not be <code>null</code>.
	 * @param key The key of the changed preference. Must not be <code>null</code>.
	 * @param oldValue The old value as a {@link String}, or <code>null</code>.
	 * @param newValue The new value as a {@link String}, or <code>null</code>.
	 */
	protected final void flushAndNotify(IEclipsePreferences node, String key, String oldValue, String newValue) {
		// Flush the preferences to the persistence store
		try { node.flush(); } catch (BackingStoreException e) { /* Ignored on purpose */ }

		// Notify the listeners
		firePreferenceEvent(node, key, oldValue, newValue);
	}

	/**
	 * Register the given listener to receive notifications of preference changes to this node.
	 * Calling this method multiple times with the same listener has no effect. The given listener
	 * argument must not be <code>null</code>.
	 *
	 * @param listener The preference change listener. Must not be <code>null</code>.
	 */
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		Assert.isNotNull(listener);
		listeners.add(listener);
	}

	/**
	 * De-register the given listener from receiving notifications of preference changes
	 * to this node. Calling this method multiple times with the same listener has no
	 * effect. The given listener argument must not be <code>null</code>.
	 *
	 * @param listener The preference change listener. Must not be <code>null</code>.
	 */
	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		Assert.isNotNull(listener);
		listeners.remove(listener);
	}

	/**
	 * Convenience method for notifying the registered preference change listeners.
	 *
	 * @param node The preference node which has changed. Must not be <code>null</code>.
	 * @param key The key of the changed preference. Must not be <code>null</code>.
	 * @param oldValue The old value as a {@link String}, or <code>null</code>.
	 * @param newValue The new value as a {@link String}, or <code>null</code>.
	 */
	protected void firePreferenceEvent(IEclipsePreferences node, String key, String oldValue, String newValue) {
		Assert.isNotNull(node);
		Assert.isNotNull(key);

		// If no listener is registered, we are done here
		if (listeners.isEmpty()) return;

		// Get the list or currently registered listeners
		Object[] l = listeners.getListeners();
		// Create the preference change event
		final PreferenceChangeEvent event = new PreferenceChangeEvent(node, key, oldValue, newValue);
		for (int i = 0; i < l.length; i++) {
			final IPreferenceChangeListener listener = (IPreferenceChangeListener) l[i];
			ISafeRunnable job = new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				@Override
				public void run() throws Exception {
					listener.preferenceChange(event);
				}
			};
			SafeRunner.run(job);
		}
	}

}
