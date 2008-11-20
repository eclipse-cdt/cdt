/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 * David McKnight   (IBM)        - [244765] Invalid thread access during workbench termination
 * David McKnight   (IBM)        - [255699] NPE when filter string doesn't return result in FileServiceSubSystem.list
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.dstore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.internal.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.internal.services.dstore.files.DStoreVirtualHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class DStoreFileAdapter implements IHostFileToRemoteFileAdapter
{
	private RemoteFilePropertyChangeListener _listener;

	private void registerFilePropertyChangeListener(FileServiceSubSystem ss)
	{
		if (_listener == null)
		{
			DStoreConnectorService connectorService = (DStoreConnectorService)ss.getConnectorService();
			IWorkbench wb = PlatformUI.getWorkbench();
			if (!wb.isClosing()) {
				Shell shell = SystemBasePlugin.getActiveWorkbenchShell();
				_listener = new RemoteFilePropertyChangeListener(shell, connectorService, connectorService.getDataStore(), ss);
			}
		}
	}


	public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes)
	{
		registerFilePropertyChangeListener(ss);

		List results = new ArrayList();

		for (int i = 0; i < nodes.length; i++)
		{
			DStoreHostFile node = (DStoreHostFile)nodes[i];

			if (node != null){
				DStoreFile lfile = null;
	
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

		return (DStoreFile[]) results.toArray(new DStoreFile[results.size()]);
	}


	public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node)
	{
		registerFilePropertyChangeListener(ss);

		DStoreFile file = null;

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

}
