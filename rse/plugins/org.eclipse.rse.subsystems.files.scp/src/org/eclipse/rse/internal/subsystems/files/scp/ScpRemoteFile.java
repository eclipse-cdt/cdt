/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
 * Nikita Shulga (Mentor Graphics) - Adapted from SftpRemoteFile.
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.scp;

import org.eclipse.rse.internal.services.ssh.files.SftpHostFile;
import org.eclipse.rse.internal.services.ssh.files.scp.ScpFileUtils;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

@SuppressWarnings("restriction")
public class ScpRemoteFile extends AbstractRemoteFile {

	public ScpRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context,
			IRemoteFile parent, IHostFile hostFile) {
		super(ss, context, parent, hostFile);
	}

	public SftpHostFile getSftpHostFile() {
		return (SftpHostFile) getHostFile();
	}

	public String getCanonicalPath() {
		String canonicalPath = getSftpHostFile().getCanonicalPath();
		if (canonicalPath.equals(getAbsolutePath()) && _parentFile != null) {
			String parentCanonicalPath = _parentFile.getCanonicalPath();
			StringBuffer path = new StringBuffer(parentCanonicalPath);
			if (!parentCanonicalPath.endsWith(ScpFileUtils.TARGET_SEPARATOR))
				path.append(ScpFileUtils.TARGET_SEPARATOR);

			path.append(getName());
			canonicalPath = path.toString();
		}
		return canonicalPath;
	}

	public String getClassification() {
		return getSftpHostFile().getClassification();
	}
}
