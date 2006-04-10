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

package org.eclipse.rse.services.local.files;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;


public class LocalHostFile implements IHostFile 
{
	private File _file;
	private boolean _isRoot = false;
	private boolean _isArchive = false;
	
	public LocalHostFile(File file)
	{
		_file = file;
		_isArchive = ArchiveHandlerManager.getInstance().isArchive(_file);
	}
	
	public LocalHostFile(File file, boolean isRoot)
	{
		_file = file;
		_isRoot = isRoot;
		_isArchive = ArchiveHandlerManager.getInstance().isArchive(_file);

	}
	
	public String getName() 
	{
		if (_isRoot)
		{
			return _file.getPath();
		}
		else
		{
			return _file.getName();
		}
	}
	
	public boolean isHidden()
	{
		String name = getName();
		return name.charAt(0) == '.';
				
	}

	public String getParentPath() 
	{
		return _file.getParent();
	}

	public boolean isDirectory() 
	{
		return _file.isDirectory();
	}

	public boolean isRoot() 
	{
		return _isRoot;
	}

	public boolean isFile() 
	{
		return _file.isFile();
	}
	
	public File getFile()
	{
		return _file;
	}

	public boolean exists()
	{
		return _file.exists();
	}

	public String getAbsolutePath()
	{
		return _file.getAbsolutePath();
	}

	public long getSize()
	{
		return _file.length();
	}

	public long getModifiedDate()
	{
		return _file.lastModified();
	}

	public void renameTo(String newAbsolutePath) 
	{
		_file = new File(newAbsolutePath);
		_isArchive = ArchiveHandlerManager.getInstance().isArchive(_file);
	}

	public boolean isArchive() 
	{
		return _isArchive;
	}

}