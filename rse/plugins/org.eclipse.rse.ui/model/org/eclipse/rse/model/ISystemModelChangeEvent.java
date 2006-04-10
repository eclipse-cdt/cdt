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

/**
 * A change event passed to you when listening for RSE model changes.
 * Note these are post-events, sent after the fact.
 */
public interface ISystemModelChangeEvent 
{
	/**
	 * Get the event type, such as {@link org.eclipse.rse.model.ISystemModelChangeEvents#SYSTEM_RESOURCE_ADDED}.
	 * @see org.eclipse.rse.model.ISystemModelChangeEvents
	 */
	public int getEventType();
	/**
	 * Get the resource type, such as {@link org.eclipse.rse.model.ISystemModelChangeEvents#SYSTEM_RESOURCETYPE_CONNECTION}.
	 * @see org.eclipse.rse.model.ISystemModelChangeEvents
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