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

package org.eclipse.rse.ui.model;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * For listeners interested in changes with remote resources.
 * These events are designed to state what the change to the resource was, not to 
 * optimize those events for a GUI (eg, a delete event versus a refresh event)
 */
public class SystemRemoteChangeEventUI extends SystemRemoteChangeEvent {

	private Viewer originatingViewer;

	/**
	 * Constructor for non-rename event
	 * @param eventType - one of the constants from {@link org.eclipse.rse.core.events.ISystemRemoteChangeEvents}
	 * @param resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter
	 * @param resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurences of that parent.
	 * @param subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be 
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 */
	public SystemRemoteChangeEventUI(int eventType, Object resource, Object resourceParent, ISubSystem subsystem) 
	{
		super(eventType, resource, resourceParent, subsystem);
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
	public SystemRemoteChangeEventUI(int eventType, Object resource, Object resourceParent, ISubSystem subsystem, String oldName) 
	{
		super(eventType, resource, resourceParent, subsystem, oldName);
	}
	
	/**
	 * Constructor you shouldn't use unless you intend to call the setters
	 */
	public SystemRemoteChangeEventUI()
	{
	}

	/**
	 * Set the originating viewer.
	 * Only this viewer is candidate for updating the selection. Eg, on a 
	 * create event, if this and the resource parent is set, the newly 
	 * created object is selected after the parent's contents are refreshed,
	 * for the originating viewer.
	 */
	public void setOriginatingViewer(Viewer originatingViewer)
	{
		this.originatingViewer = originatingViewer;
	}
	

	/**
	 * Get the originating viewer from which this remote resource change event
	 * comes from. The combination of this, if non-null, plus the resource parent,
	 * allows viewers to decide whether to update the selection within the
	 * parent resource, after refreshing that resource.
	 */
	public Viewer getOriginatingViewer()
	{
		return originatingViewer;
	}	

}
