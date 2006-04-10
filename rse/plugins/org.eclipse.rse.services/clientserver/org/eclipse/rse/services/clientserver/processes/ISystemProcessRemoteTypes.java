/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver.processes;


/**
 * All remote object types we support.
 * These can be used when registering property pages against remote universal process system objects.
 */
public interface ISystemProcessRemoteTypes
{

    // ------------------
    // TYPE CATEGORIES...
    // ------------------
    
	/**
	 * There is only one type category for remote processes.
	 * It is "processes".
	 */
    public static final String TYPECATEGORY = "processes";

    // -----------
    // TYPES...
    // -----------
    
	/**
	 * A process object
	 */
    public static final String TYPE_PROCESS = "process";
    
    /**
     * A root process object
     */
    public static final String TYPE_ROOT = "rootprocess";    

}