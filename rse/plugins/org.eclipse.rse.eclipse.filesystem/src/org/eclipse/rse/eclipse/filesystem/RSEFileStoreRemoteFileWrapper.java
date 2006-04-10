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

package org.eclipse.rse.eclipse.filesystem;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;


public class RSEFileStoreRemoteFileWrapper extends FileStore implements IFileStore 
{
	private IRemoteFile _remoteFile;
	private IFileStore _parent;
	public RSEFileStoreRemoteFileWrapper(IFileStore parent, IRemoteFile remoteFile)
	{
		_remoteFile = remoteFile;
		_parent = parent;
	}
	public String[] childNames(int options, IProgressMonitor monitor) 
	{
		String[] names;
		if (!_remoteFile.isStale() && _remoteFile.hasContents(RemoteChildrenContentsType.getInstance()))
		{
			Object[] children = _remoteFile.getContents(RemoteChildrenContentsType.getInstance());
			names = new String[children.length];
			                
			for (int i = 0; i < children.length; i++)
			{
				names[i] = ((IRemoteFile)children[i]).getName();
			}
		}
		else
		{
			IRemoteFile[] children = _remoteFile.getParentRemoteFileSubSystem().listFoldersAndFiles(_remoteFile);
			names = new String[children.length];
			for (int i = 0; i < children.length; i++)
			{
				names[i] = ((IRemoteFile)children[i]).getName();
			}		
		}
		return names;
	}
	
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException 
	{	
		FileInfo info = new FileInfo(getName());
		info.setExists(true);
		info.setLastModified(_remoteFile.getLastModified());
		boolean isDir = _remoteFile.isDirectory();
		info.setDirectory(isDir);
		if (!isDir)
		{
			info.setLength(_remoteFile.getLength());
		}
		
		info.setName(getName());
		return info;
	}
	public IFileStore getChild(String name) 
	{
		if (!_remoteFile.isStale() && _remoteFile.hasContents(RemoteChildrenContentsType.getInstance(), name))
		{
			Object[] children = _remoteFile.getContents(RemoteChildrenContentsType.getInstance(), name);
			if (children != null && children.length > 0)
			{
				return FileStoreConversionUtility.convert(_parent, (IRemoteFile)children[0]);
			}
		}
		else
		{
			IRemoteFile[] children = _remoteFile.getParentRemoteFileSubSystem().listFoldersAndFiles(_remoteFile, name);
			if (children != null && children.length > 0)
			{
				return FileStoreConversionUtility.convert(_parent, children[0]);
			}			
		}
		return null;
	}
	public String getName() 
	{
		return _remoteFile.getName();
	}
	public IFileStore getParent() 
	{
		return _parent;
	}
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException 
	{
		IFile file = UniversalFileTransferUtility.copyRemoteFileToWorkspace(_remoteFile, monitor);
		return file.getContents();
	}
	
	public URI toURI() 
	{
		try 
		{
			return new URI("rse", _remoteFile.getAbsolutePath(), null); //$NON-NLS-1$
		} 
		catch (URISyntaxException e) 
		{
			throw new RuntimeException(e);
		}	
	}

}