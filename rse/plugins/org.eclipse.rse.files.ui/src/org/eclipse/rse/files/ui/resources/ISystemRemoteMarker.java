/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;

import java.util.Map;


/**
 * This interface defines a remote marker. It can be used to tag information
 * to a any remote resource.
 * Clients must not implement this interface.
 */
public interface ISystemRemoteMarker {



	/**
	 * Remote markers extension point id.
	 */
	public static final String EXTENSION_POINT_ID = "remoteMarkers";
	
	/**
	 * Deletes this marker from its associated resource. This method has no
	 * effect if this marker does not exist.
	 */
	public void delete();
	
	/**
	 * Tests this marker for equality with the given object.
	 * Two markers are equal if and only if their id and resource info are both equal.
	 * @param object the other object
	 * @return <code>true</code> if objects are equal, <code>false</code> otherwise
	 */
	public boolean equals(Object object);

	/**
	 * Returns whether this marker exists. A marker
	 * exists if its resource info exists and has a marker with the marker's id.
	 *
	 * @return <code>true</code> if this marker exists, otherwise
	 *    <code>false</code>
	 */
	public boolean exists();

	/**
	 * Returns the attribute with the given name.  The result is an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 * Returns <code>null</code> if the attribute is undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @return the value, or <code>null</code> if the attribute is undefined.
	 */
	public Object getAttribute(String attributeName);

	/**
	 * Returns the integer-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined.
	 * or the marker does not exist or is not an integer value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public int getAttribute(String attributeName, int defaultValue);

	/**
	 * Returns the string-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a string value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public String getAttribute(String attributeName, String defaultValue);

	/**
	 * Returns the boolean-valued attribute with the given name.  
	 * Returns the given default value if the attribute is undefined
	 * or the marker does not exist or is not a boolean value.
	 *
	 * @param attributeName the name of the attribute
	 * @param defaultValue the value to use if no value is found
	 * @return the value or the default value if no value was found.
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue);

	/**
	 * Returns a map with all the attributes for the marker.
	 * If the marker has no attributes then <code>null</code> is returned.
	 *
	 * @return a map of attribute keys and values (key type : <code>String</code> 
	 *		value type : <code>String</code>, <code>Integer</code>, or 
	 *		<code>Boolean</code>) or <code>null</code>.
	 */

	public Map getAttributes();
	
	/**
	 * Returns the attributes with the given names.  The result is an an array 
	 * whose elements correspond to the elements of the given attribute name
	 * array.  Each element is <code>null</code> or an instance of one
	 * of the following classes: <code>String</code>, <code>Integer</code>, 
	 * or <code>Boolean</code>.
	 *
	 * @param attributeNames the names of the attributes
	 * @return the values of the given attributes.
	 */
	public Object[] getAttributes(String[] attributeNames);

	/**
	 * Returns the time at which this marker was created.
	 *
	 * @return the difference, measured in milliseconds, between the time at which
	 *    this marker was created and midnight, January 1, 1970 UTC.
	 */
	public long getCreationTime();

	/**
	 * Returns the id of the marker.  The id of a marker is unique
	 * relative to the resource with which the marker is associated.
	 * Marker ids are not globally unique.
	 *
	 * @return the id of the marker
	 * @see ISystemRemoteResource#findMarker
	 */
	public long getId();

	/**
	 * Returns the resource with which this marker is associated. 
	 *
	 * @return the remote resource with which this marker is associated
	 */
	public ISystemRemoteResource getResource();

	/**
	 * Returns the type of this marker.
	 *
	 * @return the type of this marker
	 */
	public String getType();

	/**
	 * Returns whether the type of this marker is considered to be a subtype of
	 * the given marker type. 
	 *
	 * @return boolean <code>true</code>if the marker's type
	 *		is the same as (or a subtype of) the given type.
	 */
	public boolean isSubtypeOf(String superType);

	/**
	 * Sets the integer-valued attribute with the given name.  
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 */
	public void setAttribute(String attributeName, int value);

	/**
	 * Sets the attribute with the given name.  The value must be <code>null</code> or 
	 * an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * If the value is <code>null</code>, the attribute is considered to be undefined.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value, or <code>null</code> if the attribute is to be undefined
	 */
	public void setAttribute(String attributeName, Object value);

	/**
	 * Sets the boolean-valued attribute with the given name.
	 *
	 * @param attributeName the name of the attribute
	 * @param value the value
	 */
	public void setAttribute(String attributeName, boolean value);

	/**
	 * Sets the given attribute key-value pairs on this marker.
	 * The values must be <code>null</code> or an instance of 
	 * one of the following classes: <code>String</code>, 
	 * <code>Integer</code>, or <code>Boolean</code>.
	 * If a value is <code>null</code>, the new value of the 
	 * attribute is considered to be undefined.
	 *
	 * @param attributeNames an array of attribute names
	 * @param values an array of attribute values
	 */
	public void setAttributes(String[] attributeNames, Object[] values);

	/**
	 * Sets the attributes for this marker to be the ones contained in the
	 * given table. The values must be an instance of one of the following classes: 
	 * <code>String</code>, <code>Integer</code>, or <code>Boolean</code>.
	 * Attributes previously set on the marker but not included in the given map
	 * are considered to be removals. Setting the given map to be <code>null</code>
	 * is equivalent to removing all marker attributes.
	 *
	 * @param attributes a map of attribute names to attribute values 
	 *		(key type : <code>String</code> value type : <code>String</code>, 
	 *		<code>Integer</code>, or <code>Boolean</code>) or <code>null</code>
	 */
	public void setAttributes(Map attributes);
}