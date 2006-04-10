/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;


public abstract class SystemResourceSelectionInputProvider extends SystemAbstractAPIProvider
{		
	private IHost _connection;
	private boolean _onlyConnection = false;
	private boolean _allowNew = true;
	private String[] _systemTypes;
	
	public SystemResourceSelectionInputProvider(IHost connection)
	{
		_connection = connection;
	}
	
	public SystemResourceSelectionInputProvider()
	{
		_connection = null;
	}
	
	public IHost getSystemConnection()
	{
		return _connection;
	}
	
	public boolean allowMultipleConnections()
	{
		return !_onlyConnection;
	}
	
	public void setAllowNewConnection(boolean flag)
	{
		_allowNew = flag;
	}
	
	public boolean allNewConnection()
	{
		return _allowNew;
	}
	
	public void setSystemConnection(IHost connection, boolean onlyConnection)
	{
		_connection = connection;
		_onlyConnection = onlyConnection;
	}
	
	public String[] getSystemTypes()
	{
		return _systemTypes;
	}
	
	public void setSystemTypes(String[] types)
	{
		_systemTypes = types;
	}
	
	public Object[] getSystemViewRoots()
	{
		if (_connection == null)
		{
			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
			_connection = registry.getHosts()[0];
			
		}
		return getConnectionChildren(_connection);
	}

	public boolean hasSystemViewRoots()
	{
		return false;
	}

	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		if (selectedConnection != null)
		{
			return getSubSystem(selectedConnection).getChildren();
		}
		return null;
	}

	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		if (selectedConnection != null)
		{
			return getSubSystem(selectedConnection).hasChildren();
		}
		return false;
	}
	
	protected abstract ISubSystem getSubSystem(IHost selectedConnection);
	
	
	
}