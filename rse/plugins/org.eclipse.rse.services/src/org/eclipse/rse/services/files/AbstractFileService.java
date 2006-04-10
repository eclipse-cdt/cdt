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

package org.eclipse.rse.services.files;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;


public abstract class AbstractFileService implements IFileService
{

	public static final int FILE_TYPE_FILES_AND_FOLDERS = 0;
	public static final int FILE_TYPE_FILES = 1;
	public static final int FILE_TYPE_FOLDERS = 2;
 

	
	public IHostFile[] getFiles(IProgressMonitor monitor, String remoteParent, String fileFilter) 
	{
		return internalFetch(monitor, remoteParent, fileFilter, FILE_TYPE_FILES);
	}

	public IHostFile[] getFolders(IProgressMonitor monitor, String remoteParent, String fileFilter) 
	{
		return internalFetch(monitor, remoteParent, fileFilter, FILE_TYPE_FOLDERS);
	}
	
	public IHostFile[] getFilesAndFolders(IProgressMonitor monitor, String parentPath, String fileFilter)
	{
		return internalFetch(monitor, parentPath, fileFilter, FILE_TYPE_FILES_AND_FOLDERS);
	}
	
	protected abstract IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType);
	
	
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

	public boolean deleteBatch(IProgressMonitor monitor, String[] remoteParents, String[] fileNames) throws SystemMessageException
	{
		boolean ok = true;
		for (int i = 0; i < remoteParents.length; i++)
		{
			ok = ok && delete(monitor, remoteParents[i], fileNames[i]);
		}
		return ok;
	}
	
	
}