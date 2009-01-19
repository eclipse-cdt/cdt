/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - [186589] Move User Actions events to the user actions plugin
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * The event IDs sent when local resources in the RSE model change. You should
 * monitor for these events in your view if you display any of the resource
 * types listed here.
 * <p>
 * To monitor, implement interface
 * {@link org.eclipse.rse.core.events.ISystemModelChangeListener} and call
 * {@link org.eclipse.rse.core.model.ISystemRegistry#addSystemModelChangeListener(ISystemModelChangeListener)}
 * and in your dispose method, call
 * {@link org.eclipse.rse.core.model.ISystemRegistry#removeSystemModelChangeListener(ISystemModelChangeListener)}.
 * <p>
 * If you are interesting in firing model change events, see
 * {@link org.eclipse.rse.core.model.ISystemRegistry#fireModelChangeEvent(int, int, Object, String)}
 * . These events will typically be signaled in an implementation of
 * ISystemRegistry.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemModelChangeEvents {

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
	//	/**
	//	 * Resource Type: user action
	//	 */
	//	public static final int SYSTEM_RESOURCETYPE_USERACTION = 128;
	//	/**
	//	 * Resource Type: named type, which are used in user actions
	//	 */
	//	public static final int SYSTEM_RESOURCETYPE_NAMEDTYPE = 256;
	//	/**
	//	 * Resource Type: compile command
	//	 */
	//	public static final int SYSTEM_RESOURCETYPE_COMPILECMD = 512;
	/**
	 * Resource Type: ALL. Used with SYSTEM_RESOURCE_ALL_RELOADED
	 */
	public static final int SYSTEM_RESOURCETYPE_ALL = 9999;
}