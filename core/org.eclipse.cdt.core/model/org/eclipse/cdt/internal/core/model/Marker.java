package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

public class Marker extends PlatformObject implements IMarker {
	private long id;
	private IResource resource;
	private HashMap attributes;
	private String type;

	public Marker(IResource res, String t) {
		resource = res;
		type = t;
		attributes = new HashMap();
		id = System.currentTimeMillis();
	}

	public void delete() throws CoreException {
	}

	public boolean equals(Object object) {
		return (this == object);
	}

	public boolean exists() {
		return true;
	}

	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	public int getAttribute(String attributeName, int defaultValue) {
		Integer loc = (Integer) getAttribute(attributeName);
		if (loc != null) {
			return loc.intValue();
		}
		return defaultValue;
	}

	public String getAttribute(String attributeName, String defaultValue) {
		String result = (String) getAttribute(attributeName);
		if (result != null) {
			return result;
		}
		return defaultValue;
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) {
		Boolean result = (Boolean) getAttribute(attributeName);
		if (result != null) {
			return true;
		}
		return defaultValue;
	}

	public Map getAttributes() throws CoreException {
		return attributes;
	}

	public Object[] getAttributes(String[] attributeNames) throws CoreException {
		ArrayList results = new ArrayList();
		for (int i = 0; i < attributeNames.length; i++) {
			Object attribute = getAttribute(attributeNames[i]);
			if (attribute != null) {
				results.add(attribute);
			}
		}
		return results.toArray();
	}

	public long getId() {
		return id;
	}

	public IResource getResource() {
		return resource;
	}

	public String getType() throws CoreException {
		return type;
	}

	public boolean isSubtypeOf(String superType) throws CoreException {
		return true;
	}

	public void setAttribute(String attributeName, Object value)
		throws CoreException {
		attributes.put(attributeName, value);
	}

	public void setAttribute(String attributeName, int value)
		throws CoreException {
		setAttribute(attributeName, new Integer(value));
	}

	public void setAttribute(String attributeName, boolean value)
		throws CoreException {
		setAttribute(attributeName, new Boolean(value));
	}

	public void setAttributes(String[] attributeNames, Object[] values)
		throws CoreException {
		for (int i = 0; i < attributeNames.length; i++) {
			attributes.put(attributeNames[i], values[i]);
		}
	}

	public void setAttributes(Map attributes) throws CoreException {
		attributes = (HashMap) attributes;
	}

	public void setId(long i) {
		id = i;
	}

}
