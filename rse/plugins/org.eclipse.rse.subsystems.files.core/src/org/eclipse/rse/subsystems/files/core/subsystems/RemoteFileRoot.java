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
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 *******************************************************************************/

package org.eclipse.rse.subsystems.files.core.subsystems;

import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;

/**
 * A root node used to drive a CheckboxTreeAndListGroup, or any viewer which
 * takes a root which should return a single IRemoteFile object in the initial
 * getChildren query.
 */
public class RemoteFileRoot extends RemoteFile
{
	private IRemoteFile rootFile;
	private IRemoteFile[] rootFiles;

	/**
	 * Constructor when root is known
	 */
	public RemoteFileRoot(IRemoteFile rootFile)
	{
		super(new RemoteFileContext(null,null,null));
		setRootFile(rootFile);
	}

	/**
	 * Constructor when root is not known.
	 * Client must call {@link #setRootFile(IRemoteFile)} before any
	 * get.. calls in this class are actually used.
	 */
	public RemoteFileRoot()
	{
		super(new RemoteFileContext(null,null,null));
	}

    /**
     * Return the root file node
     */
    public IRemoteFile getRootFile()
    {
    	return rootFile;
    }

    /**
     * Reset the root file node
     */
    public void setRootFile(IRemoteFile rootFile)
    {
    	this.rootFile = rootFile;
    	rootFiles = new IRemoteFile[1];
    	rootFiles[0] = rootFile;
    }

    /**
     * Return the root file node as an array of 1
     */
    public IRemoteFile[] getRootFiles()
    {
    	return rootFiles;
    }

	public String getName()
	{
		return "dummy"; //$NON-NLS-1$
	}

	public int compareTo(Object o)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	public boolean isVirtual()
	{
		// TODO Auto-generated method stub
		return false;
	}
	public boolean showBriefPropertySet()
	{
		// TODO Auto-generated method stub
		return false;
	}
	public String getParentPath()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String getParentNoRoot()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String getRoot()
	{
		return rootFile.getAbsolutePath();
	}
	public String getParentName()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isRoot()
	{
		return true;
	}
	public boolean isDirectory()
	{
		return true;
	}
	public boolean isFile()
	{
		return false;
	}

	public boolean isHidden()
	{
		return false;
	}

	public boolean canRead()
	{
		return rootFile.canRead();
	}

	public boolean canWrite()
	{
		return rootFile.canWrite();
	}

	public boolean exists()
	{
		return rootFile.exists();
	}

	public long getLastModified()
	{
		return rootFile.getLastModified();
	}

	public long getLength()
	{
		return rootFile.getLength();
	}

	public boolean showReadOnlyProperty()
	{
		return rootFile.showReadOnlyProperty();
	}

	public String getClassification()
	{
		return rootFile.getClassification();
	}

	/**
	 * @since 3.0
	 */
	public String getAbsolutePath()
	{
		return rootFile.getAbsolutePath();
	}

	public String getCanonicalPath()
	{
		return rootFile.getCanonicalPath();
	}

	public IHostFile getHostFile()
	{
		return rootFile.getHostFile();
	}

	/**
	 * Override this to provide permissions
	 */
	public IHostFilePermissions getPermissions() {
		return rootFile.getPermissions();
	}
}
