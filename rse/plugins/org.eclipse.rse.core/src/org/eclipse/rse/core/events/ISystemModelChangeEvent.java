/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * A change event passed to you when listening for RSE model changes.
 * Note these are post-events, sent after the fact.
 */
public interface ISystemModelChangeEvent {
	/**
	 * Get the event type, such as {@link org.eclipse.rse.core.events.ISystemModelChangeEvents#SYSTEM_RESOURCE_ADDED}.
	 * @see org.eclipse.rse.core.events.ISystemModelChangeEvents
	 */
	public int getEventType();

	/**
	 * Get the resource type, such as {@link org.eclipse.rse.core.events.ISystemModelChangeEvents#SYSTEM_RESOURCETYPE_CONNECTION}.
	 * @see org.eclipse.rse.core.events.ISystemModelChangeEvents
	 */
	public int getResourceType();

	/**
	 * Get the resource that this event applies to
	 */
	public Object getResource();

	/**
	 * Get the old name of the resource, in the event of a resource rename. Null for other event types.
	 */
	public String getOldName();
}