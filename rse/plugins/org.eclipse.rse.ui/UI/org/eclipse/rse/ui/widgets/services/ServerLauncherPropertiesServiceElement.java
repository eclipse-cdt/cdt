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

package org.eclipse.rse.ui.widgets.services;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.swt.graphics.Image;


public class ServerLauncherPropertiesServiceElement extends RSEModelServiceElement
{
	protected IServerLauncherProperties _launcherProperties;
	
	public ServerLauncherPropertiesServiceElement(IHost host, ServiceElement parent, IServerLauncherProperties launcherProperties)
	{
		super(host, parent, launcherProperties);
		_launcherProperties = launcherProperties;
	}
	
	public String getName()
	{
		return _launcherProperties.getName();
	}

	public Image getImage()
	{
		return SystemPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_LAUNCHER_CONFIGURATION_ID);
	}
	
	protected ServiceElement[] internalGetChildren()
	{
		return null;
	}


	protected boolean internalHasChildren()
	{
		return false;
	}
	
	public void childChanged(ServiceElement element)
	{
		_launcherProperties.restoreFromProperties();		
		_launcherProperties.commit();
		getParent().childChanged(element);
	}

	public void commit()
	{
		super.commit();

	}
	
	public void revert()
	{
		super.revert();		
	}

}