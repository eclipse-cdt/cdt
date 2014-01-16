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
 * David McKnight   (IBM)        - [420798] Slow performances in RDz 9.0 with opening 7000 files located on a network driver.
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
	
	// cache
	private long _lastQueryTime = 0;
	private Boolean _isFile = null;
	private Boolean _isDirectory = null;
	private Boolean _exists = null;
	private Boolean _isHidden = null;
	
	private boolean _isRoot = false;
	private boolean _isArchive = false;
	private IHostFilePermissions _permissions = null;
	
	public LocalHostFile(File file)
	{
		_file = file;
		_isArchive = ArchiveHandlerManager.getInstance().isArchive(_file);
	}
	
	public LocalHostFile(File file, boolean isRoot, boolean isFile)
	{
		_file = file;
		_isRoot = isRoot;
		if (!isRoot){
			_isArchive = ArchiveHandlerManager.getInstance().isArchive(_file);
			_isFile = new Boolean(isFile);
			_isDirectory = new Boolean(!isFile);
		}
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
		if (_isHidden == null || needsQuery()){
			String name = getName();
			_isHidden = new Boolean(name.charAt(0) == '.' || (!_isRoot && _file.isHidden()));				
		}
		return _isHidden.booleanValue();
	}

	public String getParentPath() 
	{
		return _file.getParent();
	}

	public boolean isDirectory() 
	{
		if (_isDirectory == null){
			_isDirectory = new Boolean(_file.isDirectory());
		}
		return _isDirectory.booleanValue();
	}

	public boolean isRoot() 
	{
		return _isRoot;
	}

	public boolean isFile() 
	{
		if (_isFile == null){
			_isFile = new Boolean(_file.isFile());
		}
		return _isFile.booleanValue();
	}
	
	public File getFile()
	{
		return _file;
	}

	public boolean exists()
	{
		if (_exists == null || needsQuery()){
			_exists = new Boolean(_file.exists());
		}
		return _exists.booleanValue();
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

	private boolean needsQuery(){
		long t = System.currentTimeMillis();
		if (_lastQueryTime == 0 || (t - _lastQueryTime) > 5000){
			_lastQueryTime = t;
			return true;
		}
		return false;
	}
}
