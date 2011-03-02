/********************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David McKnight   (IBM)        - [338510] "Copy Connection" operation deletes the registered property set in the original connection
 ********************************************************************************/

package org.eclipse.rse.core.model;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * A DummyHost is used as a placeholder during the construction of hosts by wizards.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DummyHost extends PlatformObject implements IHost
{
	protected String _hostName;
	protected IRSESystemType _systemType;
	
	/**
	 * Constructor.
	 * @param hostName name of the dummy host
	 * @param systemType sytem type. May be <code>null</code>.
	 */
	public DummyHost(String hostName, IRSESystemType systemType)
	{
		_hostName = hostName;
		_systemType = systemType;
	}
	public ISystemProfile getSystemProfile()
	{
		return null;
	}

	public String getSystemProfileName()
	{
		return null;
	}

	public void setHostPool(ISystemHostPool pool)
	{
		//Auto-generated method stub
	}

	public ISystemHostPool getHostPool()
	{
		return null;
	}

	public ISubSystem[] getSubSystems()
	{
		return null;
	}



	public String getLocalDefaultUserId()
	{
		return null;
	}

	public void clearLocalDefaultUserId()
	{
		//Auto-generated method stub
	}

	public void deletingHost()
	{
		//Auto-generated method stub
	}

	public void renamingSystemProfile(String oldName, String newName)
	{
		//Auto-generated method stub
	}

	public boolean getForceUserIdToUpperCase()
	{
		return false;
	}

	public boolean compareUserIds(String userId1, String userId2)
	{
		return false;
	}

	public IRSESystemType getSystemType()
	{
		return _systemType;
	}

	public void setSystemType(IRSESystemType value)
	{
		//Auto-generated method stub
	}

	public String getAliasName()
	{
		return null;
	}

	public void setAliasName(String value)
	{
		//Auto-generated method stub
	}

	public String getHostName()
	{
		return _hostName;
	}

	public void setHostName(String value)
	{
		//Auto-generated method stub
	}

	public String getDescription()
	{
		return null;
	}

	public void setDescription(String value)
	{
		//Auto-generated method stub
	}

	public String getDefaultUserId()
	{
		return null;
	}

	public void setDefaultUserId(String value)
	{
		//Auto-generated method stub
	}

	public boolean isPromptable()
	{
		return false;
	}

	public void setPromptable(boolean value)
	{
		//Auto-generated method stub
	}

	public boolean isOffline()
	{
		return false;
	}

	public void setOffline(boolean value)
	{
		//Auto-generated method stub
	}

	public IConnectorService[] getConnectorServices()
	{
		return null;
	}

	public String getName()
	{
		return null;
	}

	public IPropertySet[] getPropertySets()
	{
		return null;
	}

	public IPropertySet getPropertySet(String name)
	{
		return null;
	}

	public IPropertySet createPropertySet(String name, String description)
	{
		return null;
	}
	
	public IPropertySet createPropertySet(String name)
	{
		return null;
	}


	public boolean addPropertySet(IPropertySet set)
	{
		return false;
	}

	public boolean addPropertySets(IPropertySet[] sets)
	{
		return false;
	}

	public boolean removePropertySet(String name)
	{
		return false;
	}

	public boolean isDirty()
	{
		return false;
	}

	public void setDirty(boolean flag)
	{
	}

	public boolean commit()
	{
		return false;
	}

	public boolean wasRestored()
	{
		return false;
	}

	public void setWasRestored(boolean flag)
	{
	}
	
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}

	public boolean isTainted() {
		return false;
	}
	
	public void beginRestore() {
	}
	
	public void endRestore() {
	}

	public void setTainted(boolean flag) {
	}
	
	public String getDefaultEncoding(boolean checkRemote) {
		return null;
	}
	
	public void setDefaultEncoding(String encoding, boolean fromRemote) {
	}
	
	/**
	 * @since 3.2
	 */
	public void clonePropertySets(IPropertySetContainer targetContainer) {		
	}
	
}