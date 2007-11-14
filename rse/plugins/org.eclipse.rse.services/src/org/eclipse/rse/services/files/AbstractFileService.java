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
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 ********************************************************************************/

package org.eclipse.rse.services.files;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


public abstract class AbstractFileService implements IFileService
{
	protected abstract IHostFile[] internalFetch(String parentPath, String fileFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException;
	
	public IHostFile[] getFileMultiple(String remoteParents[], String names[], IProgressMonitor monitor) 
								throws SystemMessageException
	{
		List results = new ArrayList();
		for (int i = 0; i < remoteParents.length; i++)
		{
			results.add(getFile(remoteParents[i], names[i], monitor));
		}
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}

	public IHostFile[] list(String remoteParent, String fileFilter, 
			int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		return internalFetch(remoteParent, fileFilter, fileType, monitor);
	}
	
	public IHostFile[] listMultiple(String[] remoteParents,
			String[] fileFilters, int fileTypes[], IProgressMonitor monitor)
			throws SystemMessageException {

		List files = new ArrayList();
		for (int i = 0; i < remoteParents.length; i++)
		{
			IHostFile[] result = list(remoteParents[i], fileFilters[i], fileTypes[i], monitor);
			for (int j = 0; j < result.length; j++)
			{
				files.add(result[j]);
			}
		}
		
		return (IHostFile[])files.toArray(new IHostFile[files.size()]);
	}

	public IHostFile[] listMultiple(String[] remoteParents,
			String[] fileFilters, int fileType, IProgressMonitor monitor)
			throws SystemMessageException {

		List files = new ArrayList();
		for (int i = 0; i < remoteParents.length; i++)
		{
			IHostFile[] result = list(remoteParents[i], fileFilters[i], fileType, monitor);
			for (int j = 0; j < result.length; j++)
			{
				files.add(result[j]);
			}
		}
		
		return (IHostFile[])files.toArray(new IHostFile[files.size()]);
	}
	
	protected boolean isRightType(int fileType, IHostFile node)
	{
		switch (fileType)
		{
		case IFileServiceConstants.FILE_TYPE_FILES_AND_FOLDERS:
			return true;
		case IFileServiceConstants.FILE_TYPE_FILES:
			if (node.isFile())
			{
				return true;			
			}
			else
			{
				return false;
			}				
		case IFileServiceConstants.FILE_TYPE_FOLDERS:
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
		SystemMessage msg = getMessage("RSEF1315");   //$NON-NLS-1$
		String deletingMessage = msg.makeSubstitution("").getLevelOneText(); //$NON-NLS-1$
		monitor.beginTask(deletingMessage, remoteParents.length);
		for (int i = 0; i < remoteParents.length; i++)
		{
			monitor.subTask(msg.makeSubstitution(fileNames[i]).getLevelOneText());
			ok = ok && delete(remoteParents[i], fileNames[i], monitor);
			monitor.worked(1);
		}
		return ok;
	}

	/**
	 * Default implementation - just iterate through each file
	 */
	public boolean downloadMulti(String[] remoteParents, String[] remoteFiles,
			File[] localFiles, boolean[] isBinaries, String[] hostEncodings,
			IProgressMonitor monitor) throws SystemMessageException 
	{
		boolean result = true;
		for (int i = 0; i < remoteParents.length && result == true; i++)
		{
			String remoteParent = remoteParents[i];
			String remoteFile = remoteFiles[i];
			File localFile = localFiles[i];
			boolean isBinary = isBinaries[i];
			String hostEncoding = hostEncodings[i];
			result = download(remoteParent, remoteFile, localFile, isBinary, hostEncoding, monitor);
		}
		return result;
	}

	/**
	 * Default implementation - just iterate through each file
	 */
	public boolean uploadMulti(File[] localFiles, String[] remoteParents,
			String[] remoteFiles, boolean[] isBinaries, String[] srcEncodings,
			String[] hostEncodings, IProgressMonitor monitor)
			throws SystemMessageException 
	{
		boolean result = true;
		for (int i = 0; i < localFiles.length && result == true; i++)
		{
			File localFile = localFiles[i];
			String remoteParent = remoteParents[i];
			String remoteFile = remoteFiles[i];
			
			boolean isBinary = isBinaries[i];
			String srcEncoding = srcEncodings[i];
			String hostEncoding = hostEncodings[i];
			result = upload(localFile, remoteParent, remoteFile, isBinary, srcEncoding, hostEncoding, monitor);
		}
		return result;
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
	
	/**
	 * @deprecated
	 */
	public IHostFile[] getFiles(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException 
	{
		return internalFetch(remoteParent, fileFilter, IFileServiceConstants.FILE_TYPE_FILES, monitor);
	}

	/**
	 * @deprecated
	 */
	public IHostFile[] getFolders(String remoteParent, String fileFilter, IProgressMonitor monitor) throws SystemMessageException 
	{
		return internalFetch(remoteParent, fileFilter, IFileServiceConstants.FILE_TYPE_FOLDERS, monitor);
	}
	
	/**
	 * @deprecated
	 */
	public IHostFile[] getFilesAndFolders(String parentPath, String fileFilter, IProgressMonitor monitor) throws SystemMessageException
	{
		return internalFetch(parentPath, fileFilter, IFileServiceConstants.FILE_TYPE_FILES_AND_FOLDERS, monitor);
	}
}