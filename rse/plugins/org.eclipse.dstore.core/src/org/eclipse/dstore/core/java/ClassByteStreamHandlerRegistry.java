/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.dstore.core.java;

import java.util.HashMap;

import org.eclipse.dstore.core.model.DataStoreResources;


public class ClassByteStreamHandlerRegistry 
{
	private HashMap _map;
	private IClassByteStreamHandler _default;
	public ClassByteStreamHandlerRegistry()
	{
		_map = new HashMap();
	}
	
	/**
	 * Registers the default class byte stream handler
	 * @param handler the default byte stream handler
	 */
	public void setDefaultClassByteStreamHandler(IClassByteStreamHandler handler)
	{
		_default = handler; 
		_map.put(DataStoreResources.DEFAULT_CLASSBYTESTREAMHANDLER, handler);
		registerClassByteStreamHandler(handler);
	}
	
	/**
	 * Registers a class byte stream handler. 
	 * @param handler the handler to register
	 */
	public void registerClassByteStreamHandler(IClassByteStreamHandler handler)
	{
		_map.put(handler.getIdentifier(), handler);
	}
	
	/**
	 * Returns the class byte stream handler with the specified id.
	 * If "default" is specified or no such id has been registered, 
	 * the default byte stream handler is returned.
	 * @param id the id of the byte stream handler
	 * @return the byte stream handler
	 */
	public IClassByteStreamHandler getClassByteStreamHandler(String id)
	{
		IClassByteStreamHandler handler = (IClassByteStreamHandler)_map.get(id);
		if (handler == null)
		{
			handler = _default;
		}
		return handler;
	}
	
	/**
	 * Returns the default class byte stream handler
	 * @return the default
	 */
	public IClassByteStreamHandler getDefault()
	{
		return _default;
	}
}