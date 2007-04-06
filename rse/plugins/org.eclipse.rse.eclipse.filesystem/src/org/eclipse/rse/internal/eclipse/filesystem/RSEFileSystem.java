/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * Kushal Munir (IBM) - moved to internal package
 ********************************************************************************/

package org.eclipse.rse.internal.eclipse.filesystem;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;

public class RSEFileSystem extends FileSystem 
{
	private static RSEFileSystem _instance = new RSEFileSystem();
	
	public RSEFileSystem() {
		super();
	}
	
	public static RSEFileSystem getInstance() {
		return _instance;
	}
	
	public boolean canDelete() {
		return true;
	}
	
	public boolean canWrite() {
		return true;
	}

	public static IHost getConnectionFor(String hostName) {
		
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		IHost[] connections = sr.getHosts();
		IHost unconnected = null;
		
		for (int i = 0; i < connections.length; i++) {
			
			IHost con = connections[i];
			
			if (con.getHostName().equalsIgnoreCase(hostName)) {
				
				boolean isConnected = false;
				IConnectorService[] connectorServices = con.getConnectorServices();
				
				for (int c = 0; c < connectorServices.length  && !isConnected; c++)
				{
					IConnectorService serv = connectorServices[c];
					isConnected = serv.isConnected();
				}
				
				if (isConnected) {
					return con;
				}
				else {
					unconnected = con;
				}
			}
		}
		
		return unconnected;
	}
	
	public static IRemoteFileSubSystem getRemoteFileSubSystem(IHost host) {
		return RemoteFileUtility.getFileSubSystem(host);
	}
	
	public URI getURIFor(IRemoteFile file) {
		String path = file.getAbsolutePath();
		
		if (path.charAt(0) != '/') {
			path = "/" + path.replace('\\', '/'); //$NON-NLS-1$
		}
	
		try {
			return new URI("rse", file.getParentRemoteFileSubSystem().getHost().getHostName(), path, null); //$NON-NLS-1$
		}
		catch (URISyntaxException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public IFileStore getStore(URI uri) {
		
		try  {
			
			String path = uri.getPath();
			String hostName = uri.getHost();
			IHost con = getConnectionFor(hostName);
			
			if (con != null) {
				
				IRemoteFileSubSystem fs = getRemoteFileSubSystem(con);
				
				if (fs != null) {
					Path absPath = new Path(path);
					return new RSEFileStore(null, fs, absPath.removeLastSegments(1).toString(), absPath.lastSegment());
				}
				else {
					return EFS.getNullFileSystem().getStore(uri);
				}
			}
			else {
				return EFS.getNullFileSystem().getStore(uri);
			}
		} 
		catch (Exception e) {
			return EFS.getNullFileSystem().getStore(uri);
		}
	}
}