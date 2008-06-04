/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *******************************************************************************/

package org.eclipse.rse.core.events;

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * For listeners interested in changes with remote resources. These events are
 * designed to state what the change to the resource was, not to optimize those
 * events for a GUI (eg, a delete event versus a refresh event).
 *
 * In RSE 3.0, the concept of Operation type was added (See
 * {@link #setOperation(String)} and the new Constructors, and the oldNames
 * property was extended from a single String into a String array.
 */
public class SystemRemoteChangeEvent implements ISystemRemoteChangeEvent
{
	private int eventType;
	private Object resource, parent;
	private String[] oldNames;
	private ISubSystem subsystem;
	private Object originatingViewer;
	private String operation;

	/**
	 * Constructor for non-rename event
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter,
	 * or List of absoluteNames
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
	 *
	 * @param eventType - one of the constants from
	 *            {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the
	 *            resource as would be given by calling getAbsoluteName on its
	 *            remote adapter, or List of absoluteNames
	 * @param resourceParent - the remote resource's parent object, or absolute
	 *            name, if that is known. If it is non-null, this will aid in
	 *            refreshing occurrences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource.
	 *            This allows the search for impacts to be limited to subsystems
	 *            of the same parent factory, and to connections with the same
	 *            hostname as the subsystem's connection.
	 * @param oldNames - on a rename, copy or move operation, these are the
	 *            absolute names of the resources prior to the operation
	 * @since 3.0 replaced String oldName by String[] oldNames
	 */
	public SystemRemoteChangeEvent(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames)
	{
		this(eventType, resource, resourceParent, subsystem);
		this.oldNames = oldNames;
	}

	/**
	 * Constructor for non-rename event
	 * 
	 * @param operation - the operation for which this event was fired. From
	 *            {@link ISystemRemoteChangeEvents#SYSTEM_REMOTE_OPERATION_COPY}
	 *            and related String constants
	 * @param eventType - one of the constants from
	 *            {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the
	 *            resource as would be given by calling getAbsoluteName on its
	 *            remote adapter, or List of absoluteNames
	 * @param resourceParent - the remote resource's parent object, or absolute
	 *            name, if that is known. If it is non-null, this will aid in
	 *            refreshing occurrences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource.
	 *            This allows the search for impacts to be limited to subsystems
	 *            of the same parent factory, and to connections with the same
	 *            hostname as the subsystem's connection.
	 * @since 3.0
	 */
	public SystemRemoteChangeEvent(String operation, int eventType, Object resource, Object resourceParent, ISubSystem subsystem)
	{
		super();
		this.eventType = eventType;
		this.resource = resource;
		this.parent = resourceParent;
		this.subsystem = subsystem;
		this.operation = operation;
	}

	/**
	 * Constructor for a rename event.
	 *
	 * @param operation - the operation for which this event was fired. From
	 *            {@link ISystemRemoteChangeEvents#SYSTEM_REMOTE_OPERATION_COPY}
	 *            and related String constants
	 * @param eventType - one of the constants from
	 *            {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the
	 *            resource as would be given by calling getAbsoluteName on its
	 *            remote adapter, or List of absoluteNames
	 * @param resourceParent - the remote resource's parent object, or absolute
	 *            name, if that is known. If it is non-null, this will aid in
	 *            refreshing occurrences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource.
	 *            This allows the search for impacts to be limited to subsystems
	 *            of the same parent factory, and to connections with the same
	 *            hostname as the subsystem's connection.
	 * @param oldNames - on a rename, copy or move operation, these are the
	 *            absolute names of the resources prior to the operation
	 * @since 3.0
	 */
	public SystemRemoteChangeEvent(String operation, int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String[] oldNames)
	{
		this(operation, eventType, resource, resourceParent, subsystem);
		this.oldNames = oldNames;
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
	 * Reset the old names on a rename, move or copy event
	 *
	 * @since 3.0
	 */
	public void setOldNames(String[] oldNames)
	{
		this.oldNames = oldNames;
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
	 * It must either be the binary object of the resource, or the absolute name of the resource,
	 * or List of absoluteNames.
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
	 * Get the old name of the resource, in the event of a resource rename. Null
	 * for other event types.
	 *
	 * @since 3.0
	 */
	public String[] getOldNames()
	{
		return oldNames;
	}

	/**
	 * Set the originating viewer.
	 * Only this viewer is candidate for updating the selection. Eg, on a
	 * create event, if this and the resource parent is set, the newly
	 * created object is selected after the parent's contents are refreshed,
	 * for the originating viewer.
	 */
	public void setOriginatingViewer(Object originatingViewer) {
		this.originatingViewer = originatingViewer;
	}

	/**
	 * Get the originating viewer from which this remote resource change event
	 * comes from. The combination of this, if non-null, plus the resource parent,
	 * allows viewers to decide whether to update the selection within the
	 * parent resource, after refreshing that resource.
	 */
	public Object getOriginatingViewer() {
		return originatingViewer;
	}


	/**
	 * @since 3.0
	 * @param operation from
	 *            {@link ISystemRemoteChangeEvents#SYSTEM_REMOTE_OPERATION_COPY}
	 *            and related String constants
	 */
	public void setOperation(String operation){
		this.operation = operation;
	}

	/**
	 * Returns the operation of this event if it's not implied by the event
	 * itself. The operation can be optionally specified when the event is
	 * constructed. By default this will return null.
	 *
	 * @return the operation that triggered this event
	 * @since 3.0
	 */
	public String getOperation() {
		return operation;
	}


}
