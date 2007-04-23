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

package org.eclipse.rse.internal.core.model;

import org.eclipse.rse.core.events.ISystemModelChangeEvent;

/**
 * For listeners interested in model changes with resources in the rse project.
 * These events are designed to state what the change the resource was, not to 
 *  optimize those events for a GUI (eg, a delete event versus a refresh event)
 */
public class SystemModelChangeEvent implements ISystemModelChangeEvent
{
	private int eventType, resourceType;
	private Object resource;
	private String oldName;
	
	/**
	 * Constructor for SystemModelChangeEvent.
	 */
	public SystemModelChangeEvent(int eventType, int resourceType, Object resource) 
	{
		super();
		this.eventType = eventType;
		this.resourceType = resourceType;
		this.resource = resource;
	}
	/**
	 * Constructor for SystemModelChangeEvent for a rename event
	 */
	public SystemModelChangeEvent(int eventType, int resourceType, Object resource, String oldName) 
	{
		this(eventType, resourceType, resource);
		this.oldName = oldName;
	}
	/**
	 * Constructor you shouldn't use unless you intend to call the setters
	 */
	public SystemModelChangeEvent()
	{
	}
	
	/**
	 * Reset the event type
	 */
	public void setEventType(int eventType)
	{
		this.eventType = eventType;
	}
	/**
	 * Reset the resource type
	 */
	public void setResourceType(int resourceType)
	{
		this.resourceType = resourceType;
	}
	/**
	 * Reset the resource
	 */
	public void setResource(Object resource)
	{
		this.resource = resource;
	}
	/**
	 * Reset the old name on a rename event
	 */
	public void setOldName(String oldName)
	{
		this.oldName = oldName;
	}
	
	/**
	 * Get the event type, such as {@link org.eclipse.rse.core.events.ISystemModelChangeEvents#SYSTEM_RESOURCE_ADDED}.
	 * @see org.eclipse.rse.core.events.ISystemModelChangeEvents
	 */
	public int getEventType()
	{
		return eventType;
	}
	/**
	 * Get the resource type, such as {@link org.eclipse.rse.core.events.ISystemModelChangeEvents#SYSTEM_RESOURCETYPE_CONNECTION}.
	 * @see org.eclipse.rse.core.events.ISystemModelChangeEvents
	 */
	public int getResourceType()
	{
		return resourceType;
	}
	/**
	 * Get the resource that this event applies to
	 */
	public Object getResource()
	{
		return resource;
	}
	
	/**
	 * Get the old name of the resource, in the event of a resource rename. Null for other event types.
	 */
	public String getOldName()
	{
		return oldName;
	}
}