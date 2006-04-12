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

package org.eclipse.rse.subsystems.files.dstore.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.services.dstore.files.DStoreVirtualHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.dstore.subsystem.RemoteFilePropertyChangeListener;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;

import org.eclipse.dstore.core.model.DataElement;

public class DStoreFileAdapter implements IHostFileToRemoteFileAdapter
{
	private RemoteFilePropertyChangeListener _listener;
	
	private void registerFilePropertyChangeListener(FileServiceSubSystem ss)
	{
		if (_listener == null)
		{
			DStoreConnectorService connectorService = (DStoreConnectorService)ss.getConnectorService();
			_listener = new RemoteFilePropertyChangeListener(SystemBasePlugin.getActiveWorkbenchShell(), connectorService, connectorService.getDataStore(), ss);
		}
	}


	public IRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes) 
	{
		registerFilePropertyChangeListener(ss);
		boolean showHidden = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.SHOWHIDDEN);
		
		List results = new ArrayList();
		for (int i = 0; i < nodes.length; i++) 
		{
			DStoreHostFile node = (DStoreHostFile)nodes[i];
			if (showHidden || !node.isHidden())
			{
				IRemoteFile lfile = null;
					
					if (node instanceof DStoreVirtualHostFile)
					{
						lfile = new DStoreVirtualFile(ss, context, parent, (DStoreVirtualHostFile) node);
					}
					else
					{
						lfile = new DStoreFile(ss, context, parent, node);
					}
				results.add(lfile);
				ss.cacheRemoteFile(lfile);
			}
		}
		return (IRemoteFile[])results.toArray(new IRemoteFile[results.size()]);
	}



	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node) 
	{
		registerFilePropertyChangeListener(ss);
		
		IRemoteFile file = null;
		
		if (node instanceof DStoreVirtualHostFile)
		{
			file = new DStoreVirtualFile(ss, context, parent, (DStoreVirtualHostFile)node);
		}
		else
		{
			file = new DStoreFile(ss, context, parent, (DStoreHostFile)node);
		}
		ss.cacheRemoteFile(file);
		return file;
	}


	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, String name, boolean isDirectory, boolean isRoot)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, Object object)
	{
		registerFilePropertyChangeListener(ss);
		if (object instanceof DataElement)
		{
			DStoreHostFile hostFile = new DStoreHostFile((DataElement)object);
			IRemoteFile file = null;
	
			{
				file = new DStoreFile(ss, context, parent, hostFile);
			}
			ss.cacheRemoteFile(file);
			return file;
		}
		else
		{
			return null;
		}
	}
}