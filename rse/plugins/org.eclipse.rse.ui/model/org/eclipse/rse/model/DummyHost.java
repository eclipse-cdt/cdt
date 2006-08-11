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

package org.eclipse.rse.model;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class DummyHost implements IHost
{
	protected String _hostName;
	protected String _systemType;
	
	public DummyHost(String hostName, String systemType)
	{
		_hostName = hostName;
		_systemType = systemType;
	}
	public ISystemProfile getSystemProfile()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getSystemProfileName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setHostPool(ISystemHostPool pool)
	{
		// TODO Auto-generated method stub

	}

	public ISystemHostPool getHostPool()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ISubSystem[] getSubSystems()
	{
		// TODO Auto-generated method stub
		return null;
	}



	public String getLocalDefaultUserId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void clearLocalDefaultUserId()
	{
		// TODO Auto-generated method stub

	}

	public void deletingHost()
	{
		// TODO Auto-generated method stub

	}

	public void renamingSystemProfile(String oldName, String newName)
	{
		// TODO Auto-generated method stub

	}

	public boolean getForceUserIdToUpperCase()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean compareUserIds(String userId1, String userId2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String getSystemType()
	{
		return _systemType;
	}

	public void setSystemType(String value)
	{
		// TODO Auto-generated method stub

	}

	public String getAliasName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setAliasName(String value)
	{
		// TODO Auto-generated method stub

	}

	public String getHostName()
	{
		return _hostName;
	}

	public void setHostName(String value)
	{
		// TODO Auto-generated method stub

	}

	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setDescription(String value)
	{
		// TODO Auto-generated method stub

	}

	public String getDefaultUserId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultUserId(String value)
	{
		// TODO Auto-generated method stub

	}

	public boolean isPromptable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setPromptable(boolean value)
	{
		// TODO Auto-generated method stub

	}

	public boolean isOffline()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setOffline(boolean value)
	{
		// TODO Auto-generated method stub

	}

	public IConnectorService[] getConnectorServices()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertySet[] getPropertySets()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertySet getPropertySet(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertySet createPropertySet(String name, String description)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public IPropertySet createPropertySet(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}


	public boolean addPropertySet(IPropertySet set)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addPropertySets(IPropertySet[] sets)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removePropertySet(String name)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDirty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setDirty(boolean flag)
	{
		// TODO Auto-generated method stub

	}

	public boolean commit()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean wasRestored()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setWasRestored(boolean flag)
	{
		// TODO Auto-generated method stub

	}

}