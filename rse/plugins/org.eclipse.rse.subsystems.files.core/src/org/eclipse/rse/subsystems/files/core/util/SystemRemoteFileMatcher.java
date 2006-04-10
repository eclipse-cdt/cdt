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

package org.eclipse.rse.subsystems.files.core.util;
import org.eclipse.rse.core.SystemRemoteObjectMatcher;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;



/**
 * This class encapsulates all the criteria required to identify a match on a remote
 * system file object. 
 * <p>
 * Use the static method {@link #getFileOrFolderMatcher()} to get an default instance that 
 * matches on any directory of any name. 
 * <p>
 * You only need to instantiate this class if you want to match on a file of a
 * particular name.
 * 
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 */
public class SystemRemoteFileMatcher extends SystemRemoteObjectMatcher
{
	public static SystemRemoteFileMatcher inst, instFiles, instFolders;
    public static final String category = ISystemFileRemoteTypes.TYPECATEGORY;
    public static final String fileType = ISystemFileRemoteTypes.TYPE_FILE;
    public static final String folderType = ISystemFileRemoteTypes.TYPE_FOLDER;
        
    /**
     * Constructor when you want to match on either folders or files
     * You only need to instantiate yourself if you want to match on files/folder of a 
     *  particular name
     * Otherwise, call {@link #SystemRemoteFileMatcher(String, boolean)}.
     */
    public SystemRemoteFileMatcher(String nameFilter)
    {
    	super(null, category, nameFilter, null, null, null);
    }
    /**
     * Constructor when you want to only list files or only list folders
     * You only need to instantiate yourself if you want to match on files/folder of a 
     *  particular name
     * Otherwise, call {@link #SystemRemoteFileMatcher(String)}.
     */
    public SystemRemoteFileMatcher(String nameFilter, boolean foldersOnly)
    {
    	super(null, category, nameFilter, foldersOnly ? folderType : fileType, null, null);
    }

    
    /**
     * Return an instance that will match on any file or folder of any name from any remote system
     */
    public static SystemRemoteFileMatcher getFileOrFolderMatcher()
    {
    	if (inst == null)
    	  inst = new SystemRemoteFileMatcher(null);
    	return inst;
    }
    /**
     * Return an instance that will match on any folder of any name from any remote system
     */
    public static SystemRemoteFileMatcher getFolderOnlyMatcher()
    {
    	if (instFolders == null)
    	  instFolders = new SystemRemoteFileMatcher(null, true);
    	return instFolders;
    }
    /**
     * Return an instance that will match on any file of any name from any remote system
     */
    public static SystemRemoteFileMatcher getFileOnlyMatcher()
    {
    	if (instFiles == null)
    	  instFiles = new SystemRemoteFileMatcher(null, false);
    	return instFiles;
    }


}