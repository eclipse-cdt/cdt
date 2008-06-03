/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 * Xuan Chen (IBM)     - [200872] [Archives] Size Property displays size of Archive not of file inside Archive
 * Xuan Chen (IBM)     - [160775] [api] rename (at least within a zip) blocks UI thread
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.local.model;

import java.io.File;

import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.internal.services.local.files.LocalVirtualHostFile;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;

public class LocalVirtualFile extends LocalFile implements IVirtualRemoteFile
{

	protected File _parentArchive;
	protected LocalVirtualHostFile _node;
	protected String _absolutePath = null;

	public LocalVirtualFile(FileServiceSubSystem ss, IRemoteFileContext context, LocalVirtualHostFile node)
	{
		super(ss, context, context.getParentRemoteFile(), node);
		_node = node;
		_parentArchive = _node.getChild().getContainingArchive();
	}


	public String getAbsolutePath()
	{
		return _parentArchive.getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + getVirtualFullName();
	}

	public File getParentArchive()
	{
		return _parentArchive;
	}

	public VirtualChild getVirtualChild()
	{
		return _node.getChild();
	}

	public boolean isVirtual()
	{
		return true;
	}

	public String getVirtualFullName()
	{
		return _node.getChild().fullName;
	}

	public String getVirtualFullPath()
	{
		return _node.getChild().path;
	}

	public String getVirtualName()
	{
		return _node.getChild().name;
	}

	public void setVirtualFullName(String string)
	{
		_node.getChild().renameTo(string);
	}

	public long getLength()
	{
		return _node.getChild().getSize();
	}

	public void setVirtualFullPath(String string)
	{
		if (string.equals("")) //$NON-NLS-1$
		{
			_node.getChild().renameTo(_node.getChild().name);
		}
		else
		{
			_node.getChild().renameTo(string + "/" + _node.getChild().name); //$NON-NLS-1$
		}
	}

	public void setVirtualName(String string)
	{
		if (_node.getChild().path.equals("")) //$NON-NLS-1$
		{
			_node.getChild().renameTo(string);
		}
		else
		{
			_node.getChild().renameTo(_node.getChild().path + "/" + string); //$NON-NLS-1$
		}
	}

	public File getFileWrapper()
	{
		return _node.getFile();
	}

	public boolean canRead()
	{
		return _parentArchive.canRead();
	}

	public boolean canWrite()
	{
		return _parentArchive.canWrite();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile#getComment()
	 */
	public String getComment()
	{
		return _node.getChild().getComment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile#getCompressedSize()
	 */
	public long getCompressedSize()
	{
		return _node.getChild().getCompressedSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile#getCompressionMethod()
	 */
	public String getCompressionMethod()
	{
		return _node.getChild().getCompressionMethod();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile#getCompressionRatio()
	 */
	public double getCompressionRatio()
	{
		return _node.getChild().getCompressionRatio();
	}

	public Object getFile(String srcEncoding, boolean isText)
	{
		try {
			return _node.getChild().getExtractedFile(srcEncoding, isText, null);
		} catch (SystemMessageException e) {
			//FIXME check if this is OK, or should we better return null?
			return new SystemMessageObject(e.getSystemMessage(), ISystemMessageObject.MSGTYPE_ERROR, null);
		}
	}

	public String getContainingArchiveFullName()
	{
		return _node.getChild().getContainingArchive().getAbsolutePath();
	}


	public long getExpandedSize()
	{
		return _node.getSize();
	}


	public boolean isVirtualFile()
	{
		return !_node.getChild().isDirectory;
	}


	public boolean isVirtualFolder()
	{
		return _node.getChild().isDirectory;
	}
}
