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

package org.eclipse.rse.subsystems.files.core.subsystems;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.validators.ValidatorFolderName;


//
/**
 * Specialization for file subsystem factories.
 */
/**
 * @lastgen interface RemoteFileSubSystemConfiguration extends SubSystemConfiguration {}
 */

public interface IRemoteFileSubSystemConfiguration extends ISubSystemConfiguration
{
	/**
	 * Return true if subsystems of this factory support the environment variables property.
	 * Return true to show it, return false to hide it. 
	 */
	public boolean supportsEnvironmentVariablesPropertyPage();
	
	/**
	 * Return true if subsystems for this configuration support search functionality.
	 * @return true if search is supported
	 */
	public boolean supportsSearch();

	/**
	 * Return true if subsystems for this configuration support archive management.
	 * @return true if archive management is supported
	 */
	public boolean supportsArchiveManagement();
	
	/**
	 * Tell us if this is a unix-style file system or a windows-style file system. The
	 * default is windows.
	 * Child classes must call this, so we know how to respond to separator and path-separator requests.
	 */
	public boolean isUnixStyle();
	/**
	 * Tell us if this file system is case sensitive. The default is isUnixStyle(), and so should
	 *  rarely need to be overridden.
	 */
	public boolean isCaseSensitive();
	    
    // --------------------------------
    // VALIDATOR METHODS...
    // --------------------------------   	
    /**
     * Return validator used in filter string dialog for the path part of the filter string
     */
    public ISystemValidator getPathValidator();
    /**
     * Return validator used in filter string dialog for the file part of the filter string
     */
    public ISystemValidator getFileFilterStringValidator();
    /**
     * Return validator used when creating or renaming files
     */
    public ValidatorFileName getFileNameValidator();
    /**
     * Return validator used when creating or renaming folders
     */
    public ValidatorFolderName getFolderNameValidator();
    // --------------------------------
    // FILE SYSTEM ATTRIBUTE METHODS...
    // --------------------------------   	
	/**
	 * Return in string format the character used to separate folders. Eg, "\" or "/"
	 */
    public String getSeparator();
	/**
	 * Return in character format the character used to separate folders. Eg, "\" or "/"
	 */    
    public char getSeparatorChar();
	/**
	 * Return in string format the character used to separate paths. Eg, ";" or ":"
	 */    
    public String getPathSeparator();
	/**
	 * Return in char format the character used to separate paths. Eg, ";" or ":"
	 */    
    public char getPathSeparatorChar();
	/**
	 * Return as a string the line separator.
	 */
	public String getLineSeparator();	
	/**
	 * Return the default remote systems editor profile ID for files on this subsystem
	 */
	public String getEditorProfileID();
	
}