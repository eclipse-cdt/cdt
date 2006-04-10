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

package org.eclipse.rse.core;

import org.eclipse.rse.ui.ISystemPreferencesConstants;

/**
 * Constants related to project and folder names.
 */
public interface SystemResourceConstants 
{
	
    public static final String RESOURCE_PROJECT_NAME = "RemoteSystemsConnections";
    public static final String RESOURCE_TEMPFILES_PROJECT_NAME= "RemoteSystemsTempFiles";
    public static final String RESOURCE_CONNECTIONS_FOLDER_NAME = "Connections";
    public static final String RESOURCE_FILTERS_FOLDER_NAME = "Filters";    
    public static final String RESOURCE_TYPE_FILTERS_FOLDER_NAME = "TypeFilters";        
    public static final String RESOURCE_USERACTIONS_FOLDER_NAME = "UserActions";
    public static final String RESOURCE_COMPILECOMMANDS_FOLDER_NAME = "CompileCommands";
    
    public static final String RESOURCE_TEAMPROFILE_NAME = ISystemPreferencesConstants.DEFAULT_TEAMPROFILE;
}