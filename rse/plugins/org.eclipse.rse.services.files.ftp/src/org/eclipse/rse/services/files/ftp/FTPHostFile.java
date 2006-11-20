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
 * Michael Berger (IBM) - Fixing 140408 - FTP upload does not work
 * Javier Montalvo Orús (Symbian) - Migrate to jakarta commons net FTP client
 * Javier Montalvo Orus (Symbian) - Fixing 161211 - Cannot expand /pub folder as 
 *    anonymous on ftp.wacom.com
 * Javier Montalvo Orus (Symbian) - Fixing 161238 - [ftp] connections to VMS servers are not usable   
 ********************************************************************************/

package org.eclipse.rse.services.files.ftp;


import java.io.File;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;

public class FTPHostFile implements IHostFile
{
	
	private String _name;
	private String _parentPath;
	private boolean _isDirectory;
	private boolean _isArchive;
	private long _lastModified;
	private long _size;
	private boolean _canRead = true;
	private boolean _canWrite = true;
	private boolean _isRoot;
	private boolean _exists;
	private String _systemName;
	
	public FTPHostFile(String parentPath, String name, boolean isDirectory, boolean isRoot, long lastModified, long size, boolean exists)
	{
		_parentPath = parentPath;
		_name = name;
		_isDirectory = isDirectory;
		_lastModified = lastModified;
		_size = size;
		_isArchive = internalIsArchive();
		_canRead = true;
		_canWrite = false;
		_isRoot = isRoot;
		_exists = exists;
	}
	
	public FTPHostFile(String parentPath, FTPFile ftpFile, String systemName)
	{
		_systemName = systemName;
		_parentPath = parentPath;
		
		if(systemName.equals(FTPClientConfig.SYST_VMS))
		{
			_name = ftpFile.getName();
			if(_name.indexOf(".DIR")!=-1) //$NON-NLS-1$
			{
				_name = _name.substring(0,_name.indexOf(".DIR")); //$NON-NLS-1$
			}
			else
			{
				_name = _name.substring(0,_name.indexOf(";")); //$NON-NLS-1$
			}
		}
		else
		{
			_name = ftpFile.getName();
		}
		
		_isDirectory = ftpFile.isDirectory();
		_lastModified = ftpFile.getTimestamp().getTimeInMillis();
		_size = ftpFile.getSize();
		_isArchive = internalIsArchive();
		
		//In Windows r/w is not listed
		//In VMS it is not implemented in the Jakarta parser
		if(!systemName.equals(FTPClientConfig.SYST_NT) && !systemName.equals(FTPClientConfig.SYST_VMS)) 
		{
			_canRead = ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION);
			_canWrite = ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION);
		}
		
		_isRoot = false;
		_exists = true;
	}
	
	public long getSize()
	{
		return _size;
	}
	
	public boolean isDirectory()
	{
		return _isDirectory;
	}
	
	public boolean isFile()
	{
		return !(_isDirectory || _isRoot);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean canRead() {
		return _canRead;
	}

	public boolean canWrite() {
		return _canWrite;
	}

	public boolean exists() {
		return _exists;
	}

	public String getAbsolutePath()
	{
		if (isRoot() || _parentPath==null) {
			return getName();
		} else {
			StringBuffer path = new StringBuffer(getParentPath());
			if (!_parentPath.endsWith("/") && !_parentPath.endsWith("\\"))//$NON-NLS-1$ //$NON-NLS-2$
			{
				path.append('/');
			}
			path.append(getName());
			return path.toString();
		}
		
	}

	public long getModifiedDate()
	{
		return _lastModified;
	}

	public String geParentPath() {
		return _parentPath;
	}

	public boolean isArchive() {
		return _isArchive;
	}

	public boolean isHidden() {
		return false;
	}

	public boolean isRoot() {
		return _isRoot;
		
	}

	public String getParentPath() {
		return _parentPath;
	}

	public void renameTo(String newAbsolutePath) 
	{
		int i = newAbsolutePath.lastIndexOf("/"); //$NON-NLS-1$
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
	
}