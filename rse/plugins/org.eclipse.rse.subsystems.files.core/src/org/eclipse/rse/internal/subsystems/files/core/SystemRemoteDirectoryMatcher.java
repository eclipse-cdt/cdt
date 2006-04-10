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

package org.eclipse.rse.internal.subsystems.files.core;
import org.eclipse.rse.core.SystemRemoteObjectMatcher;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;



/**
 * This class encapsulates all the criteria required to identify a match on a remote
 * system directory object. 
 * <p>
 * Use the static method {@link #getDirectoryMatcher()}
 * to get an default instance that matches on any directory of any name. 
 * <p>
 * You only need to instantiate this class if you want to match on a directory of a
 * particular name.
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 */
public class SystemRemoteDirectoryMatcher extends SystemRemoteObjectMatcher
{
	public static SystemRemoteDirectoryMatcher inst = null;
	//public static final String factoryId = com.ibm.etools.systems.as400filesubsys.FileSubSystemFactory.factoryId;
    public static final String category = ISystemFileRemoteTypes.TYPECATEGORY;
    public static final String type = ISystemFileRemoteTypes.TYPE_FOLDER;
        
    /**
     * Constructor.
     * You only need to instantiate yourself if you want to match on a directory
     * of a particular name.
     * Otherwise, call {@link #getDirectoryMatcher()}.
     */
    public SystemRemoteDirectoryMatcher(String nameFilter)
    {
    	super(null, category, nameFilter, type, null, null);
    }
    
    /**
     * Return an instance that will match on any directory of any name from any remote system
     */
    public static SystemRemoteDirectoryMatcher getDirectoryMatcher()
    {
    	if (inst == null)
    	  inst = new SystemRemoteDirectoryMatcher(null);
    	return inst;
    }
}