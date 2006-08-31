/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

/**
 * This interface should be implemented by any remote artifact
 * that contains remote properties and (optionally) caches them.
 */
public interface IRemotePropertyHolder {
	
	/**
	 * Returns the value of the property with the given key.
	 * @param key the property key.
	 * @return the value of the property.
	 */
	public Object getProperty(String key);
	
	/**
	 * Returns the value of the properties with the given keys.
	 * @param keys the property keys.
	 * @return the correspoding values of the properties.
	 */
	public Object[] getProperties(String[] keys);
	
	/**
	 * Sets the value of the property with the given key.
	 * @param key the property key.
	 * @param value the value of the property.
	 */
	public void setProperty(String key, Object value);
	
	/**
	 * Sets the values of the properties with the given keys.
	 * @param keys the property keys.
	 * @param values the corresponding values of the properties.
	 */
	public void setProperties(String[] keys, Object[] values);
	
	/**
	 * Returns whether the property with the given key is stale.
	 * @param key the property key.
	 * @return <code>true</code> if the property is stale, <code>false</code> otherwise.
	 */
	public boolean isPropertyStale(String key);
	
	/**
	 * Marks the property with the given key as stale.
	 * @param key the property key.
	 */
	public void markPropertyStale(String key);
	
	/**
	 * Marks all properties as stale.
	 */
	public void markAllPropertiesStale();
}