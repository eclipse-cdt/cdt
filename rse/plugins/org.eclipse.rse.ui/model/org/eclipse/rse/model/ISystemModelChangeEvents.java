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
 * The event IDs sent when local resources in the RSE model change.
 * You should monitor for these events in your view if you display any of the resource types listed here.
 * <p>
 * To monitor, implement interface {@link org.eclipse.rse.model.ISystemModelChangeListener} and 
 * call {@link org.eclipse.rse.model.ISystemRegistry#addSystemModelChangeListener(ISystemModelChangeListener)}
 * and in your dispose method, call {@link org.eclipse.rse.model.ISystemRegistry#removeSystemModelChangeListener(ISystemModelChangeListener)}.
 * <p>
 * If you are interesting in firing model change events, see 
 * {@link org.eclipse.rse.model.ISystemRegistry#fireModelChangeEvent(int, int, Object, String)}.
 */
public interface ISystemModelChangeEvents 
{
	
	/**
	 * Event Type: a resource was added
	 */
	public static final int SYSTEM_RESOURCE_ADDED = 1;	

	/**
	 * Event Type: a resource was removed
	 */
	public static final int SYSTEM_RESOURCE_REMOVED = 2;	

	/**
	 * Event Type: a resource was changed
	 */
	public static final int SYSTEM_RESOURCE_CHANGED = 4;	

	/**
	 * Event Type: a resource was renamed
	 */
	public static final int SYSTEM_RESOURCE_RENAMED = 8;
	/**
	 * Event Type: a resource was reordered relative to its siblings
	 */
	public static final int SYSTEM_RESOURCE_REORDERED = 16;	
	
	/**
	 * Event Type: all resource were reloaded from the workspace: you need to refresh your viewer!
	 * This is fired after the user selects the Reload RSE action in the Team view, after recieving files from the repository.
	 */
	public static final int SYSTEM_RESOURCE_ALL_RELOADED = 128;
	

	/**
	 * Resource Type: profile
	 */
	public static final int SYSTEM_RESOURCETYPE_PROFILE = 1;	
	/**
	 * Resource Type: connection
	 */
	public static final int SYSTEM_RESOURCETYPE_CONNECTION = 2;	
	/**
	 * Resource Type: subsystem
	 */
	public static final int SYSTEM_RESOURCETYPE_SUBSYSTEM = 4;	
	/**
	 * Resource Type: filter pool
	 */
	public static final int SYSTEM_RESOURCETYPE_FILTERPOOL = 8;	
	/**
	 * Resource Type: filter pool reference. These are what subsystems contain... references to filter pools.
	 */
	public static final int SYSTEM_RESOURCETYPE_FILTERPOOLREF = 16;	
	/**
	 * Resource Type: filter 
	 */
	public static final int SYSTEM_RESOURCETYPE_FILTER = 32;	
	/**
	 * Resource Type: user action
	 */
	public static final int SYSTEM_RESOURCETYPE_USERACTION = 128;	
	/**
	 * Resource Type: named type, which are used in user actions
	 */
	public static final int SYSTEM_RESOURCETYPE_NAMEDTYPE = 256;	
	/**
	 * Resource Type: compile command
	 */
	public static final int SYSTEM_RESOURCETYPE_COMPILECMD = 512;	
	/**
	 * Resource Type: ALL. Used with SYSTEM_RESOURCE_ALL_RELOADED
	 */
	public static final int SYSTEM_RESOURCETYPE_ALL = 9999;	
}