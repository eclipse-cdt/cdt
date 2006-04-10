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

package org.eclipse.dstore.core.model;

import java.util.HashMap;

/**
 * Stores the set of registered byte stream handlers using the handler id
 * as the key.  Whenever a bytestream operation is required (i.e. for a save of bytes)
 * The appropriate byte stream handler is retrieved via the specified id.
 * If no such handler exists, then the default byte stream handler is returned.
 * 
 */
public class ByteStreamHandlerRegistry
{
	private HashMap _map;
	private IByteStreamHandler _default;
	public ByteStreamHandlerRegistry()
	{
		_map = new HashMap();
	}
	
	/**
	 * Registers the default byte stream handler
	 * @param handler the default byte stream handler
	 */
	public void setDefaultByteStreamHandler(IByteStreamHandler handler)
	{
		_default = handler;
		_map.put(DataStoreResources.DEFAULT_BYTESTREAMHANDLER, handler);
		registerByteStreamHandler(handler);
	}
	
	/**
	 * Registers a byte stream handler. 
	 * @param handler the handler to register
	 */
	public void registerByteStreamHandler(IByteStreamHandler handler)
	{
		_map.put(handler.getId(), handler);
	}
	
	/**
	 * Returns the byte stream handler with the specified id.
	 * If "default" is specified or no such id has been registered, 
	 * the default byte stream handler is returned.
	 * @param id the id of the byte stream handler
	 * @return the byte stream handler
	 */
	public IByteStreamHandler getByteStreamHandler(String id)
	{
		IByteStreamHandler handler = (IByteStreamHandler)_map.get(id);
		if (handler == null)
		{
			handler = _default;
		}
		return handler;
	}
	
	/**
	 * Returns the default byte stream handler
	 * @return the default
	 */
	public IByteStreamHandler getDefault()
	{
		return _default;
	}
}