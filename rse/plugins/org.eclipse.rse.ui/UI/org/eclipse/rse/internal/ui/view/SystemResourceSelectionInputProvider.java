/********************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others. All rights reserved.
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
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [252912] SystemRemoteFileDialog shows Local contents even when specifying a SystemType
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.view.ISystemResourceSelectionInputProvider;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;


public abstract class SystemResourceSelectionInputProvider extends SystemAbstractAPIProvider
	implements ISystemResourceSelectionInputProvider
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
		IHost[] hosts = getValidHosts();
		if (hosts != null && hosts.length>0) {
			_connection = hosts[0];
		}
	}
	
	private boolean validHost(IHost host){
		if (_systemTypes != null){
			IRSESystemType hostType = host.getSystemType();
			for (int t = 0; t < _systemTypes.length; t++){
				IRSESystemType type = _systemTypes[t];
				if (hostType == type){
					return true;
				}
			}
			return false;
		}
		else {
			return true;
		}
	}
	
	private IHost[] getValidHosts() {
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost[] hosts = registry.getHosts();
		
		// make sure the hosts are valid for the specified system types
		if (_systemTypes != null){
			List hostList = new ArrayList();
			for (int i = 0; i < hosts.length; i++){
				IHost host = hosts[i];
				if (validHost(host)){
					hostList.add(host);
				}
			}
			return (IHost[])hostList.toArray(new IHost[hostList.size()]);
		}
		else {
			return hosts;
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
		if (_connection != null){ // reset the connection if isn't valid
			if (!validHost(_connection)){
				_connection = null;
			}
		}
	}
	
	public Object[] getSystemViewRoots()
	{
		if (_connection == null)
		{
			IHost[] hosts = getValidHosts();
			if (hosts!=null && hosts.length!=0) {
				_connection = hosts[0];
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