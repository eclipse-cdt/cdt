/********************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [202866] Fix exceptions in RSE browse dialog when SystemRegistry is not yet fully initialized
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;


public abstract class SystemResourceSelectionInputProvider extends SystemAbstractAPIProvider
{		
	private IHost _connection = null;
	private boolean _onlyConnection = false;
	private boolean _allowNew = true;
	private IRSESystemType[] _systemTypes;
	private String _category = null;
	
	public SystemResourceSelectionInputProvider(IHost connection)
	{
		_connection = connection;
	}
	
	public SystemResourceSelectionInputProvider()
	{
		// choose random host
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost[] hosts = registry.getHosts();
		if (hosts != null && hosts.length>0) {
			_connection = hosts[0];
		}
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
	
	public boolean allowNewConnection()
	{
		return _allowNew;
	}
	
	public void setSystemConnection(IHost connection, boolean onlyConnection)
	{
		_connection = connection;
		_onlyConnection = onlyConnection;
	}
	
	public IRSESystemType[] getSystemTypes()
	{
		return _systemTypes;
	}
	
	public void setSystemTypes(IRSESystemType[] types)
	{
		_systemTypes = types;
	}
	
	public Object[] getSystemViewRoots()
	{
		if (_connection == null)
		{
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			IHost[] hosts = registry.getHosts();
			if (hosts!=null && hosts.length!=0) {
				_connection = registry.getHosts()[0];
			}
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
			ISubSystem ss = getSubSystem(selectedConnection);
			if (ss!=null) {
				return ss.getChildren();
			}
		}
		return new Object[0];
	}

	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		if (selectedConnection != null)
		{
			ISubSystem ss = getSubSystem(selectedConnection);
			if (ss!=null) {
				return ss.hasChildren();
			}
		}
		return false;
	}
	
	protected abstract ISubSystem getSubSystem(IHost selectedConnection);
	
	
	public void setCategory(String category)
	{
		_category = category;
	}
	
	public String getCategory()
	{
		return _category;
	}
	
	
}