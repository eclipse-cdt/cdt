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

package org.eclipse.rse.core.internal.subsystems;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.internal.model.RSEModelObject;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;


public abstract class ServerLauncher extends RSEModelObject implements IServerLauncherProperties
{
	

	protected String _name;
	protected IConnectorService _connectorService;

	protected ServerLauncher(String name, IConnectorService service)
	{
		super();
		_name = name;
		_connectorService = service; 
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescription()
	{
		return SystemResources.RESID_MODELOBJECTS_SERVERLAUNCHER_DESCRIPTION;
	}
	
	public IConnectorService getConnectorService()
	{
		return _connectorService;
	}

	/**
	 * Clone the contents of this server launcher into the given server launcher
	 * <i>Your sublcass must override this if you add additional attributes! Be sure
	 *  to call super.cloneServerLauncher(newOne) first.</i>
	 * @return the given new server launcher, for convenience.
	 */
	public IServerLauncherProperties cloneServerLauncher(IServerLauncherProperties newOne)
	{
		newOne.addPropertySets(getPropertySets());
		return newOne; 
	}
	
	/**
	 * @deprecated
	 * @param vendorName
	 * @param attributeName
	 * @param attributeValue
	 */
	public void setVendorAttribute(String vendorName, 
									String attributeName, String attributeValue)
	{
		IPropertySet set = getPropertySet(vendorName);
		if (set == null)
		{
			set = createPropertySet(vendorName, "");
		}
		set.addProperty(attributeName, attributeValue);
	}
 
	/**
	 * @deprecated use property sets directly now
	 */
	public String getVendorAttribute(String vendor, String attributeName)
	{
		IPropertySet set = getPropertySet(vendor);
		if (set != null)
		{
			return set.getPropertyValue(attributeName);
		}
		else
		{
			return null;
		}
	} 
		


	public boolean commit() 
	{
		return RSEUIPlugin.getThePersistenceManager().commit(getConnectorService().getHost());
	}
	
	

} //ServerLauncherImpl