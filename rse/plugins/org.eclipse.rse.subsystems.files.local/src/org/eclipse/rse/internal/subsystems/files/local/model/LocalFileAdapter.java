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
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.local.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.services.local.files.LocalHostFile;
import org.eclipse.rse.internal.services.local.files.LocalVirtualHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;


public class LocalFileAdapter implements IHostFileToRemoteFileAdapter
{

	public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes)
	{
		if (nodes == null) return null;

		List results = new ArrayList();
		for (int i = 0; i < nodes.length; i++)
		{
			IHostFile child = nodes[i];

			LocalFile lfile;

			if (child instanceof LocalVirtualHostFile)
			{
				LocalVirtualHostFile node = (LocalVirtualHostFile)child;
				lfile = new LocalVirtualFile(ss, context, node);
			}
			else
			{
				LocalHostFile node = (LocalHostFile)child;
				lfile = new LocalFile(ss, context, parent, node);
			}

			results.add(lfile);
			ss.cacheRemoteFile(lfile);
		}

		return (LocalFile[]) results.toArray(new LocalFile[results.size()]);
	}

	public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node)
	{
		LocalFile file = null;
		if (node instanceof LocalVirtualHostFile)
			file = new LocalVirtualFile(ss, context, (LocalVirtualHostFile) node);
		else file = new LocalFile(ss, context, parent, (LocalHostFile)node);
		ss.cacheRemoteFile(file);
		return file;
	}
}
