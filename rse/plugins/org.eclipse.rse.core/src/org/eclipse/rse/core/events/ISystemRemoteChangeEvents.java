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
 * David McKnight (IBM) - [207100] Events for after a resource is downloaded and uploaded
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * The event IDs sent when remote resources in the model change These IDs are
 * used when creating ISystemRemoteChangeEvent objects.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemRemoteChangeEvents
{
	/**
	 * Event Type: a remote resource was added
	 *
	 * The event stores the following event parameters:
	 * <ul>
	 *  <li>resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter,
	 *    or a List of absolute names each of which would be given by calling getAbsoluteName on it's remote adapter
	 *  <li>resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurrences of that parent.
	 *  <li>subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 *  <li>oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 *  <li>originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 * </ul>
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_CREATED = 1;

	/**
	 * Event Type: a remote resource was removed
	 *
	 *  The event stores the following event parameters:
	 * <ul>
	 *  <li>resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter,
	 *    or a List of absolute names each of which would be given by calling getAbsoluteName on it's remote adapter
	 *  <li>resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurrences of that parent.
	 *  <li>subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 *  <li>oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 *  <li>originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 * </ul>
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_DELETED = 2;

	/**
	 * Event Type: a remote resource was changed
	 *
	 *  The event stores the following event parameters:
	 * <ul>
	 *  <li>resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter,
	 *  or a List of absolute names each of which would be given by calling getAbsoluteName on it's remote adapter
	 *  <li>resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurrences of that parent.
	 *  <li>subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 *  <li>oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 *  <li>originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 * </ul>
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_CHANGED = 4;

	/**
	 * Event Type: a remote resource was renamed
	 *
	 * The event stores the following event parameters:
	 * <ul>
	 *  <li>resource - the remote resource object, or absolute name of the resource as would be given by calling getAbsoluteName on its remote adapter,
	 *  or a List of absolute names each of which would be given by calling getAbsoluteName on it's remote adapter
	 *  <li>resourceParent - the remote resource's parent object, or absolute name, if that is known. If it is non-null, this will aid in refreshing occurrences of that parent.
	 *  <li>subsystem - the subsystem which contains this remote resource. This allows the search for impacts to be
	 *   limited to subsystems of the same parent factory, and to connections with the same hostname as the subsystem's connection.
	 *  <li>oldName - on a rename operation, this is the absolute name of the resource prior to the rename
	 *  <li>originatingViewer - optional. If set, this gives the viewer a clue that it should select the affected resource after refreshing its parent.
	 *    This saves sending a separate event to reveal and select the new created resource on a create event, for example.
	 * </ul>
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_RENAMED = 8;


	/**
	 * Event Type: a remote resource was uploaded
	 *
	 * The event stores the following event parameters:
	 * <ul>
	 * <li>resource - the remote resource object, or absolute name of the
	 * resource as would be given by calling getAbsoluteName on its remote
	 * adapter
	 * <li>resourceParent - the remote resource's parent object, or absolute
	 * name, if that is known. If it is non-null, this will aid in refreshing
	 * occurrences of that parent.
	 * <li>subsystem - the subsystem which contains this remote resource. This
	 * allows the search for impacts to be limited to subsystems of the same
	 * parent factory, and to connections with the same hostname as the
	 * subsystem's connection.
	 * </ul>
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_UPLOADED = 20;

	/**
	 * Event Type: a remote resource was downloaded
	 *
	 * The event stores the following event parameters:
	 * <ul>
	 * <li>resource - the remote resource object, or absolute name of the
	 * resource as would be given by calling getAbsoluteName on its remote
	 * adapter
	 * <li>resourceParent - the remote resource's parent object, or absolute
	 * name, if that is known. If it is non-null, this will aid in refreshing
	 * occurrences of that parent.
	 * <li>subsystem - the subsystem which contains this remote resource. This
	 * allows the search for impacts to be limited to subsystems of the same
	 * parent factory, and to connections with the same hostname as the
	 * subsystem's connection.
	 * </ul>
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_DOWNLOADED = 24;


	/**
	 * Indicates that the event is for a delete operation
	 *
	 *  @since org.eclipse.rse.core 3.0
	 */
	public static final String SYSTEM_REMOTE_OPERATION_DELETE = "DELETE"; //$NON-NLS-1$

	/**
	 * Indicates that the event is for a rename operation
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String SYSTEM_REMOTE_OPERATION_RENAME = "RENAME"; //$NON-NLS-1$

	/**
	 * Indicates that the event is for a create operation
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String SYSTEM_REMOTE_OPERATION_CREATE = "CREATE"; //$NON-NLS-1$

	/**
	 * Indicates that the event is for a move operation
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String SYSTEM_REMOTE_OPERATION_MOVE   = "MOVE"; //$NON-NLS-1$

	/**
	 * Indicates that the event is for a copy operation
	 *
	 * @since org.eclipse.rse.core 3.0
	 */
	public static final String SYSTEM_REMOTE_OPERATION_COPY   = "COPY"; //$NON-NLS-1$

}