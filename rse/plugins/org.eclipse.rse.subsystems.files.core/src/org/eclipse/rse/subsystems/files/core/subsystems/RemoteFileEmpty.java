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
 * A "dummy" node to use as a place holder
 */
public class RemoteFileEmpty extends RemoteFile
{
	/**
	 * Constructor for RemoteFileEmpty
	 */
	public RemoteFileEmpty()
	{
		super(new RemoteFileContext(null,null,null));
	}

	/**
	 * @since 3.0
	 */
	public String getAbsolutePath()
	{
		return ""; //$NON-NLS-1$
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
		// TODO Auto-generated method stub
		return null;
	}



	public String getParentName()
	{
		// TODO Auto-generated method stub
		return null;
	}



	public boolean isRoot()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean isDirectory()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean isFile()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean isHidden()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean canRead()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean canWrite()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public boolean exists()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public long getLastModified()
	{
		// TODO Auto-generated method stub
		return 0;
	}



	public long getLength()
	{
		// TODO Auto-generated method stub
		return 0;
	}



	public boolean showReadOnlyProperty()
	{
		// TODO Auto-generated method stub
		return false;
	}



	public String getClassification()
	{
		// TODO Auto-generated method stub
		return null;
	}



	public String getCanonicalPath()
	{
		// TODO Auto-generated method stub
		return null;
	}



	public IHostFile getHostFile()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Override this to provide permissions
	 */
	public IHostFilePermissions getPermissions() {
		return null;
	}


}
