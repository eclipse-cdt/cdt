/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Uwe Stieber (Wind River) - Extend API to allow storage of non-string settings
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;

/**
 * The settings contains the state of a connection. The content of the settings
 * is not the persisted state of the connection. Storing data in the settings
 * does not make any assumption about possibly persisting the connection state.
 * Connection persistence has to be implemented by the UI container embedding
 * the Terminal control.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public interface ISettings extends IAdaptable {

	/**
	 * Set the settings from the given map. Calling this method
	 * will overwrite all previous set settings.
	 * <p>
	 * <b>Note:</b> The method will have no effect if the given settings are the
	 * same as the already set settings.
	 *
	 * @param settings The map of settings to set. Must not be <code>null</code>.
	 */
	public void set(Map<String, Object> settings);

	/**
	 * Adds all settings from the given map. If a setting already exist
	 * in the settings, than the value of the setting is overwritten.
	 *
	 * @param settings The map of settings to add. Must not be <code>null</code>.
	 */
	public void addAll(Map<String, ?> settings);

	/**
	 * Stores the setting under the given key using the given value. If the current
	 * value is equal to the given value, no store operation will be executed. If the
	 * value is not <code>null</code> and is different from the current value, the new
	 * value will be written to the settings. If the value is <code>null</code>, the
	 * key and the currently stored value are removed from the settings.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 */
	public boolean set(String key, Object value);

	/**
	 * Stores the setting under the given key using the given long value. The given
	 * long value is transformed to an <code>Long</code> object and stored to the
	 * settings store via <code>set(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 *
	 * @see <code>set(java.lang.String, java.lang.Object)</code>
	 */
	public boolean set(String key, long value);

	/**
	 * Stores the setting under the given key using the given integer value. The given
	 * integer value is transformed to an <code>Integer</code> object and stored to the
	 * settings via <code>set(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 *
	 * @see <code>set(java.lang.String, java.lang.Object)</code>
	 */
	public boolean set(String key, int value);

	/**
	 * Stores the setting under the given key using the given boolean value. The given
	 * boolean value is transformed to an <code>Boolean</code> object and stored to the
	 * settings via <code>set(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 *
	 * @see <code>set(java.lang.String, java.lang.Object)</code>
	 */
	public boolean set(String key, boolean value);

	/**
	 * Stores the setting under the given key using the given float value. The given
	 * float value is transformed to an <code>Float</code> object and stored to the
	 * settings via <code>set(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 *
	 * @see <code>set(java.lang.String, java.lang.Object)</code>
	 */
	public boolean set(String key, float value);

	/**
	 * Stores the setting under the given key using the given double value. The given
	 * double value is transformed to an <code>Double</code> object and stored to the
	 * settings via <code>set(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param value The value.
	 *
	 * @return <code>true</code> if the value had been applied to the settings, <code>false</code> otherwise.
	 *
	 * @see <code>set(java.lang.String, java.lang.Object)</code>
	 */
	public boolean set(String key, double value);

	/**
	 * Return all settings. The result map is read-only.
	 *
	 * @return A map containing all settings.
	 */
	public Map<String, Object> getAll();

	/**
	 * Queries the value stored under the given key. If the setting does not exist,
	 * <code>null</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value or <code>null</code>.
	 */
	public Object get(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.String</code>, the value casted to <code>java.lang.String</code>
	 * is returned. In all other cases, <code>null</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value casted <code>java.lang.String</code> or <code>null</code>.
	 */
	public String getString(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.String</code>, the value casted to <code>java.lang.String</code>
	 * is returned. In all other cases, the given default value is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @param defaultValue The default value or <code>null</code>.
	 *
	 * @return The stored value casted <code>java.lang.String</code> or the default value.
	 */
	public String getString(String key, String defaultValue);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.Long</code>, the value converted to an long value is returned. In
	 * all other cases, <code>-1</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value converted to a long value or <code>-1</code>.
	 */
	public long getLong(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.Integer</code>, the value converted to an integer value is returned.
	 * In all other cases, <code>-1</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value converted to an integer value or <code>-1</code>.
	 */
	public int getInt(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.Boolean</code>, the value converted to an boolean value is returned.
	 * In all other cases, <code>false</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value converted to an boolean value or <code>false</code>.
	 */
	public boolean getBoolean(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.Float</code>, the value converted to an float value is returned.
	 * In all other cases, <code>Float.NaN</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value converted to a float value or <code>Float.NaN</code>.
	 */
	public float getFloat(String key);

	/**
	 * Queries the value stored under the given key. If the setting exist and is of type
	 * <code>java.lang.Double</code>, the value converted to an double value is returned.
	 * In all other cases, <code>Double.NaN</code> is returned.
	 *
	 * @param key The key. Must not be <code>null</code>!
	 * @return The stored value converted to a double value or <code>Double.NaN</code>.
	 */
	public double getDouble(String key);

	/**
	 * Remove all settings.
	 */
	public void clear();

	/**
	 * Returns whether the settings are empty or not.
	 *
	 * @return <code>True</code> if the settings are empty, <code>false</code> if not.
	 */
	public boolean isEmpty();

	/**
	 * Returns whether the settings contains the given key.
	 *
	 * @param key The key. Must not be <code>null</code>.
	 * @return <code>True</code> if the settings contains the key, <code>false</code> if not.
	 */
	public boolean containsKey(String key);
}
