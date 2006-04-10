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

import org.eclipse.core.runtime.QualifiedName;

/**
 * This interface defines some common functionality required from all remote
 * resources, irrespective of whether the remote system is an OS/400, Windows, Linux
 * or Unix operating system. In particular, it allows users to create markers on remote
 * resources, and to create both session and persistent properties for them. A session
 * property is one that is stored only during the current workbench session, while a
 * persistent property is one that is kept between sessions. 
 * Clients must not implement this interface.
 * 
 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile
 */
public interface ISystemRemoteResource {



	/**
	 * Creates and returns the marker of the specified type on this resource.
	 * @param the marker type
	 * @return the created marker
	 */
	public ISystemRemoteMarker createMarker(String type);

	/**
	 * Deletes all markers on this resource of the given type, and optionally deletes
	 * such markers from its children. Deletion of markers with subtypes is also possible.
	 * @param the marker type, or <code>null</code> to indicate all types.
	 * @param whether or not to consider the subtypes of the given type
	 */
	public void deleteMarkers(String type, boolean includeSubtypes);

	/**
	 * Returns the marker with the specified id on this resource, or null if there
	 * is no such marker.
	 * @param the id of the marker to find
	 * @return a marker if found, or <code>null</code>
	 */
	public ISystemRemoteMarker findMarker(long id);

	/**
	 * Returns all markers of the specified type on this resource, and
	 * optionally, on its children. Markers with subtypes of the given type
	 * can also be found optionally. Returns an empty array if there are
	 * no matching markers.
	 * @param the marker type, or <code>null</code> to indicate all types.
	 * @param whether or not to consider the subtypes of the given type
	 * @return an array of markers, or an empty array if no markers are found
	 */
	public ISystemRemoteMarker[] findMarkers(String type, boolean includeSubtypes);

	/**
	 * Gets the marker with the given id. The marker is not guaranteed to exist.
	 * @param the marker id.
	 */
	public ISystemRemoteMarker getMarker(long id);

	/**
	 * Sets the value of the session property of this resource identified
	 * by the given key. If the supplied value is <code>null</code>,
	 * the session property is removed from this resource. 
	 * <p>
	 * Sessions properties are intended to be used as a caching mechanism
	 * by ISV plug-ins. They allow key-object associations to be stored with
	 * existing resources in the workspace. These key-value associations are
	 * maintained in memory (at all times), and the information is lost when a
	 * resource is deleted from the workspace, when the parent project
	 * is closed, or when the workspace is closed.
	 * </p>
	 * <p>
	 * The qualifier part of the property name must be the unique identifier
	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
	 * </p>
	 *
	 * @param key the qualified name of the property
	 * @param value the value of the session property, 
	 *     or <code>null</code> if the property is to be removed
	 * @see #getSessionProperty
	 */
	public void setSessionProperty(QualifiedName key, Object value);
	
	/**
 	 * Returns the value of the session property of this resource identified
 	 * by the given key, or <code>null</code> if this resource has no such property.
 	 *
 	 * @param key the qualified name of the property
 	 * @return the string value of the session property, 
 	 *     or <code>null</code> if this resource has no such property
 	 * @see #setSessionProperty
 	 */
	public Object getSessionProperty(QualifiedName key);
	
	/**
 	 * Sets the value of the persistent property of this resource identified
 	 * by the given key. If the supplied value is <code>null</code>,
 	 * the persistent property is removed from this resource. The change
 	 * is made immediately on disk.
 	 * <p>
 	 * Persistent properties are intended to be used by plug-ins to store
 	 * resource-specific information that should be persisted across platform sessions.
 	 * The value of a persistent property is a string which should be
 	 * short (i.e., under 2KB). Unlike session properties, persistent properties are
 	 * stored on disk and maintained across workspace shutdown and restart.
 	 * </p>
 	 * <p>
 	 * The qualifier part of the property name must be the unique identifier
 	 * of the declaring plug-in (e.g. <code>"com.example.plugin"</code>).
 	 * </p>
 	 *
 	 * @param key the qualified name of the property
 	 * @param value the string value of the property, 
 	 *     or <code>null</code> if the property is to be removed
 	 * @see #getPersistentProperty
 	 */
	public void setPersistentProperty(QualifiedName key, String value);

	/**
 	 * Returns the value of the persistent property of this resource identified
 	 * by the given key, or <code>null</code> if this resource has no such property.
 	 *
 	 * @param key the qualified name of the property
 	 * @return the string value of the property, 
 	 *     or <code>null</code> if this resource has no such property
 	 * @see #setPersistentProperty
 	 */
	public String getPersistentProperty(QualifiedName key);
	
	/**
	 * Returns whether this resource exists on the remote server.
	 * @return <code>true</code> if the resource exists, <code>false</code> otherwise
	 */
	public boolean exists();
	
	/**
	 * Returns whether a local copy of the file exists.
	 */
	public boolean isExistsLocally();
	
	/**
	 * Returns the last modified time on the server.
	 * @param the last modified time on the server
	 */
	public boolean getRemoteLastModifiedTime();
	
	/**
	 * Returns the last modified time on the client.
	 * @param the last modified time on the client.
	 */
	public boolean getLocalLastModifiedTime();
	
	/**
	 * Returns whether the local copy, if there is one, is in sync with the
	 * remote copy.
	 * @param true if the local copy is in sync, false otherwise, or if the
	 * local copy does not exist.
	 */
	public boolean isSynchronized();
}