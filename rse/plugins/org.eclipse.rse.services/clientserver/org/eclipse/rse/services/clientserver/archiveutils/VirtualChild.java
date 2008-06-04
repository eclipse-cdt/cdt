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
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancellation of archive operations
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.File;
import java.io.IOException;

import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.ISystemOperationMonitor;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationFailedException;
import org.eclipse.rse.services.clientserver.messages.SystemUnexpectedErrorException;


/**
 * A simple structure for passing information about virtual files and folders.
 *
 * @author mjberger
 */
public final class VirtualChild {

	public String fullName;
	public String name;
	public String path;
	public boolean isDirectory;
	protected ISystemArchiveHandler _handler;
	protected File _extractedFile;
	protected File _containingArchive;

	private String comment;
	private long compressedSize;
	private String compressionMethod;
	private long size;
	private long timeStamp;

	/**
	 * Constructs a new VirtualChild given a reference to its parent archive's
	 * handler, but does not populate any fields in the child. Clients must
	 * populate the fullName, name, path, and isDirectory fields.
	 */
	public VirtualChild(ISystemArchiveHandler handler)
	{
		fullName = ""; //$NON-NLS-1$
		name = ""; //$NON-NLS-1$
		path = ""; //$NON-NLS-1$
		isDirectory = false;
		_handler = handler;
		_extractedFile = null;
		_containingArchive = null;

		comment = "";  //$NON-NLS-1$
		compressedSize = -1;
		compressionMethod = "";  //$NON-NLS-1$
		size = -1;
		timeStamp = -1;
	}

	/**
	 * Constructs a new VirtualChild given a reference to its parent archive's
	 * handler (<code>handler</code>), and immediately populates the name and path info
	 * for the VirtualChild given its <code>fullVirtualName</code>. Clients
	 * must still populate the isDirectory field.
	 */
	public VirtualChild(ISystemArchiveHandler handler, String fullVirtualName)
	{
		this(handler);
		renameTo(fullVirtualName);
	}

	/**
	 * Constructs a new VirtualChild given the name of its parent archive,
	 * and immediately populates the name and path info
	 * for the VirtualChild given its <code>fullVirtualName</code>. Clients
	 * must still populate the isDirectory field.
	 * NOTE: This constructor is intended only to be used for creating NON-EXISTENT
	 * virtual children.
	 */
	public VirtualChild(String fullVirtualName, File containingArchive)
	{
		this(null);
		renameTo(fullVirtualName);
		_containingArchive = containingArchive;
	}

	/**
	 * @return This VirtualChild's parent archive's Handler.
	 */
	public ISystemArchiveHandler getHandler()
	{
		return _handler;
	}

	/**
	 * @return This VirtualChild's time stamp (retrieves the latest one
	 * from the archive).
	 */
	public long getTimeStamp()
	{
		/*
		if (_handler == null) return 0;
		return _handler.getTimeStampFor(fullName);
		*/
		return timeStamp;
	}

	/**
	 * @param value the time stamp value
	 * @since 3.0
	 */
	public void setTimeStamp(long value)
	{
		timeStamp = value;
	}

	/**
	 * @return This VirtualChild's uncompressed size (retrieves the latest one
	 * from the archive).
	 */
	public long getSize()
	{
		/*
		if (_handler == null) return 0;
		return _handler.getSizeFor(fullName);
		*/
		return size;
	}

	/**
	 * @param value the size value
	 * @since 3.0
	 */
	public void setSize(long value)
	{
		size = value;
	}

	/**
	 * @return The comment associated with this VirtualChild.
	 */
	public String getComment()
	{
		/*
		if (_handler == null) return ""; //$NON-NLS-1$
		return _handler.getCommentFor(fullName);
		*/
		return comment;
	}

	/**
	 * @param value the comment value
	 * @since 3.0
	 */
	public void setComment(String value)
	{
		if (null != value)
		{
			comment = value;
		}
		else
		{
			comment = "";  //$NON-NLS-1$
		}
	}

