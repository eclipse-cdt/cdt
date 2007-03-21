/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - removed RESOURCE_TEAMPROFILE_NAME
 ********************************************************************************/

package org.eclipse.rse.internal.core;


/**
 * Constants related to project and folder names.
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
}