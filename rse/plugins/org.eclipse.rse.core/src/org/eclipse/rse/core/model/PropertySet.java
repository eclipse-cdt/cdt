/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [334837] Ordering of Library list entries incorrect after migration
 * David McKnight (IBM)  -[413000] intermittent RSEDOMExporter NPE
 *******************************************************************************/

package org.eclipse.rse.core.model;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * A LinkedHashmap based implementation of the {@link IPropertySet} interface.
 * 
 * Not thread-safe since the underlying {@link java.util.HashMap} is 
 * not thread-safe.
 * @noextend This class is not intended to be subclassed by clients.
 * The standard extensions are included in the framework.
 */
public class PropertySet extends RSEModelObject implements IPropertySet, IRSEModelObject, ILabeledObject, Observer {
	
	private String _name;
	private String _label = null;
	private String _description = null;
	private Map _properties;
	private IPropertySetContainer _container = null;

	protected static IPropertyType _defaultType = PropertyType.getStringPropertyType();

	/**
	 * Construct a new PropertySet based on an existing one (i.e. clone it).
	 * @param propertySet existing Property Set to clone
	 */
	public PropertySet(IPropertySet propertySet) {
		_name = propertySet.getName();
		_description = propertySet.getDescription();
		_properties = new LinkedHashMap();
		if (propertySet instanceof ILabeledObject) {
			ILabeledObject p = (ILabeledObject) propertySet;
			_label = p.getLabel();
		}

		String[] keys = propertySet.getPropertyKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			IProperty property = propertySet.getProperty(key);
			addProperty(key, new Property(property));
		}
		setDirty(true);
	}

	/**
	 * Construct a new empty PropertySet.
	 * @param name of the new PropertySet
	 */
	public PropertySet(String name) {
		_name = name;
		_properties = new LinkedHashMap();
		setDirty(true);
	}

	public String getName() {
		return _name;
	}
	
	public String getLabel() {
		if (_label != null) return _label;
		return _name;
	}
	
	public void setLabel(String label) {
		_label = label;
		setDirty(true);
	}

	public String getDescription() {
		return _description;
	}
	
	public void setDescription(String description) {
		_description = description;
		setDirty(true);
	}

	public String[] getPropertyKeys() {
		Set set = _properties.keySet();
		return (String[]) set.toArray(new String[set.size()]);
	}

	public void setName(String name) {
		_name = name;
		setDirty(true);
	}

	public void setProperties(Map map) {
		_properties = new LinkedHashMap(map.size());
		for (Iterator z = map.keySet().iterator(); z.hasNext();) {
			String key = (String) z.next();
			Object value = map.get(key);
			if (value instanceof IProperty) {
				addProperty(key, (IProperty)value);
			} else if (value instanceof String) {
				addProperty(key, (String)value);
			}
		}
	}

	/**
	 * Add a typed Property to the set.
	 * 
	 * In case a Property already exists for the given key, it will be overwritten.
	 * 
	 * @param key Key to add
	 * @param property The Property to add
	 * @return The added Property
	 */
	public IProperty addProperty(String key, IProperty property) {
		synchronized (_properties){
			_properties.put(key, property);
		}
		setDirty(true);
		return property;
	}

	public IProperty addProperty(String key, String value) {
		IProperty property = getProperty(key);
		if (property != null) {
			//FIXME should throw a NumberFormatException or similar,
			//if the value does not fit the type of the existing property.
			property.setValue(value);
		} else {
			property = addProperty(key, value, _defaultType);
		}
		return property;
	}

	public IProperty addProperty(String key, String value, IPropertyType type) {
		IProperty property = new Property(key, value, type, true);
		return addProperty(key, property);
	}

	public boolean removeProperty(String key) {
		Object value = null;
		synchronized (_properties){
			value = _properties.remove(key);
		}
		if (value == null) return false;
		setDirty(true);
		return true;
	}

	public IProperty getProperty(String key) {
		return (IProperty) _properties.get(key);
	}

	public String getPropertyValue(String key) {
		IProperty property = getProperty(key);
		if (property != null) {
			return property.getValue();
		}
		return null;
	}

	public IPropertyType getPropertyType(String key) {
		IProperty property = getProperty(key);
		if (property != null) {
			return property.getType();
		}
		return null;
	}
	
	public boolean commit() {
		return getPersistableParent().commit();
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}
	
	public IRSEPersistableContainer getPersistableParent() {
		IRSEPersistableContainer result = null;
		if (_container instanceof IRSEPersistableContainer) {
			result = (IRSEPersistableContainer) _container;
		}
		return result;
	}
	
	public void setContainer(IPropertySetContainer container) {
		_container = container;
	}
	
	public IPropertySetContainer getContainer() {
		return _container;
	}
	
	public void update(Observable o, Object arg) {
		setDirty(true);
	}

}