	/**
	 * @return The amount of space this VirtualChild takes up in the archive
	 * in compressed form.
	 */
	public long getCompressedSize()
	{
		/*
		if (_handler == null) return 0;
		return _handler.getCompressedSizeFor(fullName);
		*/
		return compressedSize;
	}

	/**
	 * @param value the compressedSize value
	 * @since 3.0
	 */
	public void setCompressedSize(long value)
	{
		compressedSize = value;
	}

	/**
	 * @return The method used to compress this VirtualChild.
	 */
	public String getCompressionMethod()
	{
		/*
		if (_handler == null) return ""; //$NON-NLS-1$
		return _handler.getCompressionMethodFor(fullName);
		*/
		return compressionMethod;
	}

	/**
	 * @param value the compression method value
	 * @since 3.0
	 */
	public void setCompressionMethod(String value)
	{
		if (null != value)
		{
			compressionMethod = value;
		}
		else
		{
			compressionMethod = "";  //$NON-NLS-1$
		}
	}

	/**
	 * @return The actual minus compressed size of this VirtualChild, divided
	 * by the actual size.
	 */
	public double getCompressionRatio()
	{
		/*
		if (getSize() == 0)
		{
			return 1;
		}
		else return  ((double)getSize() - (double)getCompressedSize()) / getSize();
		*/
		if (size <= 0)
		{
			return 1;
		}
		if (compressedSize <= 0)
		{
			return 1;
		}

		return  ((double)size - (double)compressedSize) / size;
	}

	/**
	 * @return The extracted file or directory represented by this VirtualChild from the archive.
	 * Note that the extracted file is cached after it is extracted once, but if the
	 * timestamps on the cached and archived files do not match, the cached file is erased,
	 * and reextracted from the archive.
	 */
	public File getExtractedFile() throws SystemMessageException
	{
		return getExtractedFile(SystemEncodingUtil.ENCODING_UTF_8, false, null);
	}

