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

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * For listeners interested in changes with remote resources.
 * These events are designed to state what the change to the resource was, not to 
 * optimize those events for a GUI (eg, a delete event versus a refresh event)
 */
public class SystemRemoteChangeEvent implements ISystemRemoteChangeEvent
{
	private int eventType;
	private Object resource, parent;
	private String oldName;
	private ISubSystem subsystem;
	
	/**
	 * Constructor for non-rename event
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be 
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 */
	public SystemRemoteChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem) 
	{
		super();
		this.eventType = eventType;
		this.resource = resource;
		this.parent = resourceParent;
		this.subsystem = subsystem;
	}
	
	/**
	 * Constructor for a rename event.
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be 
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 * @param oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 */
	public SystemRemoteChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String oldName) 
	{
		this(eventType, resource, resourceParent, subsystem);
		this.oldName = oldName;
	}
	
	/**
	 * Constructor you shouldn't use unless you intend to call the setters
	 */
	public SystemRemoteChangeEvent()
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
	 * Reset the resource
	 */
	public void setResource(Object resource)
	{
		this.resource = resource;
	}
	
	/**
	 * Reset the resource's remote resource parent
	 */
	public void setResourceParent(Object resourceParent)
	{
		this.parent = resourceParent;
	}

	/**
	 * Reset the subsystem
	 */
	public void setSubSystem(ISubSystem subsystem)
	{
		this.subsystem = subsystem;
	}
	
	/**
	 * Reset the old name on a rename event
	 */
	public void setOldName(String oldName)
	{
		this.oldName = oldName;
	}
	
	/**
	 * Get the event type, such as {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents#SYSTEM_REMOTE_RESOURCE_CREATED}.
	 * @see org.eclipse.rse.core.events.ISystemRemoteChangeEvents
	 */
	public int getEventType()
	{
		return eventType;
	}
	
	/**
	 * Get the resource that this event applies to
	 * It must either be the binary object of the resource, or the absolute name of the resource.
	 */
	public Object getResource()
	{
		return resource;
	}
	
	/**
	 * Get the parent remote object for the affected remote object. This is not always known,
	 *  but when it is (ie, non null) then it can be used to refresh all expanded occurrences of that parent
	 */
	public Object getResourceParent()
	{
		return parent;
	}
	
	/**
	 * Get the subsystem in which this resource resides. 
	 * This allows the search for impacts to be limited to subsystems of the same parent factory, and to connections
	 *   with the same hostname as the subsystem's connection.
	 */
	public ISubSystem getSubSystem()
	{
		return subsystem;
	}
	
	/**
	 * Get the old name of the resource, in the event of a resource rename. Null for other event types.
	 */
	public String getOldName()
	{
		return oldName;
	}
	
}