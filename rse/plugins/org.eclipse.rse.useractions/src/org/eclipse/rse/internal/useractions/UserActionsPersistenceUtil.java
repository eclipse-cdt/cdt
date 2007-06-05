/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

/**
 * This class is a placeholder documenting the old persistence API and structure for
 * user actions, user type filters, and compile commands.
 * The class is not functional.
 * TODO: (dwd) this needs to be replaced with the new persistence provider scheme.
 */
public class UserActionsPersistenceUtil {
	
	
	/*
	 * Bogus infrastructure to make this compile
	 */
	
	private static class ResourceHelpers {
		IFolder getOrCreateFolder(IFolder parent, String folderName) {
			return null;
		}
		IFolder getFolder(IContainer parent, String folderName) {
			return null;
		}
	}
	
	private static ResourceHelpers getResourceHelpers() {
		return new ResourceHelpers();
	}
	
	private static IFolder getProfileFolder(String profileName) {
		return null;
	}
	
	private static String getFolderName(ISubSystemConfiguration subystemConfiguration) {
		return null;
	}
	
	private static IProject getRemoteSystemsProject() {
		return null;
	}
	
	private static final String RESOURCE_USERACTIONS_FOLDER_NAME = null;
	private static final String RESOURCE_COMPILECOMMANDS_FOLDER_NAME = null;
	private static final String RESOURCE_TYPE_FILTERS_FOLDER_NAME = null;

    /*
     * --------------------------------------------------------------------------------------------------------------------------------
     * USER ACTIONS SUBTREE FOLDER METHODS...
     * ======================================
     *  .--- Team (folder)           - getProfileFolder(SystemProfile/"team")
     *  |  |
     *  |  |
     *  |  .--- UserActions (folder)    - getUserActionsFolder(SystemProfile/"team")
     *  |     |
     *  |     .--- SubSystemConfigurationID1 (folder) - getUserActionsFolder(SystemProfile/"team", SubSystemConfiguration)
     *  |     |  .--- actions.xml (file)
     *  |     .--- SubSystemConfigurationID2 (folder)
     *  |        .--- actions.xml (file)
     * --------------------------------------------------------------------------------------------------------------------------------
     */
    // ---------------------------------------------------
    // GET USER DEFINED ACTIONS ROOT FOLDER PER PROFILE...
    // ---------------------------------------------------
    /**
     * Get user defined actions root folder given a system profile name
     */
    protected static IFolder getUserActionsFolder(String profileName)
    {
        return getResourceHelpers().getOrCreateFolder(getProfileFolder(profileName),RESOURCE_USERACTIONS_FOLDER_NAME);      	
    }
    /**
     * Get user defined actions root folder given a system profile object and subsystem factory
     */
    public static IFolder getUserActionsFolder(ISystemProfile profile, ISubSystemConfiguration ssFactory)
    {
        return getUserActionsFolder(profile.getName(),ssFactory);
    }
    /**
     * Get user defined actions root folder given a system profile name and subsystem factory
     */
    public static IFolder getUserActionsFolder(String profileName, ISubSystemConfiguration ssFactory)
    {
        IFolder parentFolder = getUserActionsFolder(profileName);
        String folderName = getFolderName(ssFactory);
        return getResourceHelpers().getOrCreateFolder(parentFolder, folderName); // Do create it.
    }
	/**
	 * Test for existence of user defined actions root folder given a system profile name and subsystem factory
	 */
	public static boolean testUserActionsFolder(String profileName, ISubSystemConfiguration ssFactory)
	{
		IFolder parentFolder = getUserActionsFolder(profileName);
		String folderName = getFolderName(ssFactory);
		return (getResourceHelpers().getFolder(parentFolder, folderName).exists()); // Do NOT create it.
	}
	
    /**
     * Get user defined actions root folder given a system profile name and subsystem factory Id.
     * This is a special-needs method provided for the Import action processing,
     * when a subsystem instance is not available.
     */
    public static IFolder getUserActionsFolder( String profileName, String factoryId)
    {
        IFolder parentFolder = getUserActionsFolder(profileName);
        return getResourceHelpers().getOrCreateFolder(parentFolder, factoryId); // Do create it.
    }

