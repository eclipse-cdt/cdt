/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.files.ftp;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;

public class FTPHostFile implements IHostFile
{
	private String _name;
	private String _parentPath;
	private boolean _isDirectory = false;
	private boolean _isRoot = false;
	private boolean _isArchive = false;
	private long _lastModified = 0;
	private long _size = 0;
	
	public FTPHostFile(String parentPath, String name, boolean isDirectory, boolean isRoot, long lastModified, long size)
	{
		_parentPath = parentPath;
		_name = name;
		_isDirectory = isDirectory;
		_isRoot = isRoot;
		_lastModified = lastModified;
		_size = size;
		_isArchive = internalIsArchive();
	}

	public String getName()
	{
		return _name;			
	}
	
	public boolean isHidden()
	{
		String name = getName();
		return name.charAt(0) == '.';
				
	}
	
	public String getParentPath()
	{
		return _parentPath;
	}
	
	public boolean isDirectory()
	{
		return _isDirectory;
	}
	
	public boolean isFile()
	{
		return !(_isDirectory || _isRoot);
	}
	
	public boolean isRoot()
	{
		return _isRoot;
	}
	
	public boolean exists()
	{
		return true;
	}

	public String getAbsolutePath()
	{
		
		StringBuffer path = new StringBuffer(getParentPath());
		if (!_parentPath.endsWith("/"))
		{
			path.append('/');
		}
		path.append(getName());
		
		
		return path.toString();
	}

	public long getSize()
	{
		return _size;
	}

	public long getModifiedDate()
	{
		return _lastModified;
	}

	public void renameTo(String newAbsolutePath) 
	{
		int i = newAbsolutePath.lastIndexOf("/");
		if (i == -1)
		{
			_name = newAbsolutePath;
		}
		else
		{
			_parentPath = newAbsolutePath.substring(0, i);
			_name = newAbsolutePath.substring(i+1);
		}
		
		_isArchive = internalIsArchive();

		
	}

	protected boolean internalIsArchive()
	{
		return ArchiveHandlerManager.getInstance().isArchive(new File(getAbsolutePath())) 
		&& !ArchiveHandlerManager.isVirtual(getAbsolutePath());
	}
	
	public boolean isArchive() 
	{
		return _isArchive;
	}
	
}