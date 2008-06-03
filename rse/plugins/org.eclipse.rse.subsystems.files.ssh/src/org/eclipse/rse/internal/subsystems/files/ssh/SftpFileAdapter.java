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
 * Martin Oberhuber (Wind River) - Adapted from FTPFileAdapter.
 * Martin Oberhuber (Wind River) - [235363][api][breaking] IHostFileToRemoteFileAdapter methods should return AbstractRemoteFile
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ssh;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.services.ssh.files.SftpHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class SftpFileAdapter implements IHostFileToRemoteFileAdapter {

	public AbstractRemoteFile[] convertToRemoteFiles(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile[] nodes) {

		List results = new ArrayList();
		if (nodes!=null) {
			for (int i = 0; i < nodes.length; i++) {
				SftpHostFile node = (SftpHostFile)nodes[i];
				SftpRemoteFile remoteFile = new SftpRemoteFile(ss, context, parent, node);
				results.add(remoteFile);
				ss.cacheRemoteFile(remoteFile);
			}
		}
		return (SftpRemoteFile[]) results.toArray(new SftpRemoteFile[results.size()]);
	}

	public AbstractRemoteFile convertToRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, IHostFile node) {
		SftpRemoteFile file = new SftpRemoteFile(ss, context, parent, (SftpHostFile) node);
		ss.cacheRemoteFile(file);
		return file;
	}

}
