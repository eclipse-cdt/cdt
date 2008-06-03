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
 * David McKnight   (IBM)        - [231209] [api][breaking] IRemoteFile.getSystemConnection() should be changed to IRemoteFile.getHost()
 * Martin Oberhuber (Wind River) - [234726] Update IRemoteFile Javadocs
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.servicesubsystem;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;


public abstract class AbstractRemoteFile extends RemoteFile
{
	protected IHostFile _hostFile;
	protected FileServiceSubSystem _subSystem;
	protected String _classiciation;


	public AbstractRemoteFile(FileServiceSubSystem subSystem, IRemoteFileContext context, IRemoteFile parent, IHostFile hostFile)
	{
		super(context);
		_subSystem = subSystem;
		_hostFile = hostFile;
		setParentRemoteFile(parent);
		if (_hostFile.isFile() && !_hostFile.isArchive()) // no need to query this again so marking false for stale
    		markStale(false, false);
	}

	public IRemoteFileSubSystem getParentRemoteFileSubSystem()
	{
		return _subSystem;
	}

	public IHost getHost()
	{
		return _subSystem.getHost();
	}

	public String getAbsolutePath()
	{
		return _hostFile.getAbsolutePath();
	}


	public String getLabel()
	{
		return _hostFile.getName();
	}

	public String getName()
	{
		return _hostFile.getName();
	}

	public String getParentPath()
	{
		return _hostFile.getParentPath();
	}

	public boolean isRoot()
	{
		return _hostFile.isRoot();
	}

	public boolean isDirectory()
	{
		return _hostFile.isDirectory();
	}

	public boolean isFile()
	{
		return _hostFile.isFile();
	}


	public boolean exists()
	{
		return _hostFile.exists();
	}

	public long getLastModified()
	{
		return _hostFile.getModifiedDate();
	}


	public long getLength()
	{
		return _hostFile.getSize();
	}

	public int compareTo(Object other) throws ClassCastException
	{
		IRemoteFile otherFile = (IRemoteFile)other;
		if (otherFile.isFile())
		{
			if (isFile())
			{
				String otherPath = otherFile.getAbsolutePath();
				String thisPath = getAbsolutePath();

				return thisPath.compareToIgnoreCase(otherPath);
			}
			else
			{
				return -1;
			}
		}
		else // not file
		{
			if (isDirectory())
			{
				String otherPath = otherFile.getAbsolutePath();
				String thisPath = getAbsolutePath();

				return thisPath.compareToIgnoreCase(otherPath);
			}
			else
			{
				return 1;
			}
		}
	}

	public boolean showBriefPropertySet()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @deprecated - shouldn't need apis like this
	 */
	public String getParentNoRoot()
	{
		String parentPath = getParentPath();
		return parentPath;
	}

	/**
	 * @deprecated - shouldn't need apis like this
	 */
	public String getRoot()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getParentName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isHidden()
	{
		return _hostFile.isHidden();
	}

	public boolean isVirtual()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canRead()
	{
		return _hostFile.canRead();
	}

	public boolean canWrite()
	{
		return _hostFile.canWrite();
	}

	public boolean showReadOnlyProperty()
	{
		return true;
	}

	public IHostFile getHostFile()
	{
		return _hostFile;
	}


}
