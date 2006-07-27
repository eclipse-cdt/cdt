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
 * ISchemaRegistry describes the interface that needs to be 
 * implemented for external tools to contribute their
 * schemas to the DataStore. 
 */
public interface ISchemaRegistry
{

	/**
	 * This method gets called when a new schema extender needs to be
	 * registered.
	 * 
	 * @param extender the new schema extender
	 */
	public void registerSchemaExtender(ISchemaExtender extender);

	/**
	 * This method is responsible for calling <code>extendSchema</code> on 
	 * each of the registered schema extenders.
	 * @param dataStore the DataStore for which the schema will be extended
	 */
	public void extendSchema(DataStore dataStore);
	
	/**
	 * Returns an <code>ExternalLoader</code> for the specified qualified class name
	 * @param qualifiedClassName the qualified class name of an external tool
	 * @return the external loader that can load to specified class
	 */
	public ExternalLoader getLoaderFor(String qualifiedClassName);
}