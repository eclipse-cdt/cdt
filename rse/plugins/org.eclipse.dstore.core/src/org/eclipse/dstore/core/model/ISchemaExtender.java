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

package org.eclipse.dstore.core.model;


import org.eclipse.dstore.core.util.ExternalLoader;

/**
 * ISchemaExtender describes the interfaces that tool extensions
 * need to implement to add or extend other schemas in the DataStore. 
 */
public interface ISchemaExtender
{


    /**
     * Add this tool's schema to the global DataStore schema.
     * This interface must be implemented by each miner in order to
     * populate the DataStore schema with information about this tool's
     * object model and information about how to communicate with the
     * tool from objects available to the user interface.
     *
     * @param schemaRoot the descriptor root
     */
    public abstract void extendSchema(DataElement schemaRoot);

	/**
	 * Implement this to returns the external class loader for this extender
	 * implementation.  In order  for a tool extension to be loaded by the DataStore, it's
	 * class loader needs to be supplied.
	 * 
	 * @return the external loader
	 */
    public abstract ExternalLoader getExternalLoader();     
}