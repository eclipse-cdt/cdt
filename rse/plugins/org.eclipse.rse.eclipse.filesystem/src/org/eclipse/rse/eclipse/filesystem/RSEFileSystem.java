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

package org.eclipse.rse.eclipse.filesystem;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;

public class RSEFileSystem extends FileSystem 
{

	public boolean canDelete() 
	{
		return true;
	}
	
	public boolean canWrite() 
	{
		return true;
	}

	private IHost getConnectionFor(String hostName)
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		IHost[] connections = sr.getHosts();
		for (int i = 0; i < connections.length; i++)
		{
			IHost con = connections[i];
			if (con.getHostName().equalsIgnoreCase(hostName))
			{
				return con;
			}
		}
		return null;
	}
	
	public IFileStore getStore(URI uri) 
	{
		try 
		{
			String path = uri.getPath();
			String hostName = uri.getHost();
			IHost con = getConnectionFor(hostName);
			if (con != null)
			{
				IRemoteFileSubSystem fs =  RemoteFileUtility.getFileSubSystem(con);
				if (fs != null)
				{
					return FileStoreConversionUtility.convert(null, fs.getRemoteFileObject(path));
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
}