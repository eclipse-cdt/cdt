package org.eclipse.rse.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A property list is an ordered property set. As items are added and removed their arrival order is
 * maintained.
 */
public class PropertyList extends PropertySet {
	
	private List _keys = new ArrayList(10);
	
	public PropertyList(String name) {
		super(name);
	}
	
	public IProperty addProperty(String key, IProperty property) {
		_keys.remove(key);
		_keys.add(key);
		return super.addProperty(key, property);
	}
	
	public IProperty addProperty(String key, String value) {
		_keys.remove(key);
		_keys.add(key);
		return super.addProperty(key, value);
	}
	
	public IProperty addProperty(String key, String value, IPropertyType type) {
		_keys.remove(key);
		_keys.add(key);
		return super.addProperty(key, value, type);
	}
	
	public String[] getPropertyKeys() {
		return (String[]) _keys.toArray(new String[_keys.size()]);
	}
	
	public boolean removeProperty(String key) {
		_keys.remove(key);
		return super.removeProperty(key);
	}
	
	public void setProperties(Map map) {
		_keys.clear();
		_keys.addAll(map.keySet());
		super.setProperties(map);
	}

}
