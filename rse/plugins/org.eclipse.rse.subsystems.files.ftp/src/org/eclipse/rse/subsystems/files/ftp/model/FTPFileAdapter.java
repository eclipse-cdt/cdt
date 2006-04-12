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

package org.eclipse.rse.subsystems.files.ftp.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.ftp.FTPHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;


public class FTPFileAdapter implements IHostFileToRemoteFileAdapter
{
	
	public IRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes) 
	{
		boolean showHidden = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.SHOWHIDDEN);

		List results = new ArrayList();
		for (int i = 0; i < nodes.length; i++) 
		{
			FTPHostFile node = (FTPHostFile)nodes[i];
			if (showHidden || !node.isHidden())
			{
				IRemoteFile ftpFile = new FTPRemoteFile(ss, context, parent, node);
				results.add(ftpFile);
				ss.cacheRemoteFile(ftpFile);
			}
		}
		return (IRemoteFile[])results.toArray(new IRemoteFile[results.size()]);
	}

	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, String name, boolean isDirectory, boolean isRoot) 
	{
		return null;
	}

	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node)
	{
		IRemoteFile file = new FTPRemoteFile(ss, context, parent, (FTPHostFile)node);
		ss.cacheRemoteFile(file);
		return file;
	}

	public IRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, Object object)
	{
		// TODO Auto-generated method stub
		return null;
	}
}