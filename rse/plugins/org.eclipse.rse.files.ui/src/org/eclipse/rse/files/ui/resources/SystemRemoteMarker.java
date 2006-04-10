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
 * This class is an internal implementation of the ISystemRemoteMarker interface.
 * It is not intended to be implemented by clients.
 */
public class SystemRemoteMarker implements ISystemRemoteMarker {


	
	/**
	 * Resource with which this marker is associated.
	 */
	protected ISystemRemoteResource resource;
	
	/**
	 * The marker id.
	 */
	protected long id;

	/**
	 * Constructor for SystemRemoteMarker.
	 */
	public SystemRemoteMarker(ISystemRemoteResource resource, long id) {
		this.resource = resource;
		this.id = id;		
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#delete()
	 */
	public void delete() {
		SystemRemoteMarkerManager.getInstance().removeMarker(getResource(), getId());
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#equals(Object)
	 */
	public boolean equals(Object object) {
		
		if (!(object instanceof ISystemRemoteMarker)) {
			return false;
		}
		else {
			ISystemRemoteMarker other = (ISystemRemoteMarker)object;
			return (id == other.getId()) && (resource.equals(other.getResource()));
		}
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#exists()
	 */
	public boolean exists() {
		return getInfo() != null;
	}
	
	/**
	 * Get the marker info.
	 * @return the marker info.
	 */
	private SystemRemoteMarkerInfo getInfo() {
		return SystemRemoteMarkerManager.getInstance().findMarkerInfo(getResource(), getId());
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttribute(String)
	 */
	public Object getAttribute(String attributeName) {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return null;
		}
		
		return info.getAttribute(attributeName);	
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttribute(String, int)
	 */
	public int getAttribute(String attributeName, int defaultValue) {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return defaultValue;
		}
		
		Object value = info.getAttribute(attributeName);
		
		if ((value != null) && (value instanceof Integer)) {
			return ((Integer)value).intValue();
		}
		
		return defaultValue;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttribute(String, String)
	 */
	public String getAttribute(String attributeName, String defaultValue) {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return defaultValue;
		}
		
		Object value = info.getAttribute(attributeName);
		
		if ((value != null) && (value instanceof String)) {
			return (String)value;
		}
		
		return defaultValue;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttribute(String, boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return defaultValue;
		}
		
		Object value = info.getAttribute(attributeName);
		
		if ((value != null) && (value instanceof Integer)) {
			return ((Boolean)value).booleanValue();
		}
		
		return defaultValue;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttributes()
	 */
	public Map getAttributes() {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return null;
		}
		
		return info.getAttributes();
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getAttributes(String[])
	 */
	public Object[] getAttributes(String[] attributeNames) {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return new Object[0];
		}
		
		return info.getAttributes(attributeNames);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getCreationTime()
	 */
	public long getCreationTime() {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return -1;
		}
		
		return info.getCreationTime();
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getId()
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getResource()
	 */
	public ISystemRemoteResource getResource() {
		return resource;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#getType()
	 */
	public String getType() {
		SystemRemoteMarkerInfo info = getInfo();
		
		if (info == null) {
			return null;
		}
		
		return info.getType();
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#isSubtypeOf(String)
	 */
	public boolean isSubtypeOf(String superType) {
		String type = getType();
		
		if (type == null) {
			return false;
		}
		
		return SystemRemoteMarkerManager.getInstance().getCache().isSubtype(type, superType);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#setAttribute(String, int)
	 */
	public void setAttribute(String attributeName, int value) {
		setAttribute(attributeName, new Integer(value));
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#setAttribute(String, Object)
	 */
	public void setAttribute(String attributeName, Object value) {
		SystemRemoteMarkerManager manager = SystemRemoteMarkerManager.getInstance();
		SystemRemoteMarkerInfo info = getInfo();
		info.setAttribute(attributeName, value);
		
		if (manager.isPersistent(info)) {
			((SystemRemoteResource)resource).getResourceInfo().set(ISystemRemoteCoreConstants.M_MARKERS_DIRTY);	// need to change this
		}
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#setAttribute(String, boolean)
	 */
	public void setAttribute(String attributeName, boolean value) {
		setAttribute(attributeName, value ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#setAttributes(String[], Object[])
	 */
	public void setAttributes(String[] attributeNames, Object[] values) {
		SystemRemoteMarkerManager manager = SystemRemoteMarkerManager.getInstance();
		SystemRemoteMarkerInfo info = getInfo();
		info.setAttributes(attributeNames, values);
		
		if (manager.isPersistent(info)) {
			((SystemRemoteResource)resource).getResourceInfo().set(ISystemRemoteCoreConstants.M_MARKERS_DIRTY);	// need to change this
		}
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteMarker#setAttributes(Map)
	 */
	public void setAttributes(Map attributes) {
		SystemRemoteMarkerManager manager = SystemRemoteMarkerManager.getInstance();
		SystemRemoteMarkerInfo info = getInfo();
		info.setAttributes(attributes);
		
		if (manager.isPersistent(info)) {
			((SystemRemoteResource)resource).getResourceInfo().set(ISystemRemoteCoreConstants.M_MARKERS_DIRTY);	// need to change this
		}
	}
}