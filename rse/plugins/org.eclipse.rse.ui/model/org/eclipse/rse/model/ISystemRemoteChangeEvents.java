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
 * The event IDs sent when remote resources in the model change
 */
public interface ISystemRemoteChangeEvents 
{
	
	/**
	 * Event Type: a remote resource was added
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_CREATED = 1;	

	/**
	 * Event Type: a remote resource was removed
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_DELETED = 2;	

	/**
	 * Event Type: a remote resource was changed
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_CHANGED = 4;	

	/**
	 * Event Type: a remote resource was renamed
	 */
	public static final int SYSTEM_REMOTE_RESOURCE_RENAMED = 8;
	

}