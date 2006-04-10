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
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;

public class LocalVirtualHostFile extends LocalHostFile 
{
	protected File _parentArchive;
	protected VirtualChild _child;
	
	public LocalVirtualHostFile(VirtualChild child)
	{
		super(child.getContainingArchive());
		_child = child;
		_parentArchive = _child.getContainingArchive();
	}
	
	public String getName() 
	{
		return _child.name;
	}

	public String getParentPath() 
	{
		return _parentArchive.getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + _child.path;
	}

	public boolean isDirectory()
	{
		return _child.isDirectory;
	}

	public boolean isRoot() 
	{
		return false;
	}

	public boolean isFile() 
	{
		return !_child.isDirectory;
	}
	
	public File getFile()
	{
		return _parentArchive;
	}

	public boolean exists()
	{
		return _child.exists();
	}
	
	public String getAbsolutePath()
	{
		return _child.getContainingArchive().getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + _child.fullName;
	}
	
	public VirtualChild getChild()
	{
		return _child;
	}
	
	public boolean isHidden()
	{
		return false;
	}
	
	public boolean isArchive()
	{
		return false;
	}

}