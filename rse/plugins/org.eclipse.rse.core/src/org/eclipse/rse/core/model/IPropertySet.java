/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - Added Javadoc.
 * David McKnight   (IBM)        - [217715] [api] RSE property sets should support nested property sets
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.core.model;

import java.util.Map;

/**
 * A Property Set stores key/value pairs, where the keys are Strings and the
 * values are an {@link IProperty}, of a type declared by an
 * {@link IPropertyType}.
 *
 * The Property Set is identified by a name. By default, the type of each
 * property is of type String, and in fact each value can be retrieved in String
 * representation.
 *
 * The key <code>"description"</code> is reserved for internal use, to store the
 * description of the Property set.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. Use
 *           {@link PropertySet} directly.
 */
public interface IPropertySet extends IPropertySetContainer {
	/**
	 * The key used to store the description of the Property Set.
	 * This is no longer used and should not be referenced except for
	 * compatibility reasons.
	 */
	public static final String DESCRIPTION_KEY = "description"; //$NON-NLS-1$

	/**
	 * Return the name of this Property Set.
	 * @return String name of the Property Set.
	 */
	public String getName();

	/**
	 * Return the description of this Property Set.
	 *
	 * Note that in order to set the description, you need to call
	 * <code>addProperty(IPropertySet.DESCRIPTION_KEY, "Description");</code>
	 *
	 * @return Description of the Property Set,
	 * 		or <code>null</code> in case no description has been set.
	 */
	public String getDescription();

	/**
	 * Sets the description property of the property set.
	 * Fully equivalent to
	 * <code>addProperty(IPropertySet.DESCRIPTION_KEY, description);</code>
	 * @param description the string describing this property set.
	 */
	public void setDescription(String description);

	/**
	 * Return the {@link IProperty} associated with the given key.
	 *
	 * If the key is not in the set, <code>null</code> is returned.
	 *
	 * @param key String key for Property
	 * @return requested Property,
	 * 		or <code>null</code> if the key is not found in the set.
	 */
	public IProperty getProperty(String key);

	/**
	 * Return the String representation of a Property.
	 *
	 * Provided that the key is found in the set, this is a shortcut
	 * for getProperty(key).getValue(). If the key is not in the set,
	 * <code>null</code> is returned.
	 *
	 * @param key String key for Property
	 * @return String value of requested Property,
	 * 		or <code>null</code> if the key is not found in the set.
	 */
	public String getPropertyValue(String key);

	/**
	 * Return the list of Property Keys in this Set.
	 *
	 * Provided that the Set has a description, the
	 * @link{DESCRIPTION_KEY} key will also be in the list.
	 * The interface defines no particular ordering for the
	 * keys.
	 *
	 * @return String array of Property keys.
	 */
	public String[] getPropertyKeys();

	/**
	 * Return the type of the property identified by the given key.
	 *
	 * @param key String key for Property
	 * @return Type of requested Property,
	 * 		or <code>null</code> if the key is not found in the set.
	 */
	public IPropertyType getPropertyType(String key);

	/**
	 * Set the name of this Property Set.
	 * @param name the name to set. Must not be <code>null</code>
	 */
	public void setName(String name);

	/**
	 * Set all the Properties of this set.
	 *
	 * @param map a Map of String to {@link IProperty} associations.
	 */
	public void setProperties(Map map);

	/**
	 * Add a Property with String value to the set.
	 *
	 * In case a Property already exist for the given key, it will be overwritten
	 * by the new value, note that this will <b>not</b> change the Property's type,
	 * so if the key identifies an integer Property but you set it to String value
	 * "foo" the resulting Property Set will be inconsistent.
	 *
	 * If the Property does not yet exist in the set, a new key will be added and
	 * the new Property will be of type "String".
	 *
	 * @param key Key to add
	 * @param value Value to add
	 * @return The added Property
	 */
	public IProperty addProperty(String key, String value);

	/**
	 * Add a typed Property to the set.
	 *
	 * In case a Property already exists for the given key, it will be
	 * removed and replaced by the new one.
	 *
	 * @param key Key to add
	 * @param value Value to add
	 * @param type Type of the Property to add
	 * @return The added Property
	 */
	public IProperty addProperty(String key, String value, IPropertyType type);

	/**
	 * Remove a Property from the set.
	 *
	 * @param key The key to remove
	 * @return <code>true</code> if the Property has been removed,
	 *      or <code>false</code> if the key has not been part of the set.
	 */
	public boolean removeProperty(String key);

	/**
	 * Sets the container of this property set. Used to notify the container of
	 * a change in a property.
	 *
	 * @param container the property set container
	 */
	public void setContainer(IPropertySetContainer container);

	/**
	 * @return the container of this property set or null if there is no container.
	 */
	public IPropertySetContainer getContainer();
}
