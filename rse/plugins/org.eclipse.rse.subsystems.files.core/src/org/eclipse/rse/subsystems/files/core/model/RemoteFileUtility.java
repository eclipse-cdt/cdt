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

package org.eclipse.rse.subsystems.files.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;



public class RemoteFileUtility
{

	public static IRemoteFileSubSystem getFileSubSystem(IHost connection)
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)	
			{
				IRemoteFileSubSystem subSystem = (IRemoteFileSubSystem)sses[i];
				return subSystem;
			}
		}
		return null;
	}
	
	public static IRemoteFileSubSystem[] getFileSubSystems(IHost connection)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)	
			{
				IRemoteFileSubSystem subSystem = (IRemoteFileSubSystem)sses[i];
				results.add(subSystem);
			}
		}
		return (IRemoteFileSubSystem[])results.toArray(new IRemoteFileSubSystem[results.size()]);
	}
	
	 public static IRemoteFileSubSystemConfiguration getFileSubSystemConfiguration(String systemType)
	 {
			ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
			ISubSystemConfiguration[] sses = sr.getSubSystemConfigurationsBySystemType(systemType);
			for (int i = 0; i < sses.length; i++)
			{
				if (sses[i] instanceof IRemoteFileSubSystemConfiguration)	
				{
					return (IRemoteFileSubSystemConfiguration)sses[i];
				}
			}
			return null;
	 }
		
}