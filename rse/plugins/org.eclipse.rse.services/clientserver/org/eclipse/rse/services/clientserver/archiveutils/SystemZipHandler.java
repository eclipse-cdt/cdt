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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.java.BasicClassFileParser;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatchLocator;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;


/**
 * @author mjberger
 * Implements an ISystemArchiveHandler for ZIP files.
 */
public class SystemZipHandler implements ISystemArchiveHandler 
{

	protected ZipFile _zipfile; // The underlying zipfile associated with this handler.
	protected HashMap _virtualFS; // The virtual file system formed by the entries in _zipfile.
	//--------------------------------------------------------------------------------------------------------------------
	// Explanation of how the virtual file system is stored in a HashMap:
	//
	// _virtualFS is a HashMap of HashMaps. How does this suggest a tree structure?
	// The keys in _virtualFS are all Strings. There is one key in _virtualFS for every directory
	// in the virtual file system. The root directory has the key "". Associated with each key, is a value, and that value
	// is itself another HashMap, representing the contents of that directory. In the "inner" HashMap, each key is
	// a String giving the name of an object in the directory, and each associated value is a
	// VirtualChild representing that object itself. 
	//
	// Note that if the object is a directory, then
	// the value representing it in the inner HashMap is still a VirtualChild, not another 
	// HashMap. There are only two levels of HashMaps in the virtual file system. If the
	// object is a directory, then the VirtualChild object representing it will have
	// isDirectory == true. We can then find the contents of this directory, by going
	// back out to the outer HashMap, and retrieving the inner HashMap associated 
	// with our directory's name. 
	//
	// This file system is designed for quick retrieval of virtual objects.
	// Retrieving a single object whose full path is known can be done
	// in worst case O(1) time rather than O(h) time for a tree file system with height h.
	// Retrieving the children of an object takes worst case O(s) time, where s is the size
	// of the inner HashMap containing those children.
	// For insertion, the object must be inserted into its appropriate inner HashMap,
	// and then the HashMaps for all ancestors must either be created or updated, so this
	// takes worst case O(d) time, where d is the depth of the object in the virtual file tree.
	// For deletion, the argument is similar, that this takes O(d) time.
	// For renames, the worst case is O(n) time, where n is the number of nodes in the
	// virtual file tree. This is because if we change the name of a directory under the root,
	// then we must change all of its children's VirtualChild objects as well. It is advisable
	// to just rebuild the tree for a rename.
	//
	// Building the tree from a list of entries in a zipfile takes O(nh) time, where n
	// is the number of entries in the zipfile, and h is the maximum height of an entry in
	// the virtual file system.
	//--------------------------------------------------------------------------------------------------------------------
	
	protected File _file; // The underlying file associated with this handler.
	protected long _vfsLastModified; // The timestamp of the file that the virtual file system reflects.
	protected boolean _exists; // Whether or not the zipfile "exists" (in order to exist, must be uncorrupted too)
	
	/**
	 * Creates a new SystemZipHandler and associates it with <code>file</code>.
	 * @param file The file that this handler will wrapper.
	 * @throws IOException If there is an error handling <code>file</code>
	 */
	public SystemZipHandler(File file)
	{
		_file = file;
		_vfsLastModified = _file.lastModified();
		if (openZipFile()) 
		{
			buildTree(); 
			closeZipFile();
			_exists = true;
		}
		else
		{
			_exists = false;
		}
	}
	
