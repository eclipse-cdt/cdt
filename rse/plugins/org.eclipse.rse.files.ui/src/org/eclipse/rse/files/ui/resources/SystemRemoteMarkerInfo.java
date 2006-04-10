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

import java.util.HashMap;
import java.util.Map;

/**
 * Objects of this type contain marker information.
 */
public class SystemRemoteMarkerInfo implements ISystemRemoteMarkerSetElement, Cloneable {



	// undefined static id
	protected static final long UNDEFINED_ID = -1;
	
	// marker identifier
	protected long id = UNDEFINED_ID;
	
	// marker type
	protected String type = null;;
	
	// marker attributes
	protected Map attributes = null;
	
	// marker creation type
	protected long creationTime = 0;
	
	/**
	 * Constructor for SystemRemoteMarkerInfo.
	 */
	public SystemRemoteMarkerInfo() {
		super();
	}
	
	/**
	 * Set the marker id.
	 * @param the id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarkerSetElement#getId()
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Set the type.
	 * @param the marker type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Get the type.
	 * @return the marker type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the attributes.
	 * @param the attributes
	 */
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Get the attributes.
	 * @return the attributes
	 */
	public Map getAttributes() {
		return getAttributes(true);
	}
	
	/**
	 * Get the attributes.
	 * @param specify whether to return a copy or the actual map
	 * @return the attribute map
	 */
	public Map getAttributes(boolean makeCopy) {
		
		if (attributes == null) {
			return null;
		}
		
		if (makeCopy) {
			HashMap newAttributes = new HashMap();
			newAttributes.putAll(attributes);
			return newAttributes;
		}
		else {
			return attributes;	
		}
	}
	
	/**
	 * Set the creation time.
	 * @param the creation time
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
	/**
	 * Get the creation time.
	 * @return the creation time
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		SystemRemoteMarkerInfo copy = (SystemRemoteMarkerInfo)(super.clone());
		copy.setAttributes(getAttributes());
		return copy;
	}
	
	/**
	 * Get attribute values given the attribute names.
	 * @param the array of attribute names
	 * @return the array of attribute values
	 */
	public Object[] getAttributes(String[] attributeNames) {
		Object[] result = new Object[attributeNames.length];
		
		for (int i = 0; i < attributeNames.length; i++)
			result[i] = getAttribute(attributeNames[i]);
		
		return result;
	}
	
	/**
	 * Get the attribute value given the attribute name.
	 * @param the attribute name
	 * @return the attribute value
	 */
	public Object getAttribute(String attributeName) {
		
		if (attributes == null) {
			return null;
		}
		else {
			return attributes.get(attributeName);
		}
	}
	
	/**
	 * Checks if attribute value is valid.
	 * @param the attribute value
	 * @return true if value is null, or a String, or an Integer, or a Boolean
	 */ 
	protected static boolean isValidAttributeValue(Object value) {
		return (value == null || value instanceof String || value instanceof Integer || value instanceof Boolean);
	}
	
	/**
	 * Set the value of an attribute.
	 * @param the attribute name
	 * @param the value of the attribute
	 */
	public void setAttribute(String attributeName, Object value) {
	
		if (!isValidAttributeValue(value)) {
			return;
		}
	
		if (attributes == null) {
			
			if (value == null) {
				return;
			}
			else {
				attributes = new HashMap();
				attributes.put(attributeName, value);
			}
		}
		else {
			
			if (value == null) {
				attributes.remove(attributeName);
				
				if (attributes.isEmpty()) {
					attributes = null;
				}
			}
			else {
				attributes.put(attributeName, value);
			}
		}
	}

	/**
	 * Set the values of an array of attributes
	 * @param the attribute names
	 * @param the attribute values
	 */
	public void setAttributes(String[] attributeNames, Object[] values) {
		
		if (!(attributeNames.length == values.length)) {
			return;
		}
		
		for (int i = 0; i < attributeNames.length; i++) {
			setAttribute(attributeNames[i], values[i]);
		}
	}
}