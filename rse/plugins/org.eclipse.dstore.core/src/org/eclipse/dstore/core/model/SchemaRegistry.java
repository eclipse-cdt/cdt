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

import java.util.ArrayList;

import org.eclipse.dstore.core.util.ExternalLoader;

/**
 * SchemaRegistry implements the interface for external tools to contribute their
 * schemas to the DataStore. 
 */
public class SchemaRegistry implements ISchemaRegistry
{


	private ArrayList _initializedDataStores = new ArrayList();
	private ArrayList _extenders = new ArrayList();

	/**
	 * Registers a schema extender with the associated DataStores
	 * @param extender the schema extender to register
	 */
	public void registerSchemaExtender(ISchemaExtender extender)
	{
		if (!_extenders.contains(extender))
		{
			_extenders.add(extender);
			for (int i = 0; i < _initializedDataStores.size(); i++)
			{
				DataStore dataStore = (DataStore) _initializedDataStores.get(i);
				DataElement schemaRoot = dataStore.getDescriptorRoot();
				extender.extendSchema(schemaRoot);
			}
		}
	}

	/**
	 * Calls extendSchema() on each of the registered schema extenders to
	 * extend the schema of the specified DataStore
	 * 
	 * @param dataStore the DataStore whos schema will be updated
	 */
	public void extendSchema(DataStore dataStore)
	{
		if (!_initializedDataStores.contains(dataStore))
		{
			DataElement schemaRoot = dataStore.getDescriptorRoot();
			for (int i = 0; i < _extenders.size(); i++)
			{
				ISchemaExtender extender = (ISchemaExtender) _extenders.get(i);
				extender.extendSchema(schemaRoot);
			}
			_initializedDataStores.add(dataStore);
		}
	}

	/**
	 * Gets the <code>ExternalLoader</code> for the specified qualified classname
	 * 
	 * @param source the qualified classname
	 * @return the external loader for the specified classname
	 */
	public ExternalLoader getLoaderFor(String source)
	{
		for (int i = 0; i < _extenders.size(); i++)
		{
			ISchemaExtender extender = (ISchemaExtender) _extenders.get(i);
			ExternalLoader loader = extender.getExternalLoader();
			if (loader.canLoad(source))
			{
				return loader;
			}
		}
		return null;
	}
}