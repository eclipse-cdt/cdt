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

import java.io.File;

import org.eclipse.core.runtime.QualifiedName;

/**
 * This class is the internal implementation of the ISystemRemoteResource interface.
 */
public class SystemRemoteResource implements ISystemRemoteResource {


	
	protected ISystemRemotePath path;

	/**
	 * Constructor for SystemRemoteResource.
	 */
	public SystemRemoteResource(ISystemRemotePath path) {
		this.path = path;
	}
	
	/**
	 * Get the resource info.
	 * @return the resource info for the resource
	 */
	public SystemRemoteResourceInfo getResourceInfo() {
		return SystemRemoteResourceManager.getInstance().getResourceInfo(path);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#createMarker(String)
	 */
	public ISystemRemoteMarker createMarker(String type){
		SystemRemoteResourceInfo resourceInfo = getResourceInfo();
		
		if (resourceInfo == null) {
			return null;
		}
		
		SystemRemoteMarkerInfo info = new SystemRemoteMarkerInfo();
		info.setType(type);
		info.setCreationTime(System.currentTimeMillis());
		SystemRemoteMarkerManager.getInstance().add(this, new SystemRemoteMarkerInfo[] { info });
		return new SystemRemoteMarker(this, info.getId());
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#deleteMarkers(String, boolean)
	 */
	public void deleteMarkers(String type, boolean includeSubtypes) {
		SystemRemoteMarkerManager.getInstance().removeMarkers(this, type, includeSubtypes); 
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#findMarker(long)
	 */
	public ISystemRemoteMarker findMarker(long id) {
		return SystemRemoteMarkerManager.getInstance().findMarker(this, id);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#findMarkers(String, boolean)
	 */
	public ISystemRemoteMarker[] findMarkers(String type, boolean includeSubtypes) {
		return SystemRemoteMarkerManager.getInstance().findMarkers(this, type, includeSubtypes);
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#getMarker(long)
	 */
	public ISystemRemoteMarker getMarker(long id) {
		return new SystemRemoteMarker(this, id);
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * 
	 */
	public ISystemRemotePath getLocation() {
		return null;
	}
	

	/**
	 * 
	 */
	public boolean existsLocally() {
		return false;
	}

	/**
	 * 
	 */
	public File getLocalCopy() {
		return null;
	}
	

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#getPersistentProperty(QualifiedName)
	 */
	public String getPersistentProperty(QualifiedName key) {
		return null;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#getSessionProperty(QualifiedName)
	 */
	public Object getSessionProperty(QualifiedName key) {
		return null;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#setPersistentProperty(QualifiedName, String)
	 */
	public void setPersistentProperty(QualifiedName key, String value) {
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#setSessionProperty(QualifiedName, Object)
	 */
	public void setSessionProperty(QualifiedName key, Object value) {
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#getLocalLastModifiedTime()
	 */
	public boolean getLocalLastModifiedTime() {
		return false;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#getRemoteLastModifiedTime()
	 */
	public boolean getRemoteLastModifiedTime() {
		return false;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#isExistsLocally()
	 */
	public boolean isExistsLocally() {
		return false;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteResource#isSynchronized()
	 */
	public boolean isSynchronized() {
		return false;
	}
}