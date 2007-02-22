/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;
/**
 * All remote object types we support.
 * These can be used when registering property pages against remote universal file system objects.
 */
public interface ISystemFileRemoteTypes 
{

    // ------------------
    // TYPE CATEGORIES...
    // ------------------
    
	/**
	 * There is only one type category for remote files.
	 * It is "files".
	 */
    public static final String TYPECATEGORY = "files"; //$NON-NLS-1$
	/**
	 * There is only one type category for remote cmds.
	 * It is "cmds".
	 */
    public static final String TYPECMDCATEGORY = "cmds"; //$NON-NLS-1$

    // -----------
    // TYPES...
    // -----------
    
	/**
	 * A folder object
	 */
    public static final String TYPE_FOLDER = "folder"; //$NON-NLS-1$
    /**
     * A file object
     */
    public static final String TYPE_FILE = "file";     //$NON-NLS-1$

    // -----------
    // SUBTYPES...
    // -----------
    
	/**
	 * A folder object
	 */
    public static final String SUBTYPE_SUBFOLDER = "subfolder"; //$NON-NLS-1$
    /**
     * A root object
     */
    public static final String SUBTYPE_ROOT = "root";     //$NON-NLS-1$

}
