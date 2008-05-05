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
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [162195] new APIs for upload multi and download multi
 * Xuan Chen        (IBM)        - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [209552] API changes to use multiple and getting rid of deprecated
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 * Martin Oberhuber (Wind River) - [210109] no need to declare IFileService constants in AbstractFileService
 * David McKnight   (IBM)        - [209704] [api] Ability to override default encoding conversion needed.
 * Xuan Chen        (IBM)        - [210555] [regression] NPE when deleting a file on SSH
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 * David McKnight   (IBM)        - [209704] added supportsEncodingConversion()
 * David McKnight   (IBM)        - [216252] use SimpleSystemMessage instead of getMessage()
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable and add Javadoc
 *******************************************************************************/

package org.eclipse.rse.services.files;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.AbstractService;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


public abstract class AbstractFileService extends AbstractService implements IFileService
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

	public void listMultiple(String[] remoteParents,
			String[] fileFilters, int fileTypes[], List hostFiles, IProgressMonitor monitor)
			throws SystemMessageException {

		for (int i = 0; i < remoteParents.length; i++)
		{
			IHostFile[] result = list(remoteParents[i], fileFilters[i], fileTypes[i], monitor);
			for (int j = 0; j < result.length; j++)
			{
				hostFiles.add(result[j]);
			}
		}

	}

	public void listMultiple(String[] remoteParents,
			String[] fileFilters, int fileType, List hostFiles, IProgressMonitor monitor)
			throws SystemMessageException {

		for (int i = 0; i < remoteParents.length; i++)
		{
			IHostFile[] result = list(remoteParents[i], fileFilters[i], fileType, monitor);
			for (int j = 0; j < result.length; j++)
			{
				hostFiles.add(result[j]);
			}
		}

	}

	protected boolean isRightType(int fileType, IHostFile node)
	{
		switch (fileType)
		{
		case IFileService.FILE_TYPE_FILES_AND_FOLDERS:
			return true;
		case IFileService.FILE_TYPE_FILES:
			if (node.isFile())
			{
				return true;
			}
			else
			{
				return false;
			}
		case IFileService.FILE_TYPE_FOLDERS:
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


	public void deleteBatch(String[] remoteParents, String[] fileNames, IProgressMonitor monitor) throws SystemMessageException
	{
		for (int i = 0; i < remoteParents.length; i++)
		{
			delete(remoteParents[i], fileNames[i], monitor);
		}
	}

	/**
	 * Default implementation - just iterate through each file
	 */
	public void downloadMultiple(String[] remoteParents, String[] remoteFiles,
			File[] localFiles, boolean[] isBinaries, String[] hostEncodings,
			IProgressMonitor monitor) throws SystemMessageException
	{
		for (int i = 0; i < remoteParents.length; i++)
		{
			String remoteParent = remoteParents[i];
			String remoteFile = remoteFiles[i];
			File localFile = localFiles[i];
			boolean isBinary = isBinaries[i];
			String hostEncoding = hostEncodings[i];
			download(remoteParent, remoteFile, localFile, isBinary, hostEncoding, monitor);
		}
	}

	/**
	 * Default implementation - just iterate through each file
	 */
	public void uploadMultiple(File[] localFiles, String[] remoteParents,
			String[] remoteFiles, boolean[] isBinaries, String[] srcEncodings,
			String[] hostEncodings, IProgressMonitor monitor)
			throws SystemMessageException
	{
		boolean result = true;
		for (int i = 0; i < localFiles.length; i++)
		{
			File localFile = localFiles[i];
			String remoteParent = remoteParents[i];
			String remoteFile = remoteFiles[i];

			boolean isBinary = isBinaries[i];
			String srcEncoding = srcEncodings[i];
			String hostEncoding = hostEncodings[i];
			upload(localFile, remoteParent, remoteFile, isBinary, srcEncoding, hostEncoding, monitor);
		}
	}

	/**
	 * Returns the local platform encoding by default. Subclasses should override to return the actual remote encoding.
	 * @see org.eclipse.rse.services.files.IFileService#getEncoding(org.eclipse.core.runtime.IProgressMonitor)
	 * @since 2.0
	 */
	public String getEncoding(IProgressMonitor monitor) throws SystemMessageException {
		return SystemEncodingUtil.getInstance().getLocalDefaultEncoding();
	}

	/**
	 * The default implementation returns <code>null</code>. Clients can override to return an input stream to the file.
	 * @see org.eclipse.rse.services.files.IFileService#getInputStream(String, String, boolean, IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return null;
	}

	/**
	 * Gets the output stream to write/append to a remote file. The default
	 * implementation returns <code>null</code>. Clients can override to
	 * return an output stream to the file.
	 * 
	 * @deprecated use
	 *             {@link #getOutputStream(String, String, int, IProgressMonitor)}
	 *             instead
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		return null;
	}

	/**
	 * Gets the output stream to write/append to a remote file.
	 * The default implementation returns <code>null</code>.
	 * Clients can override to return an output stream to the file.
	 * @see org.eclipse.rse.services.files.IFileService#getOutputStream(String, String, int, IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException {
		if ((options & IFileService.APPEND) == 0) {
			//forward to old deprecated implementation for backward compatibility with old services
			boolean isBinary = (options & IFileService.TEXT_MODE) == 0 ? true : false;
			return getOutputStream(remoteParent, remoteFile, isBinary, monitor);
		}
		return null;
	}

	/**
	 * The default implementation returns false.  Clients should override this method if they make use
	 * of IFileServiceCodePageConverter to do conversion during download and upload.
	 */
	public boolean supportsEncodingConversion(){
		return false;
	}
}
