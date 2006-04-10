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

package org.eclipse.rse.model;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.subsystems.ISubSystem;


/**
 * A change event passed to you when listening for remote resource changes.
 * Note these are post-events, sent after the fact.
 */
public interface ISystemRemoteChangeEvent 
{
	/**
	 * Get the event type, such as {@link org.eclipse.rse.model.ISystemRemoteChangeEvents#SYSTEM_REMOTE_RESOURCE_CREATED}.
	 * @see org.eclipse.rse.model.ISystemRemoteChangeEvents
	 */
	public int getEventType();

	/**
	 * Get the resource that this event applies to. 
	 * It must either be the binary object of the resource, or the absolute name of the resource.
	 */
	public Object getResource();
	/**
	 * Get the parent remote object for the affected remote object. This is not always known,
	 *  but when it is (ie, non null) then it can be used to refresh all expanded occurrences of that parent
	 */
	public Object getResourceParent();

	/**
	 * Get the old name of the resource, in the event of a resource rename. Null for other event types.
	 */
	public String getOldName();

	/**
	 * Get the subsystem in which this resource resides. 
	 * This allows the search for impacts to be limited to subsystems of the same parent factory, and to connections
	 *   with the same hostname as the subsystem's connection.
	 */
	public ISubSystem getSubSystem();

	/**
	 * Get the originating viewer from which this remote resource change event comes from. The combination of this,
	 *  if non-null, plus the selected parent, allows viewers to decide whether to update the selection within the
	 *  parent resource, after refreshing that resource.
	 */
	public Viewer getOriginatingViewer();
}