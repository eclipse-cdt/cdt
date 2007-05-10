/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 ********************************************************************************/

package org.eclipse.rse.services.files;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


public abstract class AbstractFileService implements IFileService
{

	public static final int FILE_TYPE_FILES_AND_FOLDERS = 0;
	public static final int FILE_TYPE_FILES = 1;
	public static final int FILE_TYPE_FOLDERS = 2;
 
	public IHostFile[] getFiles(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException 
	{
		return internalFetch(monitor, remoteParent, fileFilter, FILE_TYPE_FILES);
	}

	public IHostFile[] getFolders(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException 
	{
		return internalFetch(monitor, remoteParent, fileFilter, FILE_TYPE_FOLDERS);
	}
	
	public IHostFile[] getFilesAndFolders(String parentPath, String fileFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		return internalFetch(monitor, parentPath, fileFilter, FILE_TYPE_FILES_AND_FOLDERS);
	}
	
	protected abstract IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType) throws SystemMessageException;
	
	
	protected boolean isRightType(int fileType, IHostFile node)
	{
		switch (fileType)
		{
		case FILE_TYPE_FILES_AND_FOLDERS:
			return true;
		case FILE_TYPE_FILES:
			if (node.isFile())
			{
				return true;			
			}
			else
			{
				return false;
			}				
		case FILE_TYPE_FOLDERS:
			if (node.isDirectory())
			{
				return true;
			}
			else
			{
				return false;
			}
			default:
				return true;
		}
	}
	
	/**
	 * Dummy impl for now
	 */	
	public SystemMessage getMessage(String messageID)
	{
		return null;
	}

	public boolean deleteBatch(String[] remoteParents, String[] fileNames, IProgressMonitor monitor) throws SystemMessageException
	{
		boolean ok = true;
		for (int i = 0; i < remoteParents.length; i++)
		{
			ok = ok && delete(remoteParents[i], fileNames[i], monitor);
		}
		return ok;
	}

	/**
	 * Returns the local platform encoding by default. Subclasses should override to return the actual remote encoding.
	 * @see org.eclipse.rse.services.files.IFileService#getEncoding(org.eclipse.core.runtime.IProgressMonitor)
	 * @since 2.0
	 */
	public String getEncoding(IProgressMonitor monitor) throws SystemMessageException {
		return System.getProperty("file.encoding"); //$NON-NLS-1$
	}

	/**
	 * The default implementation returns <code>null</code>. Clients can override to return an input stream to the file.
	 * @see org.eclipse.rse.services.files.IFileService#getInputStream(String, String, boolean, IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return null;
	}

	/**
	 * The default implementation returns <code>null</code>. Clients can override to return an output stream to the file.
	 * @see org.eclipse.rse.services.files.IFileService#getOutputStream(String, String, boolean, IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return null;
	}
}