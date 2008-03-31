/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 ********************************************************************************/

package org.eclipse.rse.core.events;

import org.eclipse.rse.core.subsystems.ISubSystem;


/**
 * A change event passed to you when listening for remote resource changes.
 * Note these are post-events, sent after the fact.
 */
public interface ISystemRemoteChangeEvent
{
	/**
	 * Get the event type, such as {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents#SYSTEM_REMOTE_RESOURCE_CREATED}.
	 * @see org.eclipse.rse.core.events.ISystemRemoteChangeEvents
	 * @return the event type.
	 */
	public int getEventType();

	/**
	 * Get the resource that this event applies to.
	 * It must either be the binary object of the resource, or the absolute name of the resource,
	 * or a List of absolute names of the resources
	 * @return the resource that this event applies to.
	 */
	public Object getResource();

	/**
	 * Get the parent remote object for the affected remote object. This is not always known,
	 * but when it is (ie, non null) then it can be used to refresh all expanded occurrences of that parent
	 * @return the parent remote object of the affected resource,
	 *     or <code>null</code> if not applicable.
	 */
	public Object getResourceParent();

	/**
	 * Get the old name(s) of the resource(s), in the event of a resource
	 * rename, move or copy. Null for other event types.
	 * 
	 * @return the old names of the resources in case of a rename event, or
	 *         <code>null</code> if not applicable.
	 * @since org.eclipse.rse.core 3.0
	 */
	public String[] getOldNames();

	/**
	 * Get the subsystem in which this resource resides.
	 * This allows the search for impacts to be limited to subsystems
	 * of the same parent factory, and to connections with the same
	 * hostname as the subsystem's connection.
	 * @return the subsystem in which this resource resides.
	 */
	public ISubSystem getSubSystem();

	/**
	 * Returns the operation of this event if it's not implied by the event
	 * itself.
	 * 
	 * @see ISystemRemoteChangeEvents#SYSTEM_REMOTE_OPERATION_COPY
	 * @see ISystemRemoteChangeEvents#SYSTEM_REMOTE_OPERATION_MOVE
	 * 
	 * @return the operation that triggered this event
	 * @since org.eclipse.rse.core 3.0
	 */
	public String getOperation();
}