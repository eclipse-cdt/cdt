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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 ********************************************************************************/

package org.eclipse.rse.internal.core.subsystems;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.internal.core.RSECoreMessages;


public abstract class ServerLauncher extends RSEModelObject implements IServerLauncherProperties, ILabeledObject
{
	

	protected String _name;
	private String _label = null;
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
	
	public String getLabel() {
		if (_label != null) return _label;
		return _name;
	}
	
	public void setLabel(String label) {
		_label = label;
		setDirty(true);
	}
	
	public String getDescription()
	{
		return RSECoreMessages.RESID_MODELOBJECTS_SERVERLAUNCHER_DESCRIPTION;
	}
	
	public IConnectorService getConnectorService()
	{
		return _connectorService;
	}
	
	public IRSEPersistableContainer getPersistableParent() {
		return _connectorService;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = Arrays.asList(getPropertySets());
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
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
	 * @deprecated use property sets
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
			set = createPropertySet(vendorName, ""); //$NON-NLS-1$
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
		return getConnectorService().getHost().commit();
	}
	
	

} //ServerLauncherImpl