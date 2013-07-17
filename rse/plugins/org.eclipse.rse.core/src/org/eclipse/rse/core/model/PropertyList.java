/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David McKnight (IBM)  -[413000] intermittent RSEDOMExporter NPE
 *******************************************************************************/
package org.eclipse.rse.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A property list is an ordered property set.
 * As items are added and removed their arrival order is maintained.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertyList extends PropertySet {
	
	private List _keys = new ArrayList(10);
	
	public PropertyList(String name) {
		super(name);
	}
	
	public IProperty addProperty(String key, IProperty property) {
		synchronized (_keys){
			_keys.remove(key);
			_keys.add(key);
		}
		return super.addProperty(key, property);
	}
	
	public IProperty addProperty(String key, String value) {
		synchronized (_keys){
			_keys.remove(key);
			_keys.add(key);
		}
		return super.addProperty(key, value);
	}
	
	public IProperty addProperty(String key, String value, IPropertyType type) {
		synchronized (_keys){
			_keys.remove(key);
			_keys.add(key);
		}
		return super.addProperty(key, value, type);
	}
	
	public String[] getPropertyKeys() {
		return (String[]) _keys.toArray(new String[_keys.size()]);
	}
	
	public boolean removeProperty(String key) {
		synchronized (_keys){
			_keys.remove(key);
		}
		return super.removeProperty(key);
	}
	
	public void setProperties(Map map) {
		synchronized (_keys){
			_keys.clear();
			_keys.addAll(map.keySet());
		}
		super.setProperties(map);
	}

}
