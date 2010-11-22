/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Fix [168591] LocalHostFile missing equals()
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * David McKnight   (IBM)        - [294521] Local "hidden" files and folders are always shown
 *******************************************************************************/

package org.eclipse.rse.internal.services.local.files;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;


public class LocalHostFile implements IHostFile, IHostFilePermissionsContainer
{
	private File _file;
	private boolean _isRoot = false;
	private boolean _isArchive = false;
	private IHostFilePermissions _permissions = null;
	
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof LocalHostFile) {
			LocalHostFile other = (LocalHostFile)obj;
			return _file.equals(other._file)
				&& _isRoot == other._isRoot
				&& _isArchive == other._isArchive;
		}
		return false;
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
		return name.charAt(0) == '.' || _file.isHidden();
				
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

	public boolean canRead() {
		return _file.canRead();
	}

	public boolean canWrite() {
		return _file.canWrite();
	}

	public IHostFilePermissions getPermissions() {
		return _permissions;
	}

	public void setPermissions(IHostFilePermissions permissions) {
		_permissions = permissions;
	}

}
