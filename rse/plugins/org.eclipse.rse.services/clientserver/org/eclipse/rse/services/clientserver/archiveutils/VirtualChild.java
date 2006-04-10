/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.File;
import java.io.IOException;

import org.eclipse.rse.services.clientserver.SystemEncodingUtil;


/**
 * @author mjberger
 * A simple structure for passing information about virtual files and folders.
 */
public final class VirtualChild {

	public String fullName;
	public String name;
	public String path;
	public boolean isDirectory;
	protected ISystemArchiveHandler _handler;
	protected File _extractedFile;
	protected File _containingArchive;

	/**
	 * Constructs a new VirtualChild given a reference to its parent archive's
	 * handler, but does not populate any fields in the child. Clients must
	 * populate the fullName, name, path, and isDirectory fields.
	 */	
	public VirtualChild(ISystemArchiveHandler handler) 
	{
		fullName = "";
		name = "";
		path = "";
		isDirectory = false;
		_handler = handler;
		_extractedFile = null;
		_containingArchive = null;
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
		if (_handler == null) return 0;
		return _handler.getTimeStampFor(fullName);
	}
	
	/**
	 * @return This VirtualChild's uncompressed size (retrieves the latest one
	 * from the archive).
	 */
	public long getSize()
	{
		if (_handler == null) return 0;
		return _handler.getSizeFor(fullName);
	}
	
	/**
	 * @return The comment associated with this VirtualChild.
	 */
	public String getComment() 
	{
		if (_handler == null) return "";
		return _handler.getCommentFor(fullName);
	}

	/**
	 * @return The amount of space this VirtualChild takes up in the archive
	 * in compressed form.
	 */
	public long getCompressedSize() 
	{
		if (_handler == null) return 0;
		return _handler.getCompressedSizeFor(fullName);
	}

	/**
	 * @return The method used to compress this VirtualChild.
	 */
	public String getCompressionMethod() 
	{
		if (_handler == null) return "";
		return _handler.getCompressionMethodFor(fullName);
	}

	/**
	 * @return The actual minus compressed size of this VirtualChild, divided
	 * by the actual size.
	 */
	public double getCompressionRatio() 
	{
		if (getSize() == 0)
		{
			return 1;
		}
		else return  ((double)getSize() - (double)getCompressedSize()) / getSize();
	}
	
	/**
	 * @return The extracted file or directory represented by this VirtualChild from the archive.
	 * Note that the extracted file is cached after it is extracted once, but if the 
	 * timestamps on the cached and archived files do not match, the cached file is erased,
	 * and reextracted from the archive.
	 */
	public File getExtractedFile()
	{
		return getExtractedFile(SystemEncodingUtil.ENCODING_UTF_8, false);
	}
	
	/**
	 * @return The extracted file or directory represented by this VirtualChild from the archive.
	 * Assumes that the file has been encoded in the encoding specified.
	 * Note that the extracted file is cached after it is extracted once, but if the 
	 * timestamps on the cached and archived files do not match, the cached file is erased,
	 * and reextracted from the archive.
	 */
	public File getExtractedFile(String sourceEncoding, boolean isText)
	{
		if (_extractedFile == null || _extractedFile.lastModified() != getTimeStamp())
		{
			try
			{
				int i = name.lastIndexOf(".");
				String ext = "";
				if (i != -1) ext = name.substring(i+1);
				if (i < 3)
				{
				    _extractedFile = File.createTempFile(name + "123", "virtual." + ext);
				}
				else
				{
				    _extractedFile = File.createTempFile(name, "virtual." + ext);
				}
				_extractedFile.deleteOnExit();
				if (_handler == null) return _extractedFile;
				if (isDirectory)
				{
					if (!_extractedFile.isDirectory())
					{
						if (!(_extractedFile.delete() && _extractedFile.mkdirs()))
						{
							System.out.println("VirtualChild.getExtractedFile(): Could not create temp dir.");
							return null;
						}
					}
					_handler.extractVirtualDirectory(fullName, _extractedFile, sourceEncoding, isText);
				}
				else
				{
					_handler.extractVirtualFile(fullName, _extractedFile, sourceEncoding, isText);
				}
			}
			catch (IOException e)
			{
				System.out.println("VirtualChild.getExtractedFile(): ");
				System.out.println(e.getMessage());
			}
		}
		
		if (isDirectory)
		{
			return new File(_extractedFile, name);
		}
		else
		{
			return _extractedFile;
		}
	}

	/**
	 * Gets the extracted file or directory represented by this VirtualChild from the archive,
	 * and replaces the object referred to by <code>destination</code> with that extracted file or directory.
	 * Note that the extracted file is cached after it is extracted once, but if the 
	 * timestamps on the cached and archived files do not match, the cached file is erased,
	 * and reextracted from the archive.
	 * <code>destination</code> is always overwritten with either what is cached, or
	 * what is in the archive.
	 * @return true if and only if the extraction succeeded.
	 */
	public boolean getExtractedFile(File destination)
	{
		return getExtractedFile(destination, SystemEncodingUtil.ENCODING_UTF_8, false);
	}
	
	/**
	 * Gets the extracted file or directory represented by this VirtualChild from the archive,
	 * and replaces the object referred to by <code>destination</code> with that extracted file or directory.
	 * Note that the extracted file is cached after it is extracted once, but if the 
	 * timestamps on the cached and archived files do not match, the cached file is erased,
	 * and reextracted from the archive.
	 * <code>destination</code> is always overwritten with either what is cached, or
	 * what is in the archive.
	 * @return true if and only if the extraction succeeded.
	 */
	public boolean getExtractedFile(File destination, String sourceEncoding, boolean isText)
	{
		boolean success = true;
		if (_handler == null) return false;
		if (_extractedFile == null || 
		    _extractedFile.lastModified() != getTimeStamp() ||
		    !destination.getAbsolutePath().equals(_extractedFile.getAbsolutePath())
		    )
		{
			if (isDirectory)
			{
				success = _handler.extractVirtualDirectory(fullName, destination.getParentFile(), destination, sourceEncoding, isText);
			}
			else
			{
				success = _handler.extractVirtualFile(fullName, destination, sourceEncoding, isText);
			}
			_extractedFile = destination;
		}
		return success;
	}
	
	/**
	 * @return Whether or not this VirtualChild exists in the archive.
	 */
	public boolean exists()
	{
		if (_handler == null) return false;
		return _handler.exists(fullName);
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
		int i = newName.lastIndexOf("/");
		if (i == -1)
		{
			name = newName;
			path = "";
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