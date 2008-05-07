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
 * Kevin Doyle (IBM) - [189828] renameTo() now passes proper name to _child.renameTo()
 * Xuan Chen   (IBM) - [214251] [archive] "Last Modified Time" changed for all virtual files/folders if rename/paste/delete of one virtual file.
 *******************************************************************************/

package org.eclipse.rse.internal.services.local.files;

import java.io.File;

import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

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
		try {
			return _child.exists();
		} catch (SystemMessageException e) {
			return false;
		}
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

	/**
	 * @see org.eclipse.rse.internal.services.local.files.LocalHostFile#renameTo(java.lang.String)
	 */
	public void renameTo(String newAbsolutePath) {
		newAbsolutePath = ArchiveHandlerManager.cleanUpVirtualPath(newAbsolutePath);
		String newName = newAbsolutePath;
		int i = newAbsolutePath.indexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		if (i != -1)
			newName = newAbsolutePath.substring(i + ArchiveHandlerManager.VIRTUAL_SEPARATOR.length());
		_child.renameTo(newName);
	}

	public long getModifiedDate()
	{
		if (null != _child)
		{
			return _child.getTimeStamp();
		}
		else
		{
			return super.getModifiedDate();
		}
	}
}