	/**
	 * Builds the virtual file system tree out of the entries in
	 * the zipfile.
	 *
	 */
	protected void buildTree()
	{
		_virtualFS = new HashMap();
		Enumeration entries = _zipfile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry next = (ZipEntry) entries.nextElement();
			fillBranch(next);
		}
	}

	/**
	 * Populates an entire branch of the tree that comprises the
	 * virtual file system. The parameter is the leaf node, and from
	 * the virtual path of the parameter, we can deduce what the ancestors
	 * of the leaves are, and populate the tree from there.
	 * @param next The ZipEntry from which the branch will be built.
	 */
	protected void fillBranch(ZipEntry next)
	{
		VirtualChild nextChild;
		if (next.getName().equals("/")) return; // dummy entry
		if (!next.isDirectory())
		{
			SystemUniversalZipEntry nextEntry = new SystemUniversalZipEntry(next);
			nextChild = new VirtualChild(this, nextEntry.getFullName());
		}
		else // it is a directory
		{
			SystemUniversalZipEntry nextEntry = new SystemUniversalZipEntry(next);
			nextChild = new VirtualChild(this, nextEntry.getFullName());
			nextChild.isDirectory = true;
				
			if (!_virtualFS.containsKey(nextChild.fullName))
			{
				_virtualFS.put(nextChild.fullName, new HashMap());
			}
				
		}
		//	key has not been encountered before, create a new 
		// element in the virtualFS.
		if (!_virtualFS.containsKey(nextChild.path))
		{
			recursivePopulate(nextChild.path, nextChild);
		}
		else // key has been encountered before, no need to recursively
			 // populate the subdirectories
		{
			HashMap hm = (HashMap) _virtualFS.get(nextChild.path);
			hm.put(nextChild.name, nextChild);
		}		
	}

	/**
	 * Actually does the work for the fillBranch method. Recursively
	 * inserts key/value pairs into the virtualFS, then uses the key
	 * to get the next parent (moving up one level) and thus recursively
	 * populates one branch.
	 */
	protected void recursivePopulate(String key, VirtualChild value)
	{
		// base case 1: key has been encountered before, finish recursing
		if (_virtualFS.containsKey(key))
		{
			HashMap hm = (HashMap) _virtualFS.get(key);
			hm.put(value.name, value);
			return;
		}
		
		// else
		HashMap newValue = new HashMap();
		newValue.put(value.name, value);
		_virtualFS.put(key, newValue);
		
		// base case 2
		if (key.equals(""))
		{
			return;
		}
		else
		{
			int i = key.lastIndexOf("/");
			if (i == -1) // recursive last step
			{
				VirtualChild nextValue = new VirtualChild(this, key);
				nextValue.isDirectory = true;
				recursivePopulate("", nextValue);
				return;
			}
			else // recursive step
			{
				String newKey = key.substring(0, i);
				VirtualChild nextValue = new VirtualChild(this, key);
				nextValue.isDirectory = true;
				recursivePopulate(newKey, nextValue);
				return;
			}
				
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getVirtualChildrenList()
	 */
	public VirtualChild[] getVirtualChildrenList()
	{
		return getVirtualChildrenList(true);
	}
	
	/**
	 * Same as getVirtualChildrenList(), but you can choose whether
	 * to leave the zip file open or closed upon return.
	 */ 
	public VirtualChild[] getVirtualChildrenList(boolean closeZipFile) 
	{
		if (!_exists) return new VirtualChild[0];
		if (!updateVirtualFSIfNecessary()) return new VirtualChild[0];
		
		if (openZipFile())
		{
			Vector children = new Vector();
			Enumeration entries = _zipfile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry next = (ZipEntry) entries.nextElement();
				SystemUniversalZipEntry nextEntry = new SystemUniversalZipEntry(next);
				VirtualChild nextChild = new VirtualChild(this, nextEntry.getFullName());
				nextChild.isDirectory = next.isDirectory();
				children.add(nextChild);
			}
			VirtualChild[] retVal = new VirtualChild[children.size()];
			for (int i = 0; i < children.size(); i++)
			{
				retVal[i] = (VirtualChild) children.get(i);
			}
			if (closeZipFile) closeZipFile();
			return retVal;
		}
		else return new VirtualChild[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getVirtualChildrenList(java.lang.String)
	 */
	public VirtualChild[] getVirtualChildrenList(String parent)
	{
		return getVirtualChildrenList(parent, true);
	}
	
	/**
	 * Same as getVirtualChildrenList(String parent) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 
	public VirtualChild[] getVirtualChildrenList(String parent, boolean closeZipFile) 
	{
		if (!_exists) return new VirtualChild[0];
		if (!updateVirtualFSIfNecessary()) return new VirtualChild[0];

		if (openZipFile())
		{
			parent = ArchiveHandlerManager.cleanUpVirtualPath(parent);
			Vector children = new Vector();
			Enumeration entries = _zipfile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry next = (ZipEntry) entries.nextElement();
				String nextName = ArchiveHandlerManager.cleanUpVirtualPath(next.getName());
				if (nextName.startsWith(parent) && !nextName.equals(parent+"/"))
				{
					SystemUniversalZipEntry nextEntry = new SystemUniversalZipEntry(next);
					VirtualChild nextChild = new VirtualChild(this, nextEntry.getFullName());
					nextChild.isDirectory = next.isDirectory();
					children.add(nextChild);
				}
			}
			VirtualChild[] retVal = new VirtualChild[children.size()];
			for (int i = 0; i < children.size(); i++)
			{
				retVal[i] = (VirtualChild) children.get(i);
			}
			if (closeZipFile) closeZipFile();
			return retVal;
		}
		else return new VirtualChild[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getVirtualChildren(java.lang.String)
	 */
	public VirtualChild[] getVirtualChildren(String fullVirtualName)
	{
		if (!_exists) return null;
		if (!updateVirtualFSIfNecessary()) return null;
		
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		VirtualChild[] values = null;
		if (_virtualFS.containsKey(fullVirtualName)) 
		{
			HashMap hm = (HashMap) _virtualFS.get(fullVirtualName);
			Object valueArray[] = hm.values().toArray();
			values = new VirtualChild[hm.size()];
			for (int i = 0; i < hm.size(); i++)
			{
				values[i] = (VirtualChild) valueArray[i];
			}
			
		}
		return values;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getVirtualChildFolders(java.lang.String)
	 */
	public VirtualChild[] getVirtualChildFolders(String fullVirtualName)
	{
		if (!_exists) return null;
		if (!updateVirtualFSIfNecessary()) return null;
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		Vector folders = new Vector();
		VirtualChild[] values = null;
		if (_virtualFS.containsKey(fullVirtualName)) 
		{
			HashMap hm = (HashMap) _virtualFS.get(fullVirtualName);
			Object valueArray[] = hm.values().toArray();
			for (int i = 0; i < hm.size(); i++)
			{
				if (((VirtualChild) valueArray[i]).isDirectory) folders.add(valueArray[i]);
			}
			values = new VirtualChild[folders.size()];
			for (int i = 0; i < folders.size(); i++)
			{
				values[i] = (VirtualChild) folders.get(i);
			}
		}
		return values;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getVirtualFile(java.lang.String)
	 */
	public VirtualChild getVirtualFile(String fullVirtualName) 
	{
		if (!_exists) return new VirtualChild(this, fullVirtualName);
		if (!updateVirtualFSIfNecessary()) return new VirtualChild(this, fullVirtualName);

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		if (fullVirtualName == "" || fullVirtualName == null) return new VirtualChild(this);
		int i = fullVirtualName.lastIndexOf("/");
		String path;
		String name;
		if (i == -1)
		{
			path = "";
			name = fullVirtualName;
		}
		else
		{
			path = fullVirtualName.substring(0, i);
			name = fullVirtualName.substring(i+1);
		}
		HashMap hm = (HashMap) _virtualFS.get(path);
		if (hm == null) return new VirtualChild(this, fullVirtualName);
		VirtualChild vc = (VirtualChild) hm.get(name);
		if (vc == null) return new VirtualChild(this, fullVirtualName);
		return vc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#exists(java.lang.String)
	 */
	public boolean exists(String fullVirtualName)
	{
		if (!_exists) return false;

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		if (fullVirtualName == "" || fullVirtualName == null) return false;
		
		if (_vfsLastModified == _file.lastModified())
		{
			int i = fullVirtualName.lastIndexOf("/");
			String path;
			String name;
			if (i == -1)
			{
				path = "";
				name = fullVirtualName;
			}
			else
			{
				path = fullVirtualName.substring(0, i);
				name = fullVirtualName.substring(i+1);
			}
			HashMap hm = (HashMap) _virtualFS.get(path);
			if (hm == null) return false;
			if (hm.get(name) == null) return false;
			return true;
		}
		else
		{
			boolean retval = false;
			boolean keepOpen = _zipfile != null;
			if (openZipFile())
			{
				try
				{
					safeGetEntry(fullVirtualName);
					retval = true;
				}
				catch (IOException e)
				{
					try
					{
						safeGetEntry(fullVirtualName + "/");
						retval = true;
					}
					catch (IOException f)
					{
						retval = false;
					}
				}
				buildTree();
				_vfsLastModified = _file.lastModified();
				if (!keepOpen) closeZipFile();
				return retval;
			}
			else
			{
				System.out.println("Could not open the ZipFile " + _file.toString());
				return false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getArchive()
	 */
	public File getArchive()
	{
		return _file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getTimeStampFor(java.lang.String)
	 */
	public long getTimeStampFor(String fullVirtualName)
	{
		return getTimeStampFor(fullVirtualName, true);
	}

	/**
	 * Same as getTimeStampFor(String fullVirtualName) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 	 
	public long getTimeStampFor(String fullVirtualName, boolean closeZipFile)
	{
		if (!_exists) return 0;

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
			}
			catch (IOException e)
			{
				if (closeZipFile) closeZipFile();
				return _file.lastModified();
			}
			if (closeZipFile) closeZipFile();
			return entry.getTime();
		}
		else return _file.lastModified();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getSizeFor(java.lang.String)
	 */
	public long getSizeFor(String fullVirtualName)
	{
		return getSizeFor(fullVirtualName, true);
	}

	public long getSizeFor(String fullVirtualName, boolean closeZipFile)
	{
		if (!_exists) return 0;

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
			}
			catch (IOException e)
			{ 
				if (closeZipFile) closeZipFile();
				return 0;
			}
			if (closeZipFile) closeZipFile();
			return entry.getSize();
		}
		else return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#extractVirtualFile(java.lang.String, java.io.File)
	 */
	public boolean extractVirtualFile(String fullVirtualName, File destination)
	{
		return extractVirtualFile(fullVirtualName, destination, true, SystemEncodingUtil.ENCODING_UTF_8, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#extractVirtualFile(java.lang.String, java.io.File, java.lang.String, boolean)
	 */
	public boolean extractVirtualFile(String fullVirtualName, File destination, String sourceEncoding, boolean isText)
	{
		return extractVirtualFile(fullVirtualName, destination, true, sourceEncoding, isText);
	}

	/**
	 * Same as extractVirtualFile(String fullVirtualName, File destination) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 	
	public boolean extractVirtualFile(String fullVirtualName, File destination, boolean closeZipFile, String sourceEncoding, boolean isText)
	{
		if (!_exists) return false;

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
				if (entry.isDirectory())
				{
					destination.delete();
					destination.mkdirs();
					destination.setLastModified(entry.getTime());
					if (closeZipFile) closeZipFile();
					return true;
				}
				InputStream is = _zipfile.getInputStream(entry);
				if (is == null)
				{
					destination.setLastModified(entry.getTime());
					if (closeZipFile) closeZipFile();
					return true;
				}
				BufferedInputStream reader = new BufferedInputStream(is);
				
				if (!destination.exists())
				{
				    File parentFile = destination.getParentFile();
				    if (!parentFile.exists())
				        parentFile.mkdirs();
				    destination.createNewFile();
				}
				BufferedOutputStream writer = new BufferedOutputStream(
										new FileOutputStream(destination));
			
				byte[] buf = new byte[1024];
				int numRead = reader.read(buf);
			
				while (numRead > 0)
				{
					if (isText)
					{
						String bufString = new String(buf, sourceEncoding);
						byte[] convertedBuf = bufString.getBytes();
						int newSize = convertedBuf.length;
						writer.write(convertedBuf, 0, newSize);
					}
					else
					{
						writer.write(buf, 0, numRead);
					}
					numRead = reader.read(buf);	
				}
				writer.close();
				reader.close();
			}
			catch (IOException e)
			{
				if (_virtualFS.containsKey(fullVirtualName))
				{
					destination.delete();
					destination.mkdirs();
					destination.setLastModified(_file.lastModified());
					if (closeZipFile) closeZipFile();
					return true;
				}
				System.out.println(e.getMessage());
				if (closeZipFile) closeZipFile();
				return false;				   
			}
			destination.setLastModified(entry.getTime());
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#extractVirtualDirectory(java.lang.String, java.io.File)
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent)
	{
		return extractVirtualDirectory(dir, destinationParent, (File) null, SystemEncodingUtil.ENCODING_UTF_8, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#extractVirtualDirectory(java.lang.String, java.io.File, java.lang.String, boolean)
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, String sourceEncoding, boolean isText)
	{
		return extractVirtualDirectory(dir, destinationParent, (File) null, sourceEncoding, isText);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#extractVirtualDirectory(java.lang.String, java.io.File, java.io.File)
	 */
	public boolean extractVirtualDirectory(String dir, File destinationParent, File destination)
	{
		return extractVirtualDirectory(dir, destinationParent, destination, SystemEncodingUtil.ENCODING_UTF_8, false);
	}
	
	public boolean extractVirtualDirectory(String dir, File destinationParent, File destination, String sourceEncoding, boolean isText)
	{
		if (!_exists) return false;

		if (!destinationParent.isDirectory()) return false;
		dir = ArchiveHandlerManager.cleanUpVirtualPath(dir);
		if (!_virtualFS.containsKey(dir)) return false;
		
		String name;
		int charsToTrim;
		int j = dir.lastIndexOf("/");
		if (j == -1) 
		{
			charsToTrim = 0;
			name = dir;
		}
		else
		{
			charsToTrim = dir.substring(0,j).length() + 1;
			name = dir.substring(j+1);
		}

		if (destination == null)
		{
			if (dir.equals(""))
			{
				destination = destinationParent;
			}
			else
			{
				destination = new File(destinationParent, name);
			} 
		}
		
		if (!(destination == destinationParent))
		{
			if (destination.isFile() && destination.exists())
			{
				if (!SystemArchiveUtil.delete(destination))
				{
					System.out.println("Could not overwrite directory " + destination);
					System.out.println("(Could not delete old directory)");
					return false;
				}
			}
		
			if (!destination.exists())
			{
				if (!destination.mkdirs())
				{
					System.out.println("Could not overwrite directory " + destination);
					System.out.println("(Could not create new directory)");
					return false;
				}
			}
		}

		File topFile = destination;
		String topFilePath = topFile.getAbsolutePath().replace('\\', '/');				
		//if (!dir.equals(topFile.getName()))
		if (!topFilePath.endsWith(dir))    
		{
		 rename(dir, topFile.getName());  
		 dir = topFile.getName();
		}
		VirtualChild[] newChildren = getVirtualChildrenList(dir);

		
		extractVirtualFile(dir + '/', topFile, sourceEncoding, isText);
		
		for (int i = 0; i < newChildren.length; i++)
		{
			String newName = newChildren[i].fullName.substring(charsToTrim);
			char separator = File.separatorChar;
			newName = newName.replace('/', separator);
			
			File nextFile = new File(destinationParent, newName);
			/*
			// DKM: case where a rename has taken place
			// don't want to extract root folder as it appears in zip 
			if (!nextFile.getParent().equals(destination.getPath()) && 
			        nextFile.getParentFile().getParent().equals(destination.getParent()))
			{
			    nextFile = new File(destination, nextFile.getName());
			}
			*/
			if (!nextFile.exists())
			{
				if (newChildren[i].isDirectory)
				{
					if (!nextFile.mkdirs())
					{
						System.out.println("Could not create folder " + nextFile.toString());
						return false;
					}
				}
				else
				{
					createFile(nextFile);
				}
				boolean success = false;
				if (newChildren[i].isDirectory)
				{
				    success = extractVirtualFile(newChildren[i].fullName + '/', nextFile, sourceEncoding, isText);
				}
				else
				{
				    success = extractVirtualFile(newChildren[i].fullName, nextFile, sourceEncoding, isText);
				}
				if (!success) return false;
			}		
		}
		return true;
	}
	
	protected boolean createFile(File file)
	{
		try
		{
			if (!file.createNewFile())
			{
				System.out.println("File already exists: " + file.toString()); 
				return false; 
			}
			else
			{
				return true;
			}
		}
		catch (IOException e)
		{
			if (!file.getParentFile().exists() && file.getParentFile().mkdirs())
			{
				return createFile(file);
			}
			else
			{
				System.out.println("Could not create " + file.toString());
				System.out.println(e.getMessage());
				return false;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#add(java.io.File, java.lang.String, java.lang.String)
	 */
	public boolean add(File file, String virtualPath, String name)
	{
		return add(file, virtualPath, name, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
	}
	
	public boolean add(InputStream stream, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText) 
	{
		if (!_exists) return false;
		virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);

		if (exists(virtualPath + "/" + name))
		{
			// wrong method
			return replace(virtualPath + "/" + name, stream, name, sourceEncoding, targetEncoding, isText);
		}
		else
		{
			if (openZipFile())
			{
				virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);
				File outputTempFile;
				try
				{
					// Open a new tempfile which will be our destination for the new zip
					outputTempFile = new File(_file.getAbsolutePath() + "temp");
					ZipOutputStream  dest = new ZipOutputStream(
									  new FileOutputStream(outputTempFile));

					dest.setMethod(ZipOutputStream.DEFLATED);
					// get all the entries in the old zip				  
					VirtualChild[] vcList = getVirtualChildrenList(false);
					
					// if it is an empty zip file, no need to recreate it
					if (!(vcList.length == 1) || !vcList[0].fullName.equals(""))
					{
						recreateZipDeleteEntries(vcList, dest, null);
					}
					
					// append the additional entry to the zip file.
					ZipEntry newEntry = appendBytes(stream, dest, virtualPath, name, sourceEncoding, targetEncoding, isText);
					// Add the new entry to the virtual file system in memory
					fillBranch(newEntry);
					
					dest.close();
					
					// Now replace the old zip file with the new one
					replaceOldZip(outputTempFile);
				
				}
				catch (IOException e)
				{
					System.out.println("Could not add a file.");
					System.out.println(e.getMessage());
					closeZipFile();
					return false;
				}
				closeZipFile();
				return true;
			}
			else return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#add(java.io.File[], java.lang.String, java.lang.String[])
	 */
	public boolean add(File[] files, String virtualPath, String[] names) 
	{
		String[] encodings = new String[files.length];
		boolean[] isTexts = new boolean[files.length];
		for (int i = 0; i < files.length; i++)
		{
			encodings[i] = SystemEncodingUtil.ENCODING_UTF_8;
			isTexts[i] = false;
		}
		return add(files, virtualPath, names, encodings, encodings, isTexts, true);
	}

	public boolean add(File[] files, String virtualPath, String[] names, String[] sourceEncodings, String[] targetEncodings, boolean[] isText)
	{
		return add(files, virtualPath, names, sourceEncodings, targetEncodings, isText, true);
	}

	
	/**
	 * Same as add(File[] files, String virtualPath, String[] names, String[] encodings) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 
	public boolean add(File[] files, String virtualPath, String[] names, String[] sourceEncodings, String[] targetEncodings, boolean[] isText, boolean closeZipFile) 
	{
		if (!_exists) return false;
		if (openZipFile())
		{
			virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);
			int numFiles = files.length;
			for (int i = 0; i < numFiles; i++)
			{		
				if (!files[i].exists() || !files[i].canRead()) return false;
				if (exists(virtualPath + "/" + names[i]))
				{
					// sorry, wrong method buddy
					return replace(virtualPath + "/" + names[i], files[i], names[i]);
				}
			}
			File outputTempFile;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
								  new FileOutputStream(outputTempFile));

				dest.setMethod(ZipOutputStream.DEFLATED);
				// get all the entries in the old zip				  
				VirtualChild[] vcList = getVirtualChildrenList(false);
				
				// if it is an empty zip file, no need to recreate it
				if (!(vcList.length == 1) || !vcList[0].fullName.equals(""))
				{
					recreateZipDeleteEntries(vcList, dest, null);
				}
				
				// Now for each new file to add
				for (int i = 0; i < numFiles; i++)
				{
					// append the additional entry to the zip file.
					ZipEntry newEntry = appendFile(files[i], dest, virtualPath, names[i], sourceEncodings[i], targetEncodings[i], isText[i]);
					// Add the new entry to the virtual file system in memory
					fillBranch(newEntry);
				}
				dest.close();
				
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
			
			}
			catch (IOException e)
			{
				System.out.println("Could not add a file.");
				System.out.println(e.getMessage());
				if (closeZipFile) closeZipFile();
				return false;
			}
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}

	/**
	 * Helper method. . . populates <code>found</code> with a 
	 * collapsed list of all nodes in the subtree
	 * of the file system rooted at <code>parent</code>.
	 */
	public static void listAllFiles(File parent, HashSet found)
	{
		File[] children = parent.listFiles();
		if (children == null) // DKM - 56031, no authority on parent yields null
		{
		    found.remove(parent);
		    return;
		}
		for (int i = 0; i < children.length; i++)
		{
			if (!found.contains(children[i])) // prevent infinite loops due to symlinks
			{
			    if (children[i].canRead())
			    {
			        found.add(children[i]);
			        if (children[i].isDirectory())
					{	
				    	listAllFiles(children[i], found);
					}
			    }
			}
		}
	}

	/**
	 * Recreates a zip file from a list of virtual children, optionally
	 * omitting a group of children whose names are in the Set omitChildren
	 * @param vcList The list of virtual children to create the zip from
	 * @param dest The ZipOutputStream representing the zip file where the
	 * children are to be recreated
	 * @param omitChildren The set of names of children to omit when creating
	 * the zipfile. Null or empty set if there are no ommisions.
	 * @throws IOException
	 */
	protected void recreateZipDeleteEntries(VirtualChild[] vcList, ZipOutputStream dest, HashSet omitChildren) throws IOException
	{
		if (!(omitChildren == null) && vcList.length == omitChildren.size())
		{
			// the zip file will be empty, but it must have at least one entry,
			// so we will put in a dummy entry.
			ZipEntry entry = new ZipEntry("/");
			dest.putNextEntry(entry);
			dest.closeEntry();
			return;
		}
		//else
		for (int i = 0; i < vcList.length; i++)
		{
			// for each entry, append it to the new temp zip
			// unless it is in the set of omissions
			if (omitChildren != null && omitChildren.contains(vcList[i].fullName)) continue;
			if (vcList[i].isDirectory)
			{
				ZipEntry nextEntry = safeGetEntry(vcList[i].fullName + "/");
				dest.putNextEntry(nextEntry);
				dest.closeEntry();
				continue;
			}
			ZipEntry nextEntry = safeGetEntry(vcList[i].fullName);
			BufferedInputStream source = new BufferedInputStream(
										 _zipfile.getInputStream(nextEntry));
			nextEntry.setCompressedSize(-1);
			dest.putNextEntry(nextEntry);
			byte[] buf = new byte[1024];
			int numRead = source.read(buf);
		
			while (numRead > 0)
			{
				dest.write(buf, 0, numRead);
				numRead = source.read(buf);	
			}
			dest.closeEntry();
			source.close();
		}	
	}
	
	/**
	 * Recreates a zip file from a list of virtual children, but renaming the
	 * one of the VirtualChildren.
	 * @param vcList The list of virtual children to create the zip from
	 * @param dest The ZipOutputStream representing the zip file where the
	 * children are to be recreated
	 * @param oldName The name of the virtual child to rename
	 * @param newName The new name for the renamed virtual child.
	 * @throws IOException
	 */
	protected void recreateZipRenameEntries(VirtualChild[] vcList, ZipOutputStream dest, HashMap names) throws IOException
	{
		for (int i = 0; i < vcList.length; i++)
		{
			// for each entry, append it to the new temp zip
			ZipEntry nextEntry;
			ZipEntry newEntry;
			if (names.containsKey(vcList[i].getArchiveStandardName()))
			{
				// rename the entry
				String oldName = vcList[i].getArchiveStandardName();
				String newName = (String) names.get(oldName);
				vcList[i].renameTo(newName);
				nextEntry = safeGetEntry(oldName);
				newEntry = createSafeZipEntry(newName);
				newEntry.setComment(nextEntry.getComment());
				newEntry.setExtra(nextEntry.getExtra());
				newEntry.setTime(nextEntry.getTime());
			}
			else
			{
				nextEntry = safeGetEntry(vcList[i].getArchiveStandardName());
				newEntry = nextEntry;
			}
			if (nextEntry.isDirectory())
			{
				dest.putNextEntry(newEntry);
				dest.closeEntry();
				continue;
			}
			BufferedInputStream source = new BufferedInputStream(
										 _zipfile.getInputStream(nextEntry));
			newEntry.setCompressedSize(-1);
			dest.putNextEntry(newEntry);
			byte[] buf = new byte[1024];
			int numRead = source.read(buf);
		
			while (numRead > 0)
			{
				dest.write(buf, 0, numRead);
				numRead = source.read(buf);	
			}
			dest.closeEntry();
			source.close();
		}	
	}

	/**
	 * Compresses the contents of <code>file</code>, adding them to the
	 * ZipFile managed by <code>dest</code>. The file is encoded in the encoding
	 * specified by <code>encoding</code>. A new entry is created in the
	 * ZipFile with virtual path and name of <code>virtualPath</code> and <code>name</code>
	 * respectively. 
	 * @return The ZipEntry that was added to the destination zip file.
	 * @throws IOException
	 */
	protected ZipEntry appendFile(File file, ZipOutputStream dest, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText) throws IOException
	{
		ZipEntry newEntry;
		if (file.isDirectory())
		{	
			String fullName = virtualPath + "/" + name;
			if (!fullName.endsWith("/")) fullName = fullName + "/";
			newEntry = createSafeZipEntry(fullName);
		}
		else
		{
			newEntry = createSafeZipEntry(virtualPath + "/" + name);
		}
		newEntry.setTime(file.lastModified());
		dest.putNextEntry(newEntry);
		if (!file.isDirectory())
		{
			BufferedInputStream source = new BufferedInputStream(
										 new FileInputStream(file));
	
			byte[] buf = new byte[1024];
			int numRead = source.read(buf);
			long fileSize = file.length();
			long totalRead = 0;
			while (numRead > 0 && totalRead < fileSize)
			{
			    totalRead += numRead;
				if (isText)
				{
				    // DKM - if you don't specify numRead here, then buf will get picked up wiht extra bytes from b4!!!!
					String bufString = new String(buf, 0, numRead, sourceEncoding);
					byte[] convertedBuf = bufString.getBytes(targetEncoding);
					int newSize = convertedBuf.length;
					dest.write(convertedBuf, 0, newSize);
				}
				else
				{
					dest.write(buf, 0, numRead);
				}
				// specify max size here
				long maxRead = 1024;
				long deltaLeft = fileSize - totalRead;
				if (deltaLeft > 1024)
				{ 
				    numRead = source.read(buf, 0, (int)maxRead);	
				}
				else
				{
				    numRead = source.read(buf, 0, (int)deltaLeft);
				}
				
			}
			dest.closeEntry();
			source.close();
		}
		return newEntry;
	}

	/**
	 * Compresses the contents of <code>stream</code>, adding them to the
	 * ZipFile managed by <code>dest</code>. The stream is encoded in the encoding
	 * specified by <code>encoding</code>. A new entry is created in the
	 * ZipFile with virtual path and name of <code>virtualPath</code> and <code>name</code>
	 * respectively. 
	 * @return The ZipEntry that was added to the destination zip file.
	 * @throws IOException
	 */
	protected ZipEntry appendBytes(InputStream stream, ZipOutputStream dest, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText) throws IOException
	{
		ZipEntry newEntry;
		newEntry = createSafeZipEntry(virtualPath + "/" + name);
		dest.putNextEntry(newEntry);
		BufferedInputStream source = new BufferedInputStream(stream);

		byte[] buf = new byte[1024];
		int numRead = source.read(buf);
		long totalRead = 0;
		while (numRead > 0 && source.available() > 0)
		{
		    totalRead += numRead;
			if (isText)
			{
			    // DKM - if you don't specify numRead here, then buf will get picked up wiht extra bytes from b4!!!!
				String bufString = new String(buf, 0, numRead, sourceEncoding);
				byte[] convertedBuf = bufString.getBytes(targetEncoding);
				int newSize = convertedBuf.length;
				dest.write(convertedBuf, 0, newSize);
			}
			else
			{
				dest.write(buf, 0, numRead);
			}
			// specify max size here
			long maxRead = 1024;
			long deltaLeft = source.available();
			if (deltaLeft > 1024)
			{ 
			    numRead = source.read(buf, 0, (int)maxRead);	
			}
			else
			{
			    numRead = source.read(buf, 0, (int)deltaLeft);
			}
			
		}
		dest.closeEntry();
		source.close();
		return newEntry;
	}
	
	/**
	 * Replaces the old zip file managed by this SystemZipHandler, with
	 * the zip file referred to by outputTempFile.
	 * @throws IOException if outputTempFile cannot be used as a ZipFile.
	 */
	protected void replaceOldZip(File outputTempFile) throws IOException
	{
		String oldName = _file.getAbsolutePath();
		_zipfile.close();
		File oldFile = new File(oldName + "old");
		System.out.println(_file.renameTo(oldFile));
		System.out.println(outputTempFile.renameTo(_file));
		_vfsLastModified = _file.lastModified();
		_zipfile = new ZipFile(_file);
		oldFile.delete();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#delete(java.lang.String)
	 */
	public boolean delete(String fullVirtualName) 
	{
		return delete(fullVirtualName, true);
	}

	/**
	 * Same as delete(String fullVirtualName) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 
	public boolean delete(String fullVirtualName, boolean closeZipFile) 
	{
		if (!_exists) return false;
		
		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			VirtualChild vc = getVirtualFile(fullVirtualName);
			VirtualChild[] vcList;
			VirtualChild[] vcOmmit = new VirtualChild[1];
			if (!vc.exists())
			{
				if (closeZipFile) closeZipFile();
				return false;
			} // file doesn't exist
			
			if (vc.isDirectory) // file is a directory, we must delete the contents
			{
				vcOmmit = getVirtualChildrenList(fullVirtualName, false);
			}
		
			File outputTempFile = null;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
								  new FileOutputStream(outputTempFile));
				dest.setMethod(ZipOutputStream.DEFLATED);
				
				// get all the entries in the old zip				  
				vcList = getVirtualChildrenList(false);
				
				HashSet omissions = new HashSet();
				
				if (vc.isDirectory)
				{
					for (int i = 0; i < vcOmmit.length; i++)
					{
						omissions.add(vcOmmit[i].fullName);
					}
					try 
					{
						safeGetEntry(vc.fullName);
						omissions.add(vc.fullName);
					}
					catch (IOException e) {}
				}
				else
				{
					omissions.add(fullVirtualName);
				}
				
				// recreate the zip file without the omissions
				recreateZipDeleteEntries(vcList, dest, omissions);
				dest.close();
	
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
				
				// Now update the tree
				HashMap hm = (HashMap) _virtualFS.get(vc.path);
				hm.remove(vc.name);
				if (vc.isDirectory)
				{
					delTree(vc);
				}
			}
			catch (IOException e)
			{
				System.out.println(e.getMessage());
				System.out.println("Could not delete " + fullVirtualName);
				if (!(outputTempFile == null)) outputTempFile.delete();
				if (closeZipFile) closeZipFile();
				return false;
			}
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}

	/**
	 * Deletes all the children of the directory VirtualChild <code>vc</code>
	 * recursively down to the leaves.
	 * Pre: vc.isDirectory is true
	 * @param vc The child whose children we are deleting.
	 */
	protected void delTree(VirtualChild vc)
	{
		HashMap hm = (HashMap) _virtualFS.get(vc.fullName);
		Object[] children = hm.values().toArray();
		for (int i = 0; i < children.length; i++)
		{
			VirtualChild next = (VirtualChild) children[i];
			hm.remove(next.name);
			if (next.isDirectory) delTree(next);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#replace(java.lang.String, java.io.File, java.lang.String)
	 */
	public boolean replace(String fullVirtualName, File file, String name) 
	{
		return replace(fullVirtualName, file, name, true);
	}

	/**
	 * Same as replace(String fullVirtualName, File file, String name) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 	
	public boolean replace(String fullVirtualName, File file, String name, boolean closeZipFile) 
	{
		if (!_exists) return false;

		if (!file.exists() || !file.canRead()) return false;
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		if (!exists(fullVirtualName))
		{
			// sorry, wrong method buddy
			return add(file, fullVirtualName, name);
		}
		if (openZipFile())
		{
			File outputTempFile = null;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
								  new FileOutputStream(outputTempFile));
				dest.setMethod(ZipOutputStream.DEFLATED);
				// get all the entries in the old zip				  
				VirtualChild[] vcList = getVirtualChildrenList(false);
				HashSet omissions = new HashSet();
				omissions.add(fullVirtualName);
				recreateZipDeleteEntries(vcList, dest, omissions);
		
				// Now append the additional entry to the zip file.
				int i = fullVirtualName.lastIndexOf("/");
				String virtualPath;
				if (i == -1) 
				{
					virtualPath = "";
				}
				else
				{
					virtualPath = fullVirtualName.substring(0,i);
				}
				ZipEntry newEntry = appendFile(file, dest, virtualPath, name, SystemEncodingUtil.ENCODING_UTF_8, SystemEncodingUtil.ENCODING_UTF_8, false);
				dest.close();
		
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
				
			}
			catch (IOException e)
			{
				System.out.println("Could not replace " + file.getName());
				if (!(outputTempFile == null)) outputTempFile.delete();
				if (closeZipFile) closeZipFile();
				return false;
			}
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}

	public boolean replace(String fullVirtualName, InputStream stream, String name, String sourceEncoding, String targetEncoding, boolean isText) 
	{
		if (!_exists) return false;

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		if (!exists(fullVirtualName))
		{
			// wrong method
			return add(stream, fullVirtualName, name, sourceEncoding, targetEncoding, isText);
		}
		if (openZipFile())
		{
			File outputTempFile = null;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
								  new FileOutputStream(outputTempFile));
				dest.setMethod(ZipOutputStream.DEFLATED);
				// get all the entries in the old zip				  
				VirtualChild[] vcList = getVirtualChildrenList(false);
				HashSet omissions = new HashSet();
				omissions.add(fullVirtualName);
				recreateZipDeleteEntries(vcList, dest, omissions);
		
				// Now append the additional entry to the zip file.
				int i = fullVirtualName.lastIndexOf("/");
				String virtualPath;
				if (i == -1) 
				{
					virtualPath = "";
				}
				else
				{
					virtualPath = fullVirtualName.substring(0,i);
				}
				ZipEntry newEntry = appendBytes(stream, dest, virtualPath, name, sourceEncoding, targetEncoding, isText);
				dest.close();
		
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
				
			}
			catch (IOException e)
			{
				System.out.println("Could not replace " + fullVirtualName);
				if (!(outputTempFile == null)) outputTempFile.delete();
				closeZipFile();
				return false;
			}
			closeZipFile();
			return true;
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#fullRename(java.lang.String, java.lang.String)
	 */
	public boolean fullRename(String fullVirtualName, String newFullVirtualName) 
	{
		return fullRename(fullVirtualName, newFullVirtualName, true);
	}

	/**
	 * Same as fullRename(String fullVirtualName, String newFullVirtualName) but you can choose whether
	 * or not you want to leave the zipfile open after return. 
	 */ 
	public boolean fullRename(String fullVirtualName, String newFullVirtualName, boolean closeZipFile) 
	{
		if (!_exists) return false;

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		newFullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(newFullVirtualName);
		VirtualChild vc = getVirtualFile(fullVirtualName);
		if (!vc.exists())
		{
			System.out.println("The virtual file " + fullVirtualName + " does not exist.");
			return false;
		}
		if (openZipFile())
		{
			File outputTempFile = null;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
										new FileOutputStream(outputTempFile));
				dest.setMethod(ZipOutputStream.DEFLATED);
				// get all the entries in the old zip				  
				VirtualChild[] vcList = getVirtualChildrenList(false);
				VirtualChild[] renameList;
				HashMap names = new HashMap();
				// if the entry to rename is a directory, we must then rename
				// all files and directories below it en masse.
				if (vc.isDirectory)
				{
					renameList = getVirtualChildrenList(fullVirtualName, false);
					for (int i = 0; i < renameList.length; i++)
					{
						int j = fullVirtualName.length();
						String suffix = renameList[i].fullName.substring(j);
						String newName = newFullVirtualName + suffix;
						if (renameList[i].isDirectory) 
						{
							newName = newName + "/";
							names.put(renameList[i].fullName + "/", newName);
						}
						else
						{
							names.put(renameList[i].fullName, newName);
						}
					}
					try 
					{
						safeGetEntry(fullVirtualName);
						names.put(fullVirtualName + "/", newFullVirtualName + "/");
					}
					catch (IOException e) {}
				}
				else
				{
					names.put(fullVirtualName, newFullVirtualName);
				}
				// find the entry to rename and rename it
				recreateZipRenameEntries(vcList, dest, names);
				
				dest.close();
		
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
				
				// Now rebuild the tree
				buildTree();
			}
			catch (IOException e)
			{
				System.out.println("Could not rename " + fullVirtualName);
				if (!(outputTempFile == null)) outputTempFile.delete();
				if (closeZipFile) closeZipFile();
				return false;
			}
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#move(java.lang.String, java.lang.String)
	 */
	public boolean move(String fullVirtualName, String destinationVirtualPath) 
	{
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		destinationVirtualPath = ArchiveHandlerManager.cleanUpVirtualPath(destinationVirtualPath);
		int i = fullVirtualName.lastIndexOf("/");
		if (i == -1)
		{
			return fullRename(fullVirtualName, destinationVirtualPath + "/" + fullVirtualName);
		}
		String name = fullVirtualName.substring(i);
		return fullRename(fullVirtualName, destinationVirtualPath + name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#rename(java.lang.String, java.lang.String)
	 */
	public boolean rename(String fullVirtualName, String newName) 
	{
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		int i = fullVirtualName.lastIndexOf("/");
		if (i == -1)
		{
			return fullRename(fullVirtualName, newName);
		}
		String fullNewName = fullVirtualName.substring(0, i+1) + newName;
		return fullRename(fullVirtualName, fullNewName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getFiles(java.lang.String[])
	 */
	public File[] getFiles(String[] fullNames)
	{
		if (!_exists) return new File[0];

		File[] files = new File[fullNames.length];
		for (int i = 0; i < fullNames.length; i++)
		{
			String name;
			String fullName = fullNames[i];
			fullName = ArchiveHandlerManager.cleanUpVirtualPath(fullName);
			int j = fullName.lastIndexOf("/");
			if (j == -1)
			{
				name = fullName;
			}
			else
			{
				name = fullName.substring(j+1);
			}
			try
			{	
				files[i] = File.createTempFile(name, "virtual");
				files[i].deleteOnExit();
				extractVirtualFile(fullNames[i], files[i]);
			}
			catch (IOException e)
			{
				System.out.println(e.getMessage());
				System.out.println("Could not extract virtual file: " + fullNames[i]);
				return null;
			}
		}
		return files;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#createFolder(java.lang.String)
	 */
	public boolean createFolder(String name)
	{
		name = ArchiveHandlerManager.cleanUpVirtualPath(name);
		name = name + "/";
		return createVirtualObject(name, true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#createFile(java.lang.String)
	 */
	public boolean createFile(String name)
	{
		name = ArchiveHandlerManager.cleanUpVirtualPath(name);
		return createVirtualObject(name, true);
	}
	
	/**
	 * Creates a new, empty object in the virtual File system, and
	 * creates an empty file or folder in the physical zip file.
	 * @param name The name of the file or folder to create. The object
	 * created will be a folder if and only if
	 * <code>name</code> ends in a "/".
	 * @return Whether the creation was successful or not. 
	 */
	protected boolean createVirtualObject(String name, boolean closeZipFile)
	{
		if (!_exists) return false;
		if (exists(name))
		{
			// The object already exists.
			return false;
		}
		
		if (openZipFile())
		{
			File outputTempFile;
			try
			{
				// Open a new tempfile which will be our destination for the new zip
				outputTempFile = new File(_file.getAbsolutePath() + "temp");
				ZipOutputStream  dest = new ZipOutputStream(
								  new FileOutputStream(outputTempFile));
				dest.setMethod(ZipOutputStream.DEFLATED);
				// get all the entries in the old zip				  
				VirtualChild[] vcList = getVirtualChildrenList(false);
				
				// if it is an empty zip file, no need to recreate it
				if (!(vcList.length == 1) || !vcList[0].fullName.equals(""))
				{
					recreateZipDeleteEntries(vcList, dest, null);
				}
				
				// append the additional entry to the zip file.
				ZipEntry newEntry = appendEmptyFile(dest, name);
				// Add the new entry to the virtual file system in memory
				fillBranch(newEntry);
		
				dest.close();
				
				// Now replace the old zip file with the new one
				replaceOldZip(outputTempFile);
			
			}
			catch (IOException e)
			{
				System.out.println("Could not add a file.");
				System.out.println(e.getMessage());
				if (closeZipFile) closeZipFile();
				return false;
			}
			if (closeZipFile) closeZipFile();
			return true;
		}
		else return false;
	}
	
	/**
	 * Works similarly to appendFile, except no actual data is appended
	 * to the zipfile, only an entry is created. Thus, if the file were
	 * to be extracted, it would be of length 0.
	 * @param dest The destination zip stream to append the entry.
	 * @param name The new, virtual fullname to give the entry.
	 * @return The ZipEntry that was created.
	 * @throws IOException If there was an error appending the entry to the stream.
	 */
	protected ZipEntry appendEmptyFile(ZipOutputStream dest, String name) throws IOException
	{
		boolean isDirectory = name.endsWith("/");
		ZipEntry newEntry;
		newEntry = createSafeZipEntry(name);
		dest.putNextEntry(newEntry);
		if (!isDirectory)
		{
			dest.write(new byte[0], 0, 0);
			dest.closeEntry();
		}
		return newEntry;		
	}
	
	/**
	 * A "safe" ZipEntry is one whose virtual path does not begin with a
	 * "/". This seems to cause the least problems for archive utilities, 
	 * including this one.
	 * @param name The virtual name for the new, safe ZipEntry.
	 * @return The ZipEntry that is created.
	 */
	protected ZipEntry createSafeZipEntry(String name)
	{
		if (name.startsWith("/")) name = name.substring(1);
		return new ZipEntry(name);
	}
	
	public String getStandardName(VirtualChild vc)
	{
		if (vc.isDirectory) return vc.fullName + "/";
		return vc.fullName;
	}
	
	/**
	 * Opens the zipfile that this handler manages.
	 * @return Whether the zipfile was successfully opened.
	 */
	protected boolean openZipFile()
	{
		if (!(_zipfile == null)) closeZipFile();
		try
		{
			_zipfile = new ZipFile(_file);
		}
		catch (IOException e)
		{
			System.out.println("Could not open zipfile: " + _file);
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	protected boolean closeZipFile()
	{
		try
		{
			_zipfile.close();
		}
		catch (IOException e)
		{
			System.out.println("Could not close zipfile: " + _file);
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * If the mod-times of the underlying zip file and the file used to
	 * create the virtualFS are different, update the virtualFS.
	 * @return whether or not the op was successful.
	 */
	protected boolean updateVirtualFSIfNecessary()
	{
		if (_vfsLastModified != _file.lastModified())
		{
			if (openZipFile())
			{
				buildTree();
				_vfsLastModified = _file.lastModified();
				closeZipFile();
				return true;
			}
			else return false;
		}
		else return true;
	}
	
	/**
	 * Returns the entry corresponding to <code>name</code> from _zipfile. Never returns
	 * null, but rather, throws an IOException if it cannot find the entry. Tries to retrieve
	 * both <code>name</code> and <code>"/" + name<code>, to accomodate for zipfiles created
	 * in a unix environment. ASSUMES THAT _zipfile IS ALREADY OPEN!
	 */
	protected ZipEntry safeGetEntry(String name) throws IOException
	{
		ZipEntry entry = _zipfile.getEntry(name);
		if (entry == null) entry = _zipfile.getEntry("/" + name);
		if (entry == null) throw new IOException("SystemZipHandler.safeGetEntry(): The ZipEntry " + name + " cannot be found in " + _file.toString());
		return entry;
	}
	
	public boolean create() 
	{
		try
		{
			// The zipfile is our destination
			ZipOutputStream  dest = new ZipOutputStream(
							  new FileOutputStream(_file));
			dest.setMethod(ZipOutputStream.DEFLATED);
				
			VirtualChild[] vcList = new VirtualChild[0];
				
			HashSet omissions = new HashSet();
			// the above two statements force recreateZipDeleteEntries to create a dummy entry
			recreateZipDeleteEntries(vcList, dest, omissions);
			dest.close();
			
			if (openZipFile()) 
			{
				buildTree(); 
				closeZipFile();
			}
			else
			{
				return false;
			}
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			return false;
		}
		_exists = true;
		return true;
	}
	
	public SystemSearchLineMatch[] search(String fullVirtualName, SystemSearchStringMatcher matcher) 
	{
		
		// if the search string is empty or if it is "*", then return no matches
		// since it is a file search
		if (matcher.isSearchStringEmpty() || matcher.isSearchStringAsterisk()) {
			return new SystemSearchLineMatch[0];
		}
		
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			
		VirtualChild vc = getVirtualFile(fullVirtualName);
		
		if (!vc.exists() || vc.isDirectory) {
			return new SystemSearchLineMatch[0];
		}
		
		if (openZipFile()) {
			
			ZipEntry entry = null;
			
			SystemSearchLineMatch[] matches = null;
			
			try
			{
				entry = safeGetEntry(fullVirtualName);
				InputStream is = _zipfile.getInputStream(entry);
				
				if (is == null)
				{
					return new SystemSearchLineMatch[0];
				}
				
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader bufReader = new BufferedReader(isr);
				
				SystemSearchStringMatchLocator locator = new SystemSearchStringMatchLocator(bufReader, matcher);
				matches = locator.locateMatches();
			}
			catch (IOException e)
			{
				System.out.println(e.getMessage());				   
			}
			
			closeZipFile();
			
			if (matches == null) {
				return new SystemSearchLineMatch[0];
			}
			else {
				return matches;
			}
		}
		else {
			return new SystemSearchLineMatch[0];
		} 
	}
	
	public boolean exists()
	{
	        return _exists;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getCommentFor(java.lang.String)
	 */
	public String getCommentFor(String fullVirtualName) 
	{
		return getCommentFor(fullVirtualName, true);
	}

	/** 
	 * same as getCommentFor(String) but you can choose whether or not to leave
	 * the zipfile open after the method is closed
	 */
	public String getCommentFor(String fullVirtualName, boolean closeZipFile) 
	{
		if (!_exists) return "";

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
			}
			catch (IOException e)
			{ 
				if (closeZipFile) closeZipFile();
				return "";
			}
			if (closeZipFile) closeZipFile();
			String comment = entry.getComment();
			if (comment == null) return "";
			else return comment;
		}
		else return "";
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.archiveutils.ISystemArchiveHandler#getCompressedSizeFor(java.lang.String)
	 */
	public long getCompressedSizeFor(String fullVirtualName) 
	{
		return getCompressedSizeFor(fullVirtualName, true);
	}
	
	/** 
	 * same as getCompressedSizeFor(String) but you can choose whether or not to leave
	 * the zipfile open after the method is closed
	 */
	public long getCompressedSizeFor(String fullVirtualName, boolean closeZipFile) 
	{
		if (!_exists) return 0;

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
			}
			catch (IOException e)
			{ 
				if (closeZipFile) closeZipFile();
				return 0;
			}
			if (closeZipFile) closeZipFile();
			return entry.getCompressedSize();
		}
		else return 0;
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.archiveutils.ISystemArchiveHandler#getCompressionMethodFor(java.lang.String)
	 */
	public String getCompressionMethodFor(String fullVirtualName) 
	{
		return getCompressionMethodFor(fullVirtualName, true);
	}

	/** 
	 * same as getCompressionMethodFor(String) but you can choose whether or not to leave
	 * the zipfile open after the method is closed
	 */	
	public String getCompressionMethodFor(String fullVirtualName, boolean closeZipFile) 
	{
		if (!_exists) return "";

		if (openZipFile())
		{
			fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
			ZipEntry entry = null;
			try
			{
				entry = safeGetEntry(fullVirtualName);
			}
			catch (IOException e)
			{ 
				if (closeZipFile) closeZipFile();
				return "";
			}
			if (closeZipFile) closeZipFile();
			return (new Integer(entry.getMethod())).toString();
		}
		else return "";
	}
	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.core.archiveutils.ISystemArchiveHandler#getArchiveComment()
	 */
	public String getArchiveComment() 
	{
		return "";
	}

	/**
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#isExecutable(java.lang.String)
	 */
	public String getClassification(String fullVirtualName) {
		return getClassification(fullVirtualName, true);
	}
	
	/**
	 * Same as getClassification(String), but you can choose whether to leave the zip file
	 * open after the method is closed.
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#isExecutable(java.lang.String)
	 */
	public String getClassification(String fullVirtualName, boolean closeZipFile) {
		
		// default type
		String type = "file";
		
		if (!_exists) {
			return type;
		}
		
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		
		// if it's not a class file, we do not classify it
		if (!fullVirtualName.endsWith(".class")) {
			return type;
		}
		
		// class file parser
		BasicClassFileParser parser = null;
		
		boolean isExecutable = false;
		
		if (openZipFile()) {
			
			// get the input stream for the entry
			InputStream stream = null;
			
			try {
				ZipEntry entry = safeGetEntry(fullVirtualName);
				stream = _zipfile.getInputStream(entry);
				
				// use class file parser to parse the class file
				parser = new BasicClassFileParser(stream);
				parser.parse();
				
				// query if it is executable, i.e. whether it has main method
				isExecutable = parser.isExecutable();

				if (closeZipFile) {
					closeZipFile();
				}
			}
			catch (IOException e) {
				
				if (closeZipFile) {
					closeZipFile();
				}
				
				return type;
			}
		}
		
		// if it is executable, then also get qualified class name
		if (isExecutable) {
			type = "executable(java";
			
			String qualifiedClassName = parser.getQualifiedClassName();
			
			if (qualifiedClassName != null) {
    			type = type + ":" + qualifiedClassName;
			}
			
			type = type + ")";
		}
		
		return type;
	}

	public boolean add(File file, String virtualPath, String name, String sourceEncoding, String targetEncoding, ISystemFileTypes registry) 
	{
		if (!_exists) return false;
		virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);
		if (!file.isDirectory())
		{
			if (exists(virtualPath + "/" + name))
			{
				// wrong method
				return replace(virtualPath + "/" + name, file, name);
			}
			else
			{
				File[] files = new File[1];
				files[0] = file;
				String[] names = new String[1];
				names[0] = name;
				String[] sourceEncodings = new String[1];
				sourceEncodings[0] = sourceEncoding;
				String[] targetEncodings = new String[1];
				targetEncodings[0] = targetEncoding;
				boolean[] isTexts = new boolean[1];
				isTexts[0] = registry.isText(file);
				return add(files, virtualPath, names, sourceEncodings, targetEncodings, isTexts);
			}
		}
		else
		{
			String sourceName = name;
			HashSet children = new HashSet();
			listAllFiles(file, children);
			File[] sources = new File[children.size() + 1];
			String[] newNames = new String[children.size() + 1];
			Object[] kids = children.toArray();
			String[] sourceEncodings = new String[children.size() + 1];
			String[] targetEncodings = new String[children.size() + 1];
			boolean[] isTexts = new boolean[children.size() + 1];
			int charsToTrim = file.getParentFile().getAbsolutePath().length() + 1;
			if (file.getParentFile().getAbsolutePath().endsWith(File.separator)) charsToTrim--; // accounts for root
			for (int i = 0; i < children.size(); i++)
			{
				sources[i] = (File) kids[i];
				newNames[i] = sources[i].getAbsolutePath().substring(charsToTrim);
				newNames[i] = newNames[i].replace('\\','/');
				if (sources[i].isDirectory() && !newNames[i].endsWith("/")) newNames[i] = newNames[i] + "/";
			
				// this part can be changed to allow different encodings for different files
				sourceEncodings[i] = sourceEncoding;
				targetEncodings[i] = targetEncoding;
				isTexts[i] = registry.isText(sources[i]);
			}
			sources[children.size()] = file;
			newNames[children.size()] = name;
			sourceEncodings[children.size()] = sourceEncoding; 
			targetEncodings[children.size()] = targetEncoding; 

			isTexts[children.size()] = registry.isText(file);
			if  (!newNames[children.size()].endsWith("/")) newNames[children.size()] = newNames[children.size()] + "/";
			return add(sources, virtualPath, newNames, sourceEncodings, targetEncodings, isTexts);
		}
	}

	public boolean add(File file, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText) 
	{
		if (!_exists) return false;
		virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);
		if (!file.isDirectory())
		{
			if (exists(virtualPath + "/" + name))
			{
				// wrong method
				return replace(virtualPath + "/" + name, file, name);
			}
			else
			{
				File[] files = new File[1];
				files[0] = file;
				String[] names = new String[1];
				names[0] = name;
				String[] sourceEncodings = new String[1];
				sourceEncodings[0] = sourceEncoding;
				String[] targetEncodings = new String[1];
				targetEncodings[0] = targetEncoding;
				boolean[] isTexts = new boolean[1];
				isTexts[0] = isText;
				return add(files, virtualPath, names, sourceEncodings, targetEncodings, isTexts);
			}
		}
		else
		{
			String sourceName = name;
			HashSet children = new HashSet();
			listAllFiles(file, children);
			File[] sources = new File[children.size() + 1];
			String[] newNames = new String[children.size() + 1];
			Object[] kids = children.toArray();
			String[] sourceEncodings = new String[children.size() + 1];
			String[] targetEncodings = new String[children.size() + 1];
			boolean[] isTexts = new boolean[children.size() + 1];
			int charsToTrim = file.getParentFile().getAbsolutePath().length() + 1;
			if (file.getParentFile().getAbsolutePath().endsWith(File.separator)) charsToTrim--; // accounts for root
			for (int i = 0; i < children.size(); i++)
			{
				sources[i] = (File) kids[i];
				newNames[i] = sources[i].getAbsolutePath().substring(charsToTrim);
				newNames[i] = newNames[i].replace('\\','/');
				if (sources[i].isDirectory() && !newNames[i].endsWith("/")) newNames[i] = newNames[i] + "/";
			
				// this part can be changed to allow different encodings for different files
				sourceEncodings[i] = sourceEncoding;
				targetEncodings[i] = targetEncoding;
				isTexts[i] = isText;
			}
			sources[children.size()] = file;
			newNames[children.size()] = name;
			sourceEncodings[children.size()] = sourceEncoding;
			targetEncodings[children.size()] = targetEncoding;
			isTexts[children.size()] = isText;
			if  (!newNames[children.size()].endsWith("/")) newNames[children.size()] = newNames[children.size()] + "/";
			return add(sources, virtualPath, newNames, sourceEncodings, targetEncodings, isTexts);
		}
	}
}