/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 * Javier Montalvo Orus (Symbian) - [198272] FTP should return classification for symbolic links so they show a link overlay
 * Martin Oberhuber (Wind River) - [204669] Fix ftp path concatenation on systems using backslash separator
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.ftp.model;


import org.eclipse.rse.internal.services.files.ftp.FTPHostFile;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;



public class FTPRemoteFile extends AbstractRemoteFile 
{
	protected FTPHostFile _ftpHostFile;

	public FTPRemoteFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, FTPHostFile hostFile) 
	{
		super(ss, context, parent, hostFile);
		_ftpHostFile = hostFile;
	}

	public boolean isVirtual()
	{
		return false;
	}

	public String getCanonicalPath()
	{
		return getAbsolutePath();
	}

	public String getClassification()
	{
		return _ftpHostFile.getClassification();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile#getSeparator()
	 */
	public String getSeparator() {
		String absPath = getAbsolutePath();
		if (absPath!=null && absPath.length()>1) {
			return PathUtility.getSeparator(absPath);
		}
		String home = getParentRemoteFileSubSystem().getConnectorService().getHomeDirectory();
		if (home!=null && home.length()>1) {
			return PathUtility.getSeparator(home);
		}
		return PathUtility.getSeparator(absPath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile#getSeparatorChar()
	 */
	public char getSeparatorChar() {
		return getSeparator().charAt(0);
	}

}