	/**
	 * @return The extracted file or directory represented by this VirtualChild
	 *         from the archive. Assumes that the file has been encoded in the
	 *         encoding specified. Note that the extracted file is cached after
	 *         it is extracted once, but if the timestamps on the cached and
	 *         archived files do not match, the cached file is erased, and
	 *         re-extracted from the archive.
	 * @since 3.0 throws SystemMessageException
	 */
	public File getExtractedFile(String sourceEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException
	{
		File returnedFile = null;
		if (_extractedFile == null || _extractedFile.lastModified() != getTimeStamp())
		{
			try
			{
				int i = name.lastIndexOf("."); //$NON-NLS-1$
				String ext = ""; //$NON-NLS-1$
				if (i != -1) ext = name.substring(i+1);
				if (i < 3)
				{
				    _extractedFile = File.createTempFile(name + "123", "virtual." + ext); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else
				{
				    _extractedFile = File.createTempFile(name, "virtual." + ext); //$NON-NLS-1$
				}
				_extractedFile.deleteOnExit();
				if (_handler == null) return _extractedFile;
				if (isDirectory)
				{
					if (!_extractedFile.isDirectory())
					{
						if (!(_extractedFile.delete() && _extractedFile.mkdirs()))
						{
							System.out.println("VirtualChild.getExtractedFile(): Could not create temp dir."); //$NON-NLS-1$
							//We only set the status of the archive operation montor to done if it is not been cancelled.
							if (null != archiveOperationMonitor && !archiveOperationMonitor.isCancelled())
							{
								archiveOperationMonitor.setDone(true);
							}
							return null;
						}
					}
					_handler.extractVirtualDirectory(fullName, _extractedFile, sourceEncoding, isText, archiveOperationMonitor);
				}
				else
				{
					_handler.extractVirtualFile(fullName, _extractedFile, sourceEncoding, isText, archiveOperationMonitor);
				}
			}
			catch (IOException e)
			{
				throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "VirtualChild.getExtractedFile()", e); //$NON-NLS-1$
			}
		}

		if (isDirectory)
		{
			returnedFile = new File(_extractedFile, name);
		}
		else
		{
			returnedFile = _extractedFile;
		}

		//We only set the status of the archive operation montor to done if it is not been cancelled.
		if (null != archiveOperationMonitor && !archiveOperationMonitor.isCancelled())
		{
			archiveOperationMonitor.setDone(true);
		}

		return returnedFile;
	}

	/**
	 * Gets the extracted file or directory represented by this VirtualChild
	 * from the archive, and replaces the object referred to by
	 * <code>destination</code> with that extracted file or directory. Note that
	 * the extracted file is cached after it is extracted once, but if the
	 * timestamps on the cached and archived files do not match, the cached file
	 * is erased, and re-extracted from the archive. <code>destination</code> is
	 * always overwritten with either what is cached, or what is in the archive.
	 *
	 * @throws SystemMessageException in case of an error
	 * @since 3.0 throws SystemMessageException
	 */
	public void getExtractedFile(File destination, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException
	{
		getExtractedFile(destination, SystemEncodingUtil.ENCODING_UTF_8, false, archiveOperationMonitor);
	}


	/**
	 * Gets the extracted file or directory represented by this VirtualChild
	 * from the archive, and replaces the object referred to by
	 * <code>destination</code> with that extracted file or directory. Note that
	 * the extracted file is cached after it is extracted once, but if the
	 * timestamps on the cached and archived files do not match, the cached file
	 * is erased, and reextracted from the archive. <code>destination</code> is
	 * always overwritten with either what is cached, or what is in the archive.
	 *
	 * @throws SystemMessageException in case of an error
	 * @since 3.0 throws SystemMessageException
	 */
	public void getExtractedFile(File destination, String sourceEncoding, boolean isText, ISystemOperationMonitor archiveOperationMonitor)
			throws SystemMessageException
	{
		if (_handler == null)
			throw new SystemUnexpectedErrorException(IClientServerConstants.PLUGIN_ID);
		if (_extractedFile == null ||
		    _extractedFile.lastModified() != getTimeStamp() ||
		    !destination.getAbsolutePath().equals(_extractedFile.getAbsolutePath())
		    )
		{
			if (isDirectory)
			{
				_handler.extractVirtualDirectory(fullName, destination.getParentFile(), destination, sourceEncoding, isText, archiveOperationMonitor);
			}
			else
			{
				_handler.extractVirtualFile(fullName, destination, sourceEncoding, isText, archiveOperationMonitor);
			}
			_extractedFile = destination;
		}
		//We only set the status of the archive operation monitor to done if it is not been cancelled.
		if (null != archiveOperationMonitor && !archiveOperationMonitor.isCancelled())
		{
			archiveOperationMonitor.setDone(true);
		}
	}

	/**
	 * @return Whether or not this VirtualChild exists in the archive.
	 */
	public boolean exists() throws SystemMessageException
	{
		if (_handler == null) return false;
		return _handler.exists(fullName, null);
	}

	/**
	 * Renames this virtual child to newName. WARNING!!
	 * This method does not change the underlying zip file,
	 * you must rename the entry in the zip file for subsequent
	 * calls to any of the getters to work.
	 */
	public void renameTo(String newName)
	{
		newName = ArchiveHandlerManager.cleanUpVirtualPath(newName);
		fullName = newName;
		int i = newName.lastIndexOf("/"); //$NON-NLS-1$
		if (i == -1)
		{
			name = newName;
			path = ""; //$NON-NLS-1$
		}
		else
		{
			name = newName.substring(i+1);
			path = newName.substring(0, i);
		}
		// force reextraction of temp file
		_extractedFile = null;

	}

	/**
	 * @return The "standard" name for this VirtualChild, based on
	 * the handler type.
	 */
	public String getArchiveStandardName()
	{
		if (_handler == null) return fullName;
		return _handler.getStandardName(this);
	}

	public File getContainingArchive()
	{
		if (_handler == null) return _containingArchive;
		return _handler.getArchive();
	}


}
