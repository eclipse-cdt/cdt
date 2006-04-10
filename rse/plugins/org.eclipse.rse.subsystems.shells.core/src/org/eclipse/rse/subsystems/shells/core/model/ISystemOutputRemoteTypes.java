/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.subsystems.shells.core.model;
/**
 * All remote object types we support. 
 * These can be used when registering property pages against remote output objects.
 */
public interface ISystemOutputRemoteTypes 
{

    // ------------------
    // TYPE CATEGORIES...
    // ------------------
    
	/**
	 * There is only one type category for remote output.
	 * It is "output".
	 */
    public static final String TYPECATEGORY = "output";


    // -----------
    // TYPES...
    // -----------
    
    /**
	 * A folder object
	 */
    public static final String TYPE_DIRECTORY = "directory";
  
    /**
	 * A file object
	 */
    public static final String TYPE_FILE = "file";
  
    /**
	 * A command object
	 */
    public static final String TYPE_COMMAND = "command";
 
   /**
	 * A prompt object
	 */
    public static final String TYPE_PROMPT = "prompt";

 	/**
	 * An error object
	 */
    public static final String TYPE_ERROR = "error";

    /**
	 * A warning object
	 */
    public static final String TYPE_WARNING = "warning";

    /**
	 * An informational object
	 */
    public static final String TYPE_INFORMATIONAL = "informational";

    /**
	 * A grep object
	 */
    public static final String TYPE_GREP = "grep";
    
    
    /**
     *  An environment variable object
     */
    public static final String TYPE_ENVVAR = "envvar";

	/**
	 *  A libpath environment variable object
	 */
	public static final String TYPE_ENVVAR_LIBPATH = "libpathenvvar";

	/**
	  *  The path environment variable object
	  */
    public static final String TYPE_ENVVAR_PATH = "pathenvvar";
    
	/**
	  *  A process object
	  */
	public static final String TYPE_PROCESS = "process";
    
}