    /*
     * --------------------------------------------------------------------------------------------------------------------------------
     * COMPILE COMMAND SUBTREE FOLDER METHODS...
     * ======================================
     *  .--- Team (folder)           - getProfileFolder(SystemProfile/"team")
     *  |  |
     *  |  |
     *  |  .--- CompileCommands (folder)    - getCompileCommandsFolder(SystemProfile/"team")
     *  |     |
     *  |     .--- SubSystemConfigurationID1 (folder) - getCompileCommandsFolder(SystemProfile/"team", SubSystemConfiguration)
     *  |     |  .--- compileCommands.xml (file)
     *  |     .--- SubSystemConfigurationID2 (folder)
     *  |        .--- compileCommands.xml (file)
     * --------------------------------------------------------------------------------------------------------------------------------
     */
    // ---------------------------------------------------
    // GET COMPILE COMMANDS ROOT FOLDER PER PROFILE...
    // ---------------------------------------------------
    /**
     * Get compile commands root folder given a system profile name
     */
    protected static IFolder getCompileCommandsFolder(String profileName)
    {
        return getResourceHelpers().getOrCreateFolder(getProfileFolder(profileName),RESOURCE_COMPILECOMMANDS_FOLDER_NAME);      	
    }
    /**
     * Get compile commands root folder given a system profile object and subsystem factory
     */
    public static IFolder getCompileCommandsFolder(ISystemProfile profile, ISubSystemConfiguration ssFactory)
    {
        return getCompileCommandsFolder(profile.getName(),ssFactory);
    }
    /**
     * Get compile commands root folder given a system profile name and subsystem factory
     */
    public static IFolder getCompileCommandsFolder(String profileName, ISubSystemConfiguration ssFactory)
    {
        IFolder parentFolder = getCompileCommandsFolder(profileName);
        String folderName = getFolderName(ssFactory);
        return getResourceHelpers().getOrCreateFolder(parentFolder, folderName); // Do create it.
    }
   
    /**
     * Get compile commands root folder given a system profile name and subsystem factory Id.
     * This is a special-needs method provided for the Import action processing,
     * when a subsystem instance is not available.
     */
    public static IFolder getCompileCommandsFolder( String profileName, String factoryId)
    {
        IFolder parentFolder = getCompileCommandsFolder(profileName);
        return getResourceHelpers().getOrCreateFolder(parentFolder, factoryId); // Do create it.
    }

    /*
     * --------------------------------------------------------------------------------------------------------------------------------
     * TYPE FILTERS SUBTREE FOLDER METHODS...
     * ======================================
     *  .--- TypeFilters (folder)    - getTypeFiltersFolder()
     *     .--- SubSystemConfigurationID1 (folder) - getTypeFiltersFolder(SubSystemConfiguration)
     *     |  .--- typefilters.xmi (file)
     * --------------------------------------------------------------------------------------------------------------------------------
     */

    /**
     * Get the typeFilters root folder
     */
    public static IFolder getTypeFiltersFolder()
    {
        return getResourceHelpers().getFolder(getRemoteSystemsProject(),RESOURCE_TYPE_FILTERS_FOLDER_NAME);      	
    }
    /**
     * Get the typeFilters sub-folder per subsystem factory object
     */
    public static IFolder getTypeFiltersFolder(ISubSystemConfiguration ssFactory)
    {
        IFolder parentFolder = getTypeFiltersFolder();
        String folderName = getFolderName(ssFactory);
        return getResourceHelpers().getOrCreateFolder(parentFolder, folderName); // DO create it.            	
    }
    /**
     * Get the typeFilters sub-folder per subsystem factory id
     */
    public static IFolder getTypeFiltersFolder(String ssFactoryId)
    {
        IFolder parentFolder = getTypeFiltersFolder();
        return getResourceHelpers().getOrCreateFolder(parentFolder, ssFactoryId); // DO create it.            	
    }
}
