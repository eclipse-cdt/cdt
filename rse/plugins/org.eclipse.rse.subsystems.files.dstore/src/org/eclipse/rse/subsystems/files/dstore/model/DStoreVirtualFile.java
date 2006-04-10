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

package org.eclipse.rse.subsystems.files.dstore.model;

import org.eclipse.rse.services.dstore.files.DStoreVirtualHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;

public class DStoreVirtualFile extends DStoreFile implements IVirtualRemoteFile
{

	protected DStoreVirtualHostFile _node;
	
	public DStoreVirtualFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, DStoreVirtualHostFile hostFile)
	{
		super(ss, context, parent, hostFile);
		_node = hostFile;
	}

	public String getVirtualName()
	{
		return _node.getName();
	}

	public boolean isVirtualFolder()
	{
		return _node.isDirectory();
	}

	public boolean isVirtualFile()
	{
		return _node.isFile();
	}

	public long getCompressedSize()
	{
		return _node.getCompressedSize();
	}

	public String getCompressionMethod()
	{
		return _node.getCompressionMethod();
	}

	public double getCompressionRatio()
	{
		return _node.getCompressionRatio();
	}

	public long getExpandedSize()
	{
		return _node.getExpandedSize();
	}
	
	public String getComment()
	{
		return _node.getComment();
	}

}