/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Adapted from FTPRemoteFile.
 * Martin Oberhuber (Wind River) - [216343] immediate link targets and canonical paths for Sftp
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ssh;

import org.eclipse.rse.internal.services.ssh.files.SftpHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class SftpRemoteFile extends AbstractRemoteFile {

	public SftpRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, SftpHostFile hostFile) {
		super(ss, context, parent, hostFile);
	}
	
	public SftpHostFile getSftpHostFile() {
		return (SftpHostFile)getHostFile();
	}

	public boolean isVirtual() {
		return false;
	}

	public String getCanonicalPath() {
		String canPath = getSftpHostFile().getCanonicalPath();
		if (canPath.equals(getAbsolutePath()) && _parentFile!=null) {
			String parentCanPath = _parentFile.getCanonicalPath();
			StringBuffer path = new StringBuffer(parentCanPath);
			if (!parentCanPath.endsWith("/")) //$NON-NLS-1$
			{
				path.append('/');
			}
			path.append(getName());
			canPath = path.toString();
		}
		return canPath;
	}

	public String getClassification() {
		return getSftpHostFile().getClassification();
	}

}
