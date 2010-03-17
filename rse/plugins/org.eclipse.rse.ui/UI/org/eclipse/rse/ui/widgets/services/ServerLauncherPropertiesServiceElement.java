/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight  (IBM)  - [306187] cancel in properties of remote system view doesn't work
 *******************************************************************************/

package org.eclipse.rse.ui.widgets.services;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;


public class ServerLauncherPropertiesServiceElement extends RSEModelServiceElement
{
	protected IServerLauncherProperties _launcherProperties;
	private boolean _userModified = false;
	
	public ServerLauncherPropertiesServiceElement(IHost host, ServiceElement parent, IServerLauncherProperties launcherProperties)
	{
		super(host, parent, launcherProperties);
		_launcherProperties = launcherProperties;
	}
	
	public String getName()
	{
		if (_launcherProperties instanceof ILabeledObject) {
			ILabeledObject lp = (ILabeledObject) _launcherProperties;
			return lp.getLabel();
		}
		return _launcherProperties.getName();
	}

	public Image getImage()
	{
		return RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_LAUNCHER_CONFIGURATION_ID);
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
		_userModified = true;
		_launcherProperties.restoreFromProperties();		
		_launcherProperties.commit();
		getParent().childChanged(element);
	}
	
	public boolean userModified()
	{
		return _userModified;
	}

	public void commit()
	{
		super.commit();

	}
	
	public void revert()
	{
		super.revert();
		
		_launcherProperties = _launcherProperties.getConnectorService().getRemoteServerLauncherProperties();
		_launcherProperties.restoreFromProperties();
		_launcherProperties.commit();
	}

	public IServerLauncherProperties getServerLauncherProperties()
	{
		return _launcherProperties;
	}
}
