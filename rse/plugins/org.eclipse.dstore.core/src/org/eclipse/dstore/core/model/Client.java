/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Noriaki Takatsu and Masao Nishimoto
 *            
 * Contributors:
 *  Noriaki Takatsu    (IBM)   [220126] [dstore][api][breaking] Single process server for multiple clients
 *******************************************************************************/

package org.eclipse.dstore.core.model;

import org.eclipse.dstore.core.server.IServerLogger;

public class Client 
{
	public  String _userid;
	private IServerLogger _logger;
	
	
	public void setUserid(String userid)
    {
    	_userid = userid;
    }
	
	public String getUserid()
	{
		return _userid;
	}
	
	public void setLogger(IServerLogger logger)
	{
		_logger = logger;
	}
	
	public IServerLogger getLogger()
	{
		return _logger;
	}
	
	public String getProperty(String key)
	{
		return System.getProperty(key);
	}
	
}

