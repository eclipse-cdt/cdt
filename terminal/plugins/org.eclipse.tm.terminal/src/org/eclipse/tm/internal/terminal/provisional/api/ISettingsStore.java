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
 * The settings store contains the state of a connection. The content of the
 * settings store is not the persisted state of the connection. Storing data
 * in the settings store does not make any assumption about possibly persisting
 * the connection state. Connection persistence has to be implemented by the UI
 * container embedding the Terminal control.  
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public interface ISettingsStore extends IAdaptable {
	
	/**
	 * Set the properties from the given map. Calling this method
	 * will overwrite all previous set properties.
	 * <p>
	 * <b>Note:</b> The method will have no effect if the given properties are the
	 * same as the already set properties.
	 *
	 * @param properties The map of properties to set. Must not be <code>null</code>.
	 */
	public void setProperties(Map<String, Object> properties);

	/**
	 * Adds all properties from the given map. If a property already exist
	 * in the properties container, than the value of the property is overwritten.
	 *
	 * @param properties The map of properties to add. Must not be <code>null</code>.
	 */
	public void addProperties(Map<String, ?> properties);

	/**
	 * Stores the property under the given property key using the given property value.
	 * If the current property value is equal to the given property value, no store
	 * operation will be executed. If the property value is not <code>null</code> and
	 * is different from the current property value, the new value will be written to
	 * the property store and a property change event is fired. If the property value
	 * is <code>null</code>, the property key and the currently stored value are removed
	 * from the property store.
	 *
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 */
	public boolean setProperty(String key, Object value);

	/**
	 * Stores the property under the given property key using the given long
	 * property value. The given long value is transformed to an <code>Long</code>
	 * object and stored to the properties store via <code>setProperty(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 *
	 * @see <code>setProperty(java.lang.String, java.lang.Object)</code>
	 */
	public boolean setProperty(String key, long value);

	/**
	 * Stores the property under the given property key using the given integer
	 * property value. The given integer value is transformed to an <code>Integer</code>
	 * object and stored to the properties store via <code>setProperty(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 *
	 * @see <code>setProperty(java.lang.String, java.lang.Object)</code>
	 */
	public boolean setProperty(String key, int value);

	/**
	 * Stores the property under the given property key using the given boolean
	 * property value. The given boolean value is transformed to an <code>Boolean</code>
	 * object and stored to the properties store via <code>setProperty(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 *
	 * @see <code>setProperty(java.lang.String, java.lang.Object)</code>
	 */
	public boolean setProperty(String key, boolean value);

	/**
	 * Stores the property under the given property key using the given float
	 * property value. The given float value is transformed to an <code>Float</code>
	 * object and stored to the properties store via <code>setProperty(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 *
	 * @see <code>setProperty(java.lang.String, java.lang.Object)</code>
	 */
	public boolean setProperty(String key, float value);

	/**
	 * Stores the property under the given property key using the given double
	 * property value. The given double value is transformed to an <code>Double</code>
	 * object and stored to the properties store via <code>setProperty(java.lang.String, java.lang.Object)</code>.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param value The property value.
	 * @return <code>true</code> if the property value had been applied to the property store, <code>false</code> otherwise.
	 *
	 * @see <code>setProperty(java.lang.String, java.lang.Object)</code>
	 */
	public boolean setProperty(String key, double value);

	/**
	 * Return all properties. The result map is read-only.
	 *
	 * @return A map containing all properties.
	 */
	public Map<String, Object> getProperties();

	/**
	 * Queries the property value stored under the given property key. If the property
	 * does not exist, <code>null</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value or <code>null</code>.
	 */
	public Object getProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.String</code>, the property value casted to
	 * <code>java.lang.String</code> is returned. In all other cases, <code>null</code>
	 * is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value casted <code>java.lang.String</code> or <code>null</code>.
	 */
	public String getStringProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.String</code>, the property value casted to
	 * <code>java.lang.String</code> is returned. In all other cases, the given default
	 * value is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @param defaultValue The default value or <code>null</code>.
	 * 
	 * @return The stored property value casted <code>java.lang.String</code> or <code>null</code>.
	 */
	public String getStringProperty(String key, String defaultValue);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.Long</code>, the property value converted
	 * to an long value is returned. In all other cases, <code>-1</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value converted to a long value or <code>-1</code>.
	 */
	public long getLongProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.Integer</code>, the property value converted
	 * to an integer value is returned. In all other cases, <code>-1</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value converted to an integer value or <code>-1</code>.
	 */
	public int getIntProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.Boolean</code>, the property value converted
	 * to an boolean value is returned. In all other cases, <code>false</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value converted to an boolean value or <code>false</code>.
	 */
	public boolean getBooleanProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.Float</code>, the property value converted
	 * to an float value is returned. In all other cases, <code>Float.NaN</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value converted to a float value or <code>Float.NaN</code>.
	 */
	public float getFloatProperty(String key);

	/**
	 * Queries the property value stored under the given property key. If the property
	 * exist and is of type <code>java.lang.Double</code>, the property value converted
	 * to an double value is returned. In all other cases, <code>Double.NaN</code> is returned.
	 *
	 * @param key The property key. Must not be <code>null</code>!
	 * @return The stored property value converted to a double value or <code>Double.NaN</code>.
	 */
	public double getDoubleProperty(String key);

	/**
	 * Remove all properties from the properties store. The method does not fire any
	 * properties changed event.
	 */
	public void clearProperties();

	/**
	 * Returns whether this properties container is empty or not.
	 *
	 * @return <code>True</code> if the properties container is empty, <code>false</code> if not.
	 */
	public boolean isEmpty();

	/**
	 * Returns whether this properties container contains the given key.
	 *
	 * @param key The key. Must not be <code>null</code>.
	 * @return <code>True</code> if the properties container contains the key, <code>false</code> if not.
	 */
	public boolean containsKey(String key);
}
