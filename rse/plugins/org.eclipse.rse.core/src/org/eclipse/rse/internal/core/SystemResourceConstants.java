/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * David Dykstal (IBM) - removed RESOURCE_TEAMPROFILE_NAME
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.internal.core;

/**
 * Constants related to project and folder names.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface SystemResourceConstants
{
	public static final String RESOURCE_PROJECT_NAME = "RemoteSystemsConnections"; //$NON-NLS-1$
	public static final String RESOURCE_TEMPFILES_PROJECT_NAME= "RemoteSystemsTempFiles"; //$NON-NLS-1$
	public static final String RESOURCE_CONNECTIONS_FOLDER_NAME = "Connections"; //$NON-NLS-1$
	public static final String RESOURCE_FILTERS_FOLDER_NAME = "Filters";     //$NON-NLS-1$
	public static final String RESOURCE_TYPE_FILTERS_FOLDER_NAME = "TypeFilters";         //$NON-NLS-1$
	public static final String RESOURCE_USERACTIONS_FOLDER_NAME = "UserActions"; //$NON-NLS-1$
	public static final String RESOURCE_COMPILECOMMANDS_FOLDER_NAME = "CompileCommands"; //$NON-NLS-1$


	// yantzi: artemis 6.0, offline messages
	public static final String MSG_OFFLINE_CANT_CONNECT			= "RSEC3001"; //$NON-NLS-1$
	// Connection doesn't exist
	public static final String MSG_CONNECTION_DELETED = "RSEF5011"; //$NON-NLS-1$

	public static final String MSG_LOADING_PROFILE_SHOULDBE_ACTIVATED = "RSEG1068"; //$NON-NLS-1$
	public static final String MSG_LOADING_PROFILE_SHOULDNOTBE_DEACTIVATED = "RSEG1069";	 //$NON-NLS-1$

}
