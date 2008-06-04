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
 * Xuan Chen (IBM) - [194293] [Local][Archives] Saving file second time in an Archive Errors
 * Xuan Chen (IBM) - [199132] [Archives-TAR][Local-Windows] Can't open files in tar archives
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Xuan Chen (IBM) - [209828] Need to move the Create operation to a job.
 * Xuan Chen (IBM) - [209825] Update SystemTarHandler so that archive operations could be cancelable.
 * Xuan Chen (IBM) - [211551] NPE when moving multiple folders from one tar file to another tar file
 * Xuan Chen (IBM) - [211653] Copy virtual directory with nested directory of tar file did not work
 * Xuan Chen (IBM) - [214251] [archive] "Last Modified Time" changed for all virtual files/folders if rename/paste/delete of one virtual file.
 * Xuan Chen (IBM) - [191370] [dstore] Supertransfer zip not deleted when cancelling copy
 * Xuan Chen (IBM) - [api] SystemTarHandler has inconsistent API
 * Johnson Ma (Wind River) - [195402][api] Add tar.gz archive support
 * Xuan Chen (IBM) - [224576] [api] Inconsistent boolean return values in SystemTarHandler API
 * Martin Oberhuber (Wind River) - [199854][api] Improve error reporting for archive handlers
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.archiveutils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.rse.internal.services.clientserver.archiveutils.ITarConstants;
import org.eclipse.rse.internal.services.clientserver.archiveutils.SystemArchiveUtil;
import org.eclipse.rse.services.clientserver.IClientServerConstants;
import org.eclipse.rse.services.clientserver.ISystemFileTypes;
import org.eclipse.rse.services.clientserver.ISystemOperationMonitor;
import org.eclipse.rse.services.clientserver.SystemReentrantMutex;
import org.eclipse.rse.services.clientserver.java.BasicClassFileParser;
import org.eclipse.rse.services.clientserver.messages.SystemLockTimeoutException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationFailedException;
import org.eclipse.rse.services.clientserver.messages.SystemUnexpectedErrorException;
import org.eclipse.rse.services.clientserver.messages.SystemUnsupportedOperationException;
import org.eclipse.rse.services.clientserver.search.SystemSearchLineMatch;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatchLocator;
import org.eclipse.rse.services.clientserver.search.SystemSearchStringMatcher;


/**
 * This class deals with tar files.
 */
public class SystemTarHandler implements ISystemArchiveHandler {

	protected File file;
	protected long modTimeDuringCache;
	protected VirtualFileSystem vfs;
	/** @since 3.0 */
	protected SystemReentrantMutex _mutex;

	/**
	 * This class represents a virtual file system. A virtual file system is simply a data structure that
	 * helps manage the contents of an archive file. It provides services that a handler can use.
	 */
	private class VirtualFileSystem {

		private VirtualFileNode rootNode;

		/**
		 * Constructor for the virtual file system.
		 * @param root the root entry.
		 */
		public VirtualFileSystem(VirtualChild root) {
			this.rootNode = new VirtualFileNode(root);
		}

		/**
		 * Adds the entry to the tree according to its full path. Creates the parents
		 * of the entry if they don't exist.
		 * If the entry already exists in the tree, it is overwritten.
		 * @param entry the entry to be added to the tree.
		 */
		public void addEntry(VirtualChild entry) {
			addEntry(entry, true);
		}

		/**
		 * Adds the entry to the tree according to its full path. Creates the parents
		 * of the entry if they don't exist.
		 * @param entry the entry to be added to the tree.
		 * @param replace whether to replace if an entry with the same path
		 *        already exists in the tree.
		 */
		public void addEntry(VirtualChild entry, boolean replace) {

			String path = entry.fullName;

			VirtualFileNode parentNode = rootNode;

			int idx = path.indexOf("/"); //$NON-NLS-1$
			String name = path;

			String segPath = ""; //$NON-NLS-1$

			// ensure each segment exists or is created if it does not exist
			while (idx > 0) {
				name = path.substring(0, idx);
				path = path.substring(idx+1);

				segPath = segPath + name + "/"; //$NON-NLS-1$

				boolean exists = parentNode.childExists(name);

				// only create new parent if it does not already exist
				if (!exists) {
					VirtualChild child = new VirtualChild(SystemTarHandler.this, segPath);
					child.isDirectory = true;
					parentNode.addChild(name, new VirtualFileNode(child), true);
				}

				// the new parent is the child (which may have been created)
				parentNode = parentNode.getChild(name);

				idx = path.indexOf("/"); //$NON-NLS-1$
			}

			parentNode.addChild(path, new VirtualFileNode(entry), replace);
		}

		/**
		 * Removes the entry from the tree.
		 * @param entry the entry to be removed from the tree.
		 * @return the removed virtual child, or <code>null</code> if the entry
		 *         does not exist.
		 */
		public VirtualChild removeEntry(VirtualChild entry) {
			return removeEntry(entry.fullName);
		}

		/**
		 * Removes the entry with the given path from the tree.
		 * @param path path of the entry to be removed from the tree.
		 * @return the removed virtual child, or <code>null</code> if the entry
		 *         does not exist.
		 */
		public VirtualChild removeEntry(String path) {

			// strip out trailing separator
			if (path.charAt(path.length()-1) == '/') {
				path = path.substring(0, path.length()-1);
			}

			// get the parent node
			VirtualFileNode parent = getParentNode(path);

			// get the name of the entry
			String name = null;

			int idx = path.lastIndexOf('/');

			if (idx == -1) {
				name = path;
			}
			else {
				name = path.substring(idx+1);
			}

			// remove the entry from the parent
			VirtualFileNode removedChild = parent.removeChild(name);

			if (removedChild == null) {
				return null;
			}
			else {
				return removedChild.getEntry();
			}
		}

		/**
		 * Gets an entry from the given path.
		 * @param path the path of the entry.
		 * @return the entry, or <code>null</code> if the entry does not exist.
		 */
		public VirtualChild getEntry(String path) {
			VirtualFileNode node = getNode(path);

			if (node == null) {
				return null;
			}
			else {
				return node.getEntry();
			}
		}

		/**
		 * Returns an array of children of the given entry.
		 * @param entry the parent entry.
		 * @return an array of children, or an empty array if none exists. Returns <code>null</code>
		 * 		   if the parent entry isn't a directory.
		 */
		public VirtualChild[] getChildren(VirtualChild entry) {
			return getChildren(entry.fullName);
		}

		/**
		 * Returns an array of children folders of the given entry.
		 * @param entry the parent entry.
		 * @return an array of children, or an empty array if none exists. Returns <code>null</code>
		 * 		   if the parent entry isn't a directory.
		 */
		public VirtualChild[] getChildrenFolders(VirtualChild entry) {
			return getChildrenFolders(entry.fullName);
		}

		/**
		 * Returns an array of children of the entry with the given path.
		 * @param path of the parent entry, or "" to indicate the root entry.
		 * @return an array of children, or an empty array if none exists, or if the entry is not a directory.
		 */
		public VirtualChild[] getChildren(String path) {
			VirtualFileNode node = getNode(path);

			if (node == null) {
				return new VirtualChild[0];
			}

			VirtualFileNode[] childNodes = node.getChildren();
			VirtualChild[] children = new VirtualChild[childNodes.length];

			for (int i = 0; i < children.length; i++) {
				children[i] = childNodes[i].getEntry();
			}

			return children;
		}

		/**
		 * Returns an array of children folders of the entry with the given path.
		 * @param path of the parent entry, or "" to indicate the root entry.
		 * @return an array of children, or an empty array if none exists, or if the entry is not a directory.
		 */
		public VirtualChild[] getChildrenFolders(String path) {
			VirtualFileNode node = getNode(path);
			VirtualFileNode[] childNodes = node.getChildrenFolders();
			VirtualChild[] children = new VirtualChild[childNodes.length];

			for (int i = 0; i < children.length; i++) {
				children[i] = childNodes[i].getEntry();
			}

			return children;
		}

		/**
		 * Gets the parent entry.
		 * @param entry the entry whose parent we want.
		 * @return the parent of the entry, or <code>null</code> if the parent does not exist.
		 */
		public VirtualChild getParent(VirtualChild entry) {
			VirtualFileNode node = getParentNode(entry.fullName);

			if (node == null) {
				return null;
			}
			else {
				return node.getEntry();
			}
		}

		/**
		 * Returns the parent node for the entry with the given path.
		 * @param path the path of the entry whose parent we want.
		 * @return the node representing the parent entry, or <code>null</code> if the parent
		 *         node doesn't exist.
		 */
		private VirtualFileNode getParentNode(String path) {

			// strip out trailing separator
			if (path.charAt(path.length()-1) == '/') {
				path = path.substring(0, path.length()-1);
			}

			int idx = path.lastIndexOf('/');

			if (idx == -1) {
				return rootNode;
			}
			else {
				return getNode(path.substring(0, idx));
			}
		}

		/**
		 * Returns the node representing the entry with the given path.
		 * @param path the path of the entry, or <code>""</code> to indicate the root node.
		 * @return the node at the given path, or <code>null</code> if no such node exists.
		 */
		private VirtualFileNode getNode(String path) {

			if (path.equals("")) { //$NON-NLS-1$
				return rootNode;
			}

			// strip out trailing separator
			if (path.charAt(path.length()-1) == '/') {
				path = path.substring(0, path.length()-1);
			}

			int idx = 0;
			int jdx = 0;
			VirtualFileNode tempNode = rootNode;
			boolean done = false;

			while (true) {
				jdx = path.indexOf('/', idx);

				if (jdx == -1) {
					jdx = path.length();
					done = true;
				}

				String tempName = path.substring(idx, jdx);
				tempNode = tempNode.getChild(tempName);

				if (tempNode == null) {
					return null;
				}

				if (!done) {
					idx = jdx + 1;
				}
				else {
					break;
				}
			}

			return tempNode;
		}
	}

	/**
	 * This class represents a node in the tree.
	 */
	private class VirtualFileNode {

		private static final int MODE_ALL = 1;
		private static final int MODE_FILES_ONLY = 2;
		private static final int MODE_FOLDERS_ONLY = 3;

		private VirtualChild entry;
		private HashMap map;
		private boolean isDir;

		/**
		 * Constructor for the virtual file node.
		 * @param entry the entry that this node represents.
		 */
		public VirtualFileNode(VirtualChild entry) {
			this.entry = entry;
			this.isDir = entry.isDirectory;
			this.map = new HashMap();
		}

		/**
		 * Returns the entry represented by this node.
		 * @return the entry represented by this node.
		 */
		public VirtualChild getEntry() {
			return entry;
		}

		/**
		 * Returns whether the node represents a directory or a file.
		 * @return <code>true</code> if the node represents a directory, <code>false</code> otherwise;
		 */
		public boolean isDir() {
			return isDir;
		}

		/**
		 * Adds a child if this node is a directory.
		 * @param childName the name with which to identify the child.
		 * @param child the child.
		 * @param replace <code>true</code> to replace an existing child with the same
		 *        name (if any), <code>false</code> otherwise. If <code>true</code>, and there
		 * 		  is an existing child that is a directory, then its children will be added to
		 * 	      to the new child.
		 */
		public void addChild(String childName, VirtualFileNode child, boolean replace) {

			if (isDir) {

				// if replace is true, replace an existing node (if any) with this one
				// note that is a
				if (replace) {

					// get the existing child
					VirtualFileNode oldChild = getChild(childName);

					// if there is an existing child which is a directory, and we want to replace it
					// with a directory
					if (oldChild != null && oldChild.isDir() && child.isDir()) {
						Iterator iter = oldChild.getChildrenNames();

						while (iter.hasNext()) {
							String name = (String)(iter.next());
							VirtualFileNode grandChild = oldChild.getChild(name);
							child.addChild(name, grandChild, true);
						}
					}

					map.put(childName, child);
				}
				// otherwise first check if it already exists, and only add if it doesn't
				else {
					boolean exists = childExists(childName);

					if (!exists) {
						map.put(childName, child);
					}
				}
			}
		}

		/**
		 * Removes the child with the given name only if the node is a directory.
		 * @param childName the name of the child.
		 * @return the child that was removed, or <code>null</code> if a child with
		 * 		   the given name wasn't found or this node isn't a directory.
		 */
		public VirtualFileNode removeChild(String childName) {

			if (isDir) {
				return (VirtualFileNode)map.remove(childName);
			}
			else {
				return null;
			}
		}

		/**
		 * Gets the child with the given name.
		 * @param childName the name of the child.
		 * @return the child with the given name, or <code>null</code> if a child with
		 * 		   the given name wasn't found or this node isn't a directory.
		 */
		public VirtualFileNode getChild(String childName) {

			if (isDir) {
				return (VirtualFileNode)map.get(childName);
			}
			else {
				return null;
			}
		}

		/**
		 * Returns an array of children.
		 * @return an array of children, or an empty array if none exists, or if the node is not a directory.
		 */
		public VirtualFileNode[] getChildren() {
			return getChildren(MODE_ALL);
		}

		/**
		 * Returns an array of children that are folders.
		 * @return an array of children, or an empty array if none exists, or if the node is not a directory.
		 */
		public VirtualFileNode[] getChildrenFolders() {
			return getChildren(MODE_FOLDERS_ONLY);
		}

		/**
		 * Returns an array of children.
		 * @param mode the mode. One of <code>MODE_ALL</code>, <code>MODE_FILES_ONLY</code> and <code>MODE_FOLDERS_ONLY</code>.
		 * @return an array of children, or an empty array if none exists, or if the node is not a directory.
		 */
		public VirtualFileNode[] getChildren(int mode) {

			if (isDir) {

				// if we want all children (i.e. files and folders), then this
				// is probably quicker than getting each value and casting
				if (mode == MODE_ALL) {
					int num = map.size();
					VirtualFileNode[] children = new VirtualFileNode[num];
					map.values().toArray(children);
					return children;
				}
				// either we want only files or only folders
				else {
					Vector v = new Vector();
					Iterator iter = map.values().iterator();

					while (iter.hasNext()) {
						VirtualFileNode node = (VirtualFileNode)iter.next();
						boolean isDir = node.isDir();

						if (mode == MODE_FILES_ONLY && !isDir) {
							v.add(node);
						}
						else if (mode == MODE_FOLDERS_ONLY && isDir) {
							v.add(node);
						}
					}

					VirtualFileNode[] children = new VirtualFileNode[v.size()];
					v.toArray(children);
					return children;
				}
			}
			else {
				return new VirtualFileNode[0];
			}
		}

		/**
		 * Returns whether the child with the given name exists.
		 * @param childName the name of the child.
		 * @return <code>true</code> if the child exists, <code>false</code>otherwise.
		 *         Returns <code>false</code> if this node isn't a directory.
		 */
		public boolean childExists(String childName) {

			if (isDir) {
				return map.containsKey(childName);
			}
			else {
				return false;
			}
		}

		/**
		 * Returns an iterator of the names of all the children of this node.
		 * @return a iterator of all the names of the children, or <code>null</code> if the node
		 *         is not a directory.
		 */
		public Iterator getChildrenNames() {

			if (isDir) {
				return map.keySet().iterator();
			}
			else {
				return null;
			}
		}
	}

	/**
	 * Constructor for handler. Calls <code>init</code>.
	 * @param file the tar file.
	 */
	public SystemTarHandler(File file) throws IOException {
		super();
		init(file);
		createCache();
		modTimeDuringCache = file.lastModified();
		_mutex = new SystemReentrantMutex();
	}

	/**
	 * Initializes the handler from the given file and does caching.
	 * @param file the file to handle
	 * @throws IOException in case of error during initialization
	 *     (can be thrown by overriding methods)
	 */
	protected void init(File file) throws IOException {
		this.file = file;
	}

	/**
	 * Reads the contents of the tar file, and caches the entries.
	 */
	protected void createCache() {

		TarFile tarFile = getTarFile();

		Enumeration entries = tarFile.entries();

		VirtualChild root = new VirtualChild(this);
		root.isDirectory = true;

		vfs = new VirtualFileSystem(root);

		while (entries.hasMoreElements()) {
			TarEntry entry = (TarEntry)entries.nextElement();
			VirtualChild child = getVirtualChild(entry);
			vfs.addEntry(child);
		}
	}

	/**
	 * Update the virtual file system tree. If newOldName HashMap is input, use
	 * the old name in the HashMap to do the search in virtual file system tree.
	 *
	 * @since 3.0
	 */
	protected void updateTree(HashMap newOldNames)
	{
		TarFile tarFile = getTarFile();
		Enumeration entries = tarFile.entries();
		while (entries.hasMoreElements())
		{
			TarEntry entry = (TarEntry)entries.nextElement();
			String searchName = null;
			String entryName = entry.getName();
			VirtualChild child = null;
			if (newOldNames != null && newOldNames.containsKey(entryName))
			{
				searchName = (String)newOldNames.get(entryName);  //use its old name
			}
			else
			{
				searchName = entryName;
			}

			child = vfs.getEntry(searchName);

			if (null == child)
			{
				child = getVirtualChild(entry);
			}
			else
			{
				vfs.removeEntry(searchName);
				child = updateVirtualChild(entry, child);
			}

			vfs.addEntry(child);
		}
	}

	/**
	 * Gets a tar file from the underlying file.
	 *
	 * @return the tar file, or <code>null</code> if the tar file does not
	 *         exist.
	 * @since 3.0 TarFile moved to API
	 */
	protected TarFile getTarFile() {

		TarFile tarFile = null;

		try {
			tarFile = new TarFile(file);
		}
		catch (IOException e) {
			// TODO: log error
		}

		return tarFile;
	}

	/**
	 * Updates the cache if the tar file has changed since the last time
	 * we cached. Will not change the cache if the tar file hasn't been
	 * updated. Other methods should call this method before performing
	 * any operations on the cache and the underlying tar file.
	 */
	protected void updateCache() throws IOException {
		File newFile = new File(file.getAbsolutePath());
		long modTime = newFile.lastModified();

		// if the modified time of the file is not the same as the modified time before last
		// cache, then recreate cache
		if (modTime != modTimeDuringCache) {
			// reinitialize
			init(newFile);
			updateTree(null);
			modTimeDuringCache = newFile.lastModified();
		}
	}

	/**
	 * {@inheritDoc}
	 * @since 3.0
	 */
	public VirtualChild[] getVirtualChildrenList(ISystemOperationMonitor archiveOperationMonitor) {

		// this method does not read from cache
		Vector v = new Vector();

		TarFile tarFile = getTarFile();
		Enumeration entries = tarFile.entries();

		while (entries.hasMoreElements()) {
			TarEntry entry = (TarEntry)entries.nextElement();
			VirtualChild child = new VirtualChild(this, entry.getName());
			child.isDirectory = entry.isDirectory();
			v.add(child);
		}

		int numOfChildren = v.size();

		VirtualChild[] children = new VirtualChild[numOfChildren];

		for (int i = 0; i < numOfChildren; i++) {
			children[i] = (VirtualChild)v.get(i);
		}

		return children;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public VirtualChild[] getVirtualChildrenList(String parent, ISystemOperationMonitor archiveOperationMonitor) {
		parent = ArchiveHandlerManager.cleanUpVirtualPath(parent);

		// this method does not read from cache
		Vector v = new Vector();

		TarFile tarFile = getTarFile();
		Enumeration entries = tarFile.entries();

		while (entries.hasMoreElements()) {
			TarEntry entry = (TarEntry)entries.nextElement();

			// only add those entries that have names that begin with the parent name
			// also check that the entry name isn't just the parent name + '/' (i.e. still the same
			// as the parent)
			String entryName = entry.getName();
			String parentNameEndWithSlash = parent + "/";  //$NON-NLS-1$
			if (entryName.startsWith(parentNameEndWithSlash) && !entryName.equals(parentNameEndWithSlash)) {
				VirtualChild child = new VirtualChild(this, entryName);
				child.isDirectory = entry.isDirectory();
				v.add(child);
			}
		}

		int numOfChildren = v.size();

		VirtualChild[] children = new VirtualChild[numOfChildren];

		for (int i = 0; i < numOfChildren; i++) {
			children[i] = (VirtualChild)v.get(i);
		}

		return children;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public VirtualChild[] getVirtualChildren(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) {

		try {
			updateCache();
		}
		catch (IOException e) {
			// TODO: log error
			return new VirtualChild[0];
		}

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		return vfs.getChildren(fullVirtualName);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public VirtualChild[] getVirtualChildFolders(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) {

		try {
			updateCache();
		}
		catch (IOException e) {
			// TODO: log error
			return new VirtualChild[0];
		}

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		return vfs.getChildrenFolders(fullVirtualName);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public VirtualChild getVirtualFile(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) {

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		if (fullVirtualName == null || fullVirtualName.equals("")) { //$NON-NLS-1$
			return new VirtualChild(this);
		}

		try {
			updateCache();
		}
		catch (IOException e) {
			// TODO: log error
			return new VirtualChild(this, fullVirtualName);
		}

		VirtualChild entry = vfs.getEntry(fullVirtualName);

		// if entry is null, then create a new virtual child object
		// for which exists will return false
		if (entry == null) {
			entry = new VirtualChild(this, fullVirtualName);
		}

		return entry;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public boolean exists(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) {

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		if (fullVirtualName == null || fullVirtualName.equals("")) { //$NON-NLS-1$
			return false;
		}

		try {
			updateCache();
		}
		catch (IOException e) {
			// TODO: log error
			return false;
		}

		VirtualChild child = vfs.getEntry(fullVirtualName);

		if (child != null) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getArchive()
	 */
	public File getArchive() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getTimeStampFor(java.lang.String)
	 */
	public long getTimeStampFor(String fullVirtualName) {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		// get the entry with that name
		TarEntry entry = getTarFile().getEntry(fullVirtualName);

		// if the entry exists, return its last modified time
		if (entry != null) {
			return entry.getModificationTime();
		}
		// otherwise return the last modified time of the file
		// TODO: is this correct?
		else {
			return file.lastModified();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getSizeFor(java.lang.String)
	 */
	public long getSizeFor(String fullVirtualName) {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		// get the entry with that name
		TarEntry entry = getTarFile().getEntry(fullVirtualName);

		// if the entry exists, return the size
		if (entry != null) {
			return entry.getSize();
		}
		// otherwise return 0
		// TODO: is this correct?
		else {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualFile(String fullVirtualName, File destination, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		TarEntry entry = null;

		InputStream inStream = null;
		OutputStream outStream = null;
		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{
				entry = getTarFile().getEntry(fullVirtualName);
				updateCache();

				// if the entry is a directory, simply create the destination and set the last modified time to
				// the entry's last modified time
				if (entry.isDirectory()) {

					// if destination exists, then delete it
					if (destination.exists()) {
						destination.delete();
					}

					// create destination directory, and set the last modified time to
					// the entry's last modified time
					destination.mkdirs();
					destination.setLastModified(entry.getModificationTime());
					return;
				}

				inStream = getTarFile().getInputStream(entry);

				if (inStream == null) {
					destination.setLastModified(entry.getModificationTime());
					throw new SystemUnexpectedErrorException(IClientServerConstants.PLUGIN_ID);
				}
				//Need to make sure destination file exists.
				if (!destination.exists())
				{
					File parentFile = destination.getParentFile();
					if (!parentFile.exists())
						parentFile.mkdirs();
					destination.createNewFile();
				}

				outStream = new FileOutputStream(destination);

				byte[] buf = new byte[ITarConstants.BLOCK_SIZE];
				int numRead = inStream.read(buf);

				while (numRead > 0) {
					outStream.write(buf, 0, numRead);
					numRead = inStream.read(buf);
				}
			}
		}
		catch (IOException e) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
		finally {

			try {

				if (outStream != null) {
					outStream.close();
				}

				if (inStream != null) {
					inStream.close();
				}


				// finished creating and writing to the file, so now set the last modified time
				// to the entry's last modified time
				if (entry != null)
				{
					destination.setLastModified(entry.getModificationTime());
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			releaseMutex(mutexLockStatus);

		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualDirectory(String fullVirtualName, File destinationParent, ISystemOperationMonitor archiveOperationMonitor)
			throws SystemMessageException {
		extractVirtualDirectory(fullVirtualName, destinationParent, (File) null, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualDirectory(String fullVirtualName, File destinationParent, File destination, ISystemOperationMonitor archiveOperationMonitor)
			throws SystemMessageException {

		// if the destination directory doesn't exist, create it
		if (!destinationParent.exists()) {

			if (!destinationParent.mkdirs()) {
				throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Create folder " + destinationParent); //$NON-NLS-1$
			}
		}
		// otherwise if the destination directory does exist, but is not a directory, then quit
		else if (!destinationParent.isDirectory()) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "No folder: " + destinationParent); //$NON-NLS-1$
		}

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{

				updateCache();

				VirtualChild dir = vfs.getEntry(fullVirtualName);

				if (dir == null || !dir.isDirectory) {
					throw new SystemUnexpectedErrorException(IClientServerConstants.PLUGIN_ID);
				}

				if (destination == null) {

					if (fullVirtualName.equals("")) { //$NON-NLS-1$
						destination = destinationParent;
					}
					else {
						destination = new File(destinationParent, dir.name);
					}
				}

				File topDir = destination;
				String topDirPath = topDir.getAbsolutePath();

				// TODO: why are we checking that destination and destination parent are not equal?
				if (!destination.equals(destinationParent)) {

					if (destination.isFile() && destination.exists()) {
						SystemArchiveUtil.delete(destination);
					}

					destination.mkdirs();
				}

				// if the directory does not exist, try to create it
				if (!topDir.exists() && !topDir.mkdirs()) {
					throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Create folder " + topDir); //$NON-NLS-1$
				}
				else {
					extractVirtualFile(fullVirtualName, topDir, archiveOperationMonitor);
				}

				// get the children of this directory
				VirtualChild[] children = vfs.getChildren(fullVirtualName);

				for (int i = 0; i < children.length; i++) {
					VirtualChild tempChild = children[i];
					String childPath = topDirPath;
					if (!tempChild.isDirectory)
					{
						childPath = topDirPath + File.separator + tempChild.name;
					}
					File childFile = new File(childPath);

					// if the child is a directory, then we need to extract it and its children
					if (tempChild.isDirectory) {

						// and now extract its children
						extractVirtualDirectory(tempChild.fullName, childFile, (File) null, archiveOperationMonitor);
					}
					// otherwise if the child is a file, simply extract it
					else {
						extractVirtualFile(tempChild.fullName, childFile, archiveOperationMonitor);
					}
				}
			}
		}
		catch (IOException e)
		{
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(File file, String virtualPath, String name, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);

		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{
				if (!file.isDirectory()) {

					// if it exists, call replace
					String fullVirtualName = getFullVirtualName(virtualPath, name);
					if (exists(fullVirtualName, archiveOperationMonitor)) {
						replace(fullVirtualName, file, name, archiveOperationMonitor);
						setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
						return;
					}
					else {
						File[] files = new File[1];
						files[0] = file;
						String[] names = new String[1];
						names[0] = name;
						add(files, virtualPath, names, archiveOperationMonitor);
						setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
						return;
					}
				}
				else {
					Vector children = new Vector();
					listAllFiles(file, children);
					int numOfChildren = children.size();
					File[] sources = new File[numOfChildren + 1];
					String[] newNames = new String[numOfChildren + 1];
					int charsToTrim = file.getParentFile().getAbsolutePath().length() + 1;
					for (int i = 0; i < numOfChildren; i++)
					{
						sources[i] = (File)children.get(i);
						newNames[i] = sources[i].getAbsolutePath().substring(charsToTrim);
						newNames[i] = newNames[i].replace('\\','/');

						if (sources[i].isDirectory() && !newNames[i].endsWith("/")) { //$NON-NLS-1$
							newNames[i] = newNames[i] + "/"; //$NON-NLS-1$
						}
					}

					sources[numOfChildren] = file;
					newNames[numOfChildren] = name;

					if  (!newNames[numOfChildren].endsWith("/")) { //$NON-NLS-1$
						newNames[numOfChildren] = newNames[numOfChildren] + "/"; //$NON-NLS-1$
					}

					add(sources, virtualPath, newNames, archiveOperationMonitor);
					setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
					return;
				}
			}
		}
		catch (Exception e)
		{
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
		setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
		throw new SystemUnexpectedErrorException(IClientServerConstants.PLUGIN_ID);
	}

	/**
	 * Helper method. Populates given List <code>found</code> with a collapsed
	 * list of all nodes in the subtree of the file system rooted at
	 * <code>parent</code>.
	 *
	 * @since 3.0 Vector changed into List
	 */
	public void listAllFiles(File parent, List found) {

		File[] children = parent.listFiles();

		for (int i = 0; i < children.length; i++) {

			if (children[i].isDirectory()) {
				listAllFiles(children[i], found);
			}

			found.add(children[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(File[] files, String virtualPath, String[] names, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		File outputTempFile = null;
		TarOutputStream outStream = null;

		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{

				updateCache();

				virtualPath = ArchiveHandlerManager.cleanUpVirtualPath(virtualPath);

				int numFiles = files.length;

				for (int i = 0; i < numFiles; i++) {

					if (!files[i].exists() || !files[i].canRead()) {
						setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
						throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Cannot read: " + files[i]); //$NON-NLS-1$
					}

					// if the entry already exists, then we should do a replace
					// TODO (KM): should we simply replace and return?
					// I think we should check each entry and replace or create for each one
					String fullVirtualName = getFullVirtualName(virtualPath, names[i]);
					if (exists(fullVirtualName, archiveOperationMonitor)) {

						replace(fullVirtualName, files[i], names[i], archiveOperationMonitor);
						setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
						return;
					}
				}

				// open a new temp file which will be our destination for the new tar file
				outputTempFile = new File(file.getAbsolutePath() + "temp"); //$NON-NLS-1$
				outStream = getTarOutputStream(outputTempFile);

				// get all the entries in the current tar
				VirtualChild[] children = getVirtualChildrenList(archiveOperationMonitor);

				// if it is an empty temp file, no need to recreate it
				if (children.length != 0) {
					boolean ok = createTar(children, outStream, (HashSet)null, archiveOperationMonitor);
					if (!ok)
					{
						//cancelled
						outStream.close();
						if (outputTempFile != null)
						{
							outputTempFile.delete();
						}
						throw new SystemOperationCancelledException();
					}
				}
				VirtualChild[] newEntriesAdded = new VirtualChild[numFiles];
				// for each new file to add
				for (int i = 0; i < numFiles; i++) {

					if (archiveOperationMonitor != null && archiveOperationMonitor.isCancelled())
					{
						outStream.close();
						if (outputTempFile != null)
						{
							outputTempFile.delete();
						}
						throw new SystemOperationCancelledException();
					}
					String childVirtualPath = virtualPath + "/" + names[i]; //$NON-NLS-1$

					TarEntry newEntry = createTarEntry(files[i], childVirtualPath);

					// append the additional entry to the tar file
					appendFile(files[i], newEntry, outStream);

					// add the new entry to the cache, so that the cache is updated
					VirtualChild temp = getVirtualChild(newEntry);
					newEntriesAdded[i] = temp;
				}


				// close output stream
				outStream.close();

				// replace the current tar file with the new one, and do not update cache since
				// we just did
				replaceFile(outputTempFile, false);

				//Also need to add the new entries into VFS
				for (int i = 0; i < numFiles; i++)
				{
					vfs.addEntry(newEntriesAdded[i]);
				}
			}

		}
		catch (IOException e) {
			// close output stream
			if (outStream != null)
			{
				try
				{
					outStream.close();
				}
				catch (Exception exp)
				{
					exp.printStackTrace();
				}
			}
			if (outputTempFile != null)
			{
				outputTempFile.delete();
			}
			setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
		setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
	}

	/**
	 * Create a tar file from the given virtual child objects, using the given
	 * output stream and omitting the children in the given set.
	 *
	 * @param children an array of virtual children from which to create a tar
	 * 		file.
	 * @param outStream the tar output stream to use.
	 * @param omitChildren the set of names for children that should be omitted
	 * 		from the given array of virtual children.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return <code>true</code> if the operation completes successfully, or
	 * 	<code>false</code> if it has been cancelled.
	 * @throws IOException if an I/O exception occurs.
	 * @since org.eclipse.rse.services 3.0
	 */
	protected boolean createTar(VirtualChild[] children, TarOutputStream outStream, HashSet omitChildren, ISystemOperationMonitor archiveOperationMonitor) throws IOException {

		// TODO: if all children are to be deleted, we leave the tar file with a dummy entry
		if (omitChildren != null && children.length == omitChildren.size()) {
			return true;
		}

		TarFile tarFile = getTarFile();

		// go through each child
		for (int i = 0; i < children.length; i++) {

			if (archiveOperationMonitor != null && archiveOperationMonitor.isCancelled())
			{
				//the operation has been cancelled
				return false;
			}
			// if entry name is in the omit set, then do not include it
			if (omitChildren != null && omitChildren.contains(children[i].fullName)) {
				continue;
			}

			// if child is a directory, then just add an entry for it
			// there is no data
			if (children[i].isDirectory) {

				// include a '/' at the end, since it is a directory
				TarEntry nextEntry = tarFile.getEntry(children[i].fullName + "/"); //$NON-NLS-1$

				// put the entry
				outStream.putNextEntry(nextEntry);

				// close the entry
				outStream.closeEntry();
			}
			// otherwise child is a file, so add an entry for it
			// and then add data (i.e. file contents).
			else {

				TarEntry nextEntry = tarFile.getEntry(children[i].fullName);

				// get the input stream for the file contents
				InputStream inStream = tarFile.getInputStream(nextEntry);

				// put the entry
				outStream.putNextEntry(nextEntry);

				// write data
				byte[] buf = new byte[ITarConstants.BLOCK_SIZE];
				int numRead = inStream.read(buf);

				while (numRead > 0) {
					outStream.write(buf, 0, numRead);
					numRead = inStream.read(buf);
				}

				// close input stream
				inStream.close();

				// close entry, but do not close the output stream
				outStream.closeEntry();
			}
		}
		return true;
	}

	/**
	 * Appends a file to a tar output stream, using the given entry that
	 * represents the file.
	 *
	 * @param file the file to be appended to the tar output stream.
	 * @param entry the entry which represents the file.
	 * @param outStream the tar output stream.
	 * @throws IOException if an I/O error occurs.
	 * @since 3.0 TarEntry moved into API
	 */
	protected void appendFile(File file, TarEntry entry, TarOutputStream outStream) throws IOException {

		// put the next entry in the output stream
		outStream.putNextEntry(entry);

		// now write data if it is a file
		// there is no data for folders
		if (!file.isDirectory()) {
			BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));

			byte[] buf = new byte[ITarConstants.BLOCK_SIZE];
			int numRead = inStream.read(buf);

			while (numRead > 0) {
				outStream.write(buf, 0, numRead);
				numRead = inStream.read(buf);
			}

			// close the input stream
			inStream.close();
		}

		// close the entry
		outStream.closeEntry();
	}

	/**
	 * Creates a tar entry for a file with the given virtual path. The entry
	 * will contain the size and last modified time of the file. The entry's
	 * checksum will be calculated.
	 *
	 * @param file the file for which to create a tar entry.
	 * @param virtualPath the virtual path for the entry.
	 * @return the tar entry representing the given file.
	 * @since 3.0 TarEntry moved into API
	 */
	protected TarEntry createTarEntry(File file, String virtualPath) {

		String fullName = virtualPath;

		// if directory, end with a '/'
		if (file.isDirectory()) {

			if (!fullName.endsWith("/")) { //$NON-NLS-1$
				fullName = fullName + "/"; //$NON-NLS-1$
			}
		}

		// strip out leading '/'
		// TODO (KM): Why?
		if (fullName.startsWith("/")) { //$NON-NLS-1$
			fullName = fullName.substring(1);
		}

		// create a new entry and set its size and last modified time
		TarEntry entry = new TarEntry(fullName);

		// set the size if the file is not a directory
		if (!file.isDirectory()) {
			long size = file.length();
			entry.setSize(size);
		}
		else {
			entry.setSize(0);
		}

		// set modified time
		long lastModified = file.lastModified();
		entry.setModificationTime(lastModified);

		// set the user name
		String userName = System.getProperty("user.name"); //$NON-NLS-1$

		if (userName != null) {
			entry.setUserName(userName);
		}

		// set user permissions
		boolean canRead = file.canRead();
		boolean canWrite = file.canWrite();

		// getting execute permission is a bit tricky
		// need to go through security manager
		boolean canExecute = false;

		// first get the system security manager
		SecurityManager sm = System.getSecurityManager();

		// if there is no security manager then create a new one
		if (sm == null) {
			sm = new SecurityManager();
		}

		try {

			// if security manager successfully created, check permission

			// create a file permission to check execute
			FilePermission permission = new FilePermission(file.getAbsolutePath(), "execute"); //$NON-NLS-1$

			// this call will throw a SecurityException if permission does not exist
			sm.checkPermission(permission);
			canExecute = true;

		}
		catch(SecurityException e) {
			canExecute = false;
		}

		entry.setUserMode(canRead, canWrite, canExecute);

		// calculate checksum
		entry.calculateChecksum();

		return entry;
	}

	/**
	 * Changes a tar entry according to the file information and given path. The
	 * given path will be the new name of the entry. The size and last modified
	 * fields will be changed to the file's size and last modified time. The
	 * entry's checksum will be calculated.
	 *
	 * @param entry the entry that needs to be changed.
	 * @param file the file for which the tar entry is being changed.
	 * @param virtualPath the virtual path for the entry.
	 * @return the changed entry.
	 * @since 3.0 TarEntry moved into API
	 */
	protected TarEntry changeTarEntry(TarEntry entry, File file, String virtualPath) {

		// TODO (KM): This does not update the permissions in the entry according to the
		// file permissions on disk. Need to look at how to retrieve permissions for owner, group
		// and other, and then have to set these in the entry accordingly.

		String fullName = virtualPath;

		// if directory, end with a '/'
		if (file.isDirectory()) {

			if (!fullName.endsWith("/")) { //$NON-NLS-1$
				fullName = fullName + "/"; //$NON-NLS-1$
			}
		}

		// strip out leading '/'
		// TODO (KM): Why?
		if (fullName.startsWith("/")) { //$NON-NLS-1$
			fullName = fullName.substring(1);
		}

		// change entry name
		entry.setName(fullName);

		// update size field in entry
		if (!file.isDirectory()) {
			long size = file.length();
			entry.setSize(size);
		}
		else {
			entry.setSize(0);
		}

		// update last modified field in entry
		long lastModified = file.lastModified();
		entry.setModificationTime(lastModified);

		// calculate checksum
		entry.calculateChecksum();

		return entry;
	}

	/**
	 * Returns a virtual child given a tar entry.
	 *
	 * @param entry a tar entry.
	 * @return the virtual child that represents the tar entry.
	 * @since 3.0 TarEntry moved into API
	 */
	protected VirtualChild getVirtualChild(TarEntry entry) {
		VirtualChild child = new VirtualChild(this, entry.getName());
		child.isDirectory = entry.isDirectory();
		child.setComment("");  //$NON-NLS-1$
		child.setCompressedSize(entry.getSize());
		child.setCompressionMethod(""); //$NON-NLS-1$;
		child.setSize(entry.getSize());
		child.setTimeStamp(entry.getModificationTime());
		return child;
	}

	/**
	 * update a virtual child given a tar entry.
	 *
	 * @param entry a tar entry.
	 * @return the virtual child that represents the tar entry.
	 * @since 3.0 TarEntry moved into API
	 */
	protected VirtualChild updateVirtualChild(TarEntry entry, VirtualChild child) {
		child.renameTo(entry.getName());
		child.isDirectory = entry.isDirectory();
		child.setComment("");  //$NON-NLS-1$
		child.setCompressedSize(entry.getSize());
		child.setCompressionMethod(""); //$NON-NLS-1$;
		child.setSize(entry.getSize());
		child.setTimeStamp(entry.getModificationTime());
		return child;
	}

	/**
	 * Replaces the old tar file managed by the handler with the given file, and optionally update
	 * the cache.
	 * @param newFile the new tar file.
	 * @param updateCache <code>true</code> to update the cache, <code>false</code> otherwise.
	 * 					  Only specify <code>false</code> if the cache has already been updated to reflect the
	 *                    contents of this new file.
	 * @throws IOException if an I/O problem occurs.
	 */
	protected void replaceFile(File newFile, boolean updateCache) throws IOException {
		String name = file.getAbsolutePath();

		// create a temp file (in case something goes wrong)
		File tempFile = new File(name + ".old"); //$NON-NLS-1$

		// rename current file to tempFile
		file.renameTo(tempFile);

		// rename the new file to the file this handler manages
		newFile.renameTo(file);

		// reinitialize
		init(file);

		// if we do not want to update the cache, we set the last modified time during cache to
		// the modified time of the file, so when we call updateCache, it'll do nothing
		if (!updateCache) {
			modTimeDuringCache = file.lastModified();
		}

		// update cache if necessary
		updateCache();

		// delete the temporary file
		tempFile.delete();
	}



	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void replace(String fullVirtualName, File file, String name, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		// update our cache before accessing cache
		try {
			updateCache();
		}
		catch (IOException e) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}

		if (!file.exists() && !file.canRead()) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Cannot read " + file); //$NON-NLS-1$
		}

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		// if the virtual file does not exist, we actually want to add
		if (!exists(fullVirtualName, archiveOperationMonitor)) {
			add(file, fullVirtualName, name, archiveOperationMonitor);
			return;
		}

		try {

			// open a new temp file which will be our destination for the new tar file
			File outputTempFile = new File(getArchive().getAbsolutePath() + "temp"); //$NON-NLS-1$

			TarOutputStream outStream = getTarOutputStream(outputTempFile);

			// get all the entries
			VirtualChild[] children = getVirtualChildrenList(archiveOperationMonitor);

			// create a set of omissions
			HashSet omissions = new HashSet();

			// add the virtual file to be replaced
			omissions.add(fullVirtualName);

			boolean ok = createTar(children, outStream, omissions, archiveOperationMonitor);
			if (!ok)
			{
				outStream.close();
				if (outputTempFile != null)
				{
					outputTempFile.delete();
				}
				throw new SystemOperationCancelledException();
			}

			// now append the new file to the tar
			String parentVirtualPath = null;

			int i = fullVirtualName.lastIndexOf("/"); //$NON-NLS-1$

			// if the virtual name has no '/', then we will replace it with the
			// new name
			if (i == -1) {
				parentVirtualPath = ""; //$NON-NLS-1$
			}
			// otherwise, we get the parent path to which the new name will be appended
			else {
				parentVirtualPath = fullVirtualName.substring(0, i);
			}

			String virtualPath = parentVirtualPath + "/" + name; //$NON-NLS-1$

			// get the existing entry for the file
			TarFile tarFile = getTarFile();
			TarEntry entry = tarFile.getEntry(fullVirtualName);

			// update the entry with the file information
			entry = changeTarEntry(entry, file, virtualPath);

			// now append this entry to the output stream
			appendFile(file, entry, outStream);

			// remove old entry from cache
			//vfs.removeEntry(vfs.getEntry(fullVirtualName));

			// add the new entry to cache
			VirtualChild temp = updateVirtualChild(entry, vfs.getEntry(fullVirtualName));
			//vfs.addEntry(temp);

			// close output stream
			outStream.close();

			// replace the current tar file with the new one, and do not update cache since
			// we just did
			replaceFile(outputTempFile, false);
		}
		catch (IOException e) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public boolean delete(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException
	{
		boolean returnCode = doDelete(fullVirtualName, archiveOperationMonitor);
		setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
		return returnCode;
	}


		/**
	 * Delete a virtual object.
	 *
	 * @param fullVirtualName virtual path identifying the object
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return <code>true</code> if successful, <code>false</code> if the entry
	 * 	to delete did not exist (so this was a successful no-op).
	 * @throws SystemMessageException in case of an error
	 * @since org.eclipse.rse.services 3.0
	 */
	protected boolean doDelete(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		File outputTempFile = null;
		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{
				updateCache();
				fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
				VirtualChild child = getVirtualFile(fullVirtualName, archiveOperationMonitor);
				VirtualChild[] omitArray = new VirtualChild[0];

				// child does not exist, so quit
				if (!child.exists()) {
					return false;
				}

				// child is a directory, so get its children since we need to delete them as well
				if (child.isDirectory) {
					omitArray = getVirtualChildrenList(fullVirtualName, archiveOperationMonitor);
				}

				// open a new temp file which will be our destination for the new tar file
				outputTempFile = new File(file.getAbsolutePath() + "temp"); //$NON-NLS-1$
				TarOutputStream outStream = getTarOutputStream(outputTempFile);

				// get all the entries in the current tar
				VirtualChild[] children = getVirtualChildrenList(archiveOperationMonitor);

				// create a set to hold omissions
				HashSet omissions = new HashSet();

				// add the child to it
				omissions.add(child.fullName);

				// now go through array of children to be deleted
				// this will be of length 0 if the child is not a directory
				for (int i = 0; i < omitArray.length; i++) {
					omissions.add(omitArray[i].fullName);
				}

				boolean ok = createTar(children, outStream, omissions, archiveOperationMonitor);
				if (!ok)
				{
					outStream.close();
					if (outputTempFile != null)
					{
						outputTempFile.delete();
					}
					throw new SystemOperationCancelledException();
				}

				// delete the child from the cache (this will also delete its children if it
				// is a directory)
				vfs.removeEntry(child);

				// close output stream
				outStream.close();

				// replace the current tar file with the new one, and do not update cache since
				// we just did
				replaceFile(outputTempFile, false);
			} else {
				throw new SystemLockTimeoutException(IClientServerConstants.PLUGIN_ID);
			}
		}
		catch (IOException e) {
			if (!(outputTempFile == null)) outputTempFile.delete();
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Could not delete " + fullVirtualName, e); //$NON-NLS-1$
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void rename(String fullVirtualName, String newName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		int i = fullVirtualName.lastIndexOf("/"); //$NON-NLS-1$

		// if the original does not have any separator, simply rename it.
		if (i == -1)
		{
			fullRename(fullVirtualName, newName, archiveOperationMonitor);
		}
		// otherwise, get the parent path and append the new name to it.
		else
		{
			String fullNewName = fullVirtualName.substring(0, i+1) + newName;
			fullRename(fullVirtualName, fullNewName, archiveOperationMonitor);
		}
		setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void move(String fullVirtualName, String destinationVirtualPath, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		destinationVirtualPath = ArchiveHandlerManager.cleanUpVirtualPath(destinationVirtualPath);

		int i = fullVirtualName.lastIndexOf("/"); //$NON-NLS-1$

		// if the original does not have any separator, simply append it to the destination path.
		if (i == -1) {
			fullRename(fullVirtualName, destinationVirtualPath + "/" + fullVirtualName, archiveOperationMonitor); //$NON-NLS-1$
		}
		// otherwise, get the last segment (the name) and append that to the destination path.
		else {
			String name = fullVirtualName.substring(i);
			fullRename(fullVirtualName, destinationVirtualPath + name, archiveOperationMonitor);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void fullRename(String fullVirtualName, String newFullVirtualName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		File outputTempFile = null;
		// update our cache before accessing cache
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{

				fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
				newFullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(newFullVirtualName);
				VirtualChild child = getVirtualFile(fullVirtualName, archiveOperationMonitor);

				// if the virtual file to be renamed does not exist, then quit
				if (!child.exists()) {
					throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Not exists: " + child); //$NON-NLS-1$
				}

				// open a new temp file which will be our destination for the new tar file
				outputTempFile = new File(file.getAbsolutePath() + "temp"); //$NON-NLS-1$
				TarOutputStream outStream = getTarOutputStream(outputTempFile);

				// get all the entries
				VirtualChild[] children = getVirtualChildrenList(archiveOperationMonitor);

				// the rename list
				// a hashmap containing old name, new name associations for each
				// child that has to be renamed
				HashMap oldNewNames = new HashMap();
				// a hashmap containing new name, old name associations for each
				// child that has to be renamed
				HashMap newOldNames = new HashMap();

				// if the entry to rename is a directory, we need to rename all
				// its children entries
				if (child.isDirectory) {

					// add the entry itself to the rename list
					// include '/' in both the old name and the new name since it is a directory
					oldNewNames.put(fullVirtualName + "/", newFullVirtualName + "/"); //$NON-NLS-1$ //$NON-NLS-2$
					newOldNames.put(newFullVirtualName + "/", fullVirtualName + "/"); //$NON-NLS-1$ //$NON-NLS-2$

					// get all the children of the entry to be renamed
					VirtualChild[] childrenArray = getVirtualChildrenList(fullVirtualName, archiveOperationMonitor);

					// now we need to get the relative path of each child with respect to the virtual name
					// and append the relative path to the new virtual name
					for (int i = 0; i < childrenArray.length; i++) {

						int j = fullVirtualName.length();

						// get the relative path with respect to the virtual name
						String suffix = childrenArray[i].fullName.substring(j);

						// add the relative path to the new virtual name
						String newName = newFullVirtualName + suffix;

						// if a child is a directory, ensure that '/'s are added both for the old name
						// and the new name
						if (childrenArray[i].isDirectory) {
							oldNewNames.put(childrenArray[i].fullName + "/", newName + "/"); //$NON-NLS-1$ //$NON-NLS-2$
							newOldNames.put(newName + "/", childrenArray[i].fullName + "/"); //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							oldNewNames.put(childrenArray[i].fullName, newName);
							newOldNames.put(newName, childrenArray[i].fullName);
						}
					}
				}
				// otherwise entry is not a directory, so simply add it to the rename list
				else {
					oldNewNames.put(fullVirtualName, newFullVirtualName);
					newOldNames.put(newFullVirtualName, fullVirtualName);
				}

				// create tar with renamed entries
				boolean ok = createTar(children, outStream, oldNewNames, archiveOperationMonitor);
				if (!ok)
				{
					outStream.close();
					if (outputTempFile != null)
					{
						outputTempFile.delete();
					}
					throw new SystemOperationCancelledException();
				}


				// close the output stream
				outStream.close();

				// replace the current tar file with the new one, and force an update of the cache.
				// TODO: we force a fresh update of the cache because it is seemingly complicated
				// to do the delta upgrade of the cache. But investigate this, since it will
				// probably be more efficient
				replaceFile(outputTempFile, true);
				updateTree(newOldNames);
			}
			else
			{
				throw new SystemLockTimeoutException(IClientServerConstants.PLUGIN_ID);
			}
		}
		catch (IOException e) {
			if (!(outputTempFile == null)) outputTempFile.delete();
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, "Could not rename " + fullVirtualName, e); //$NON-NLS-1$
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
	}

	/**
	 * Creates a tar file from the given virtual child objects, using the given
	 * output stream and renaming entries according to hash map entries.
	 *
	 * @param children an array of virtual children from which to create a tar
	 * 		file.
	 * @param outStream the tar output stream to use.
	 * @param renameMap a map containing associations between old names and new
	 * 		names. Old names are the keys in the map, and the values are the new
	 * 		names.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return <code>true</code> if the operation completes successfully, or
	 * 	<code>false</code> if it is cancelled by the user.
	 * @throws IOException if an I/O exception occurs.
	 * @since org.eclipse.rse.services 3.0
	 */
	protected boolean createTar(VirtualChild[] children, TarOutputStream outStream, HashMap renameMap, ISystemOperationMonitor archiveOperationMonitor) throws IOException {

		TarFile tarFile = getTarFile();

		// go through each child
		for (int i = 0; i < children.length; i++) {

			if (archiveOperationMonitor != null && archiveOperationMonitor.isCancelled())
			{
				//the operation has been cancelled
				return false;
			}

			VirtualChild child = children[i];
			String oldPath = child.getArchiveStandardName();
			String newPath = oldPath;
			boolean needToRename = false;

			// if entry is to be renamed, get the new path
			if (renameMap.containsKey(oldPath)) {
				newPath = (String)(renameMap.get(oldPath));
				child.renameTo(newPath);
				needToRename = true;
			}

			TarEntry nextEntry = tarFile.getEntry(oldPath);

			// if child is a directory, then just add an entry for it
			// there is no data
			if (children[i].isDirectory) {

				// if we need to rename the entry, then do so now
				if (needToRename) {
					nextEntry = changeTarEntryName(nextEntry, newPath);
				}

				// put the entry
				outStream.putNextEntry(nextEntry);

				// close the entry
				outStream.closeEntry();
			}
			// otherwise child is a file, so add an entry for it
			// and then add data (i.e. file contents).
			else {

				// get the input stream for the file contents
				InputStream inStream = tarFile.getInputStream(nextEntry);

				// if we need to rename the entry, then do so now
				// this must be done after we have obtained the input stream
				// since tarFile.getInputStream() depends on the entry name
				if (needToRename) {
					nextEntry = changeTarEntryName(nextEntry, newPath);
				}

				// put the entry
				outStream.putNextEntry(nextEntry);

				// write data
				byte[] buf = new byte[ITarConstants.BLOCK_SIZE];
				int numRead = inStream.read(buf);

				while (numRead > 0) {
					outStream.write(buf, 0, numRead);
					numRead = inStream.read(buf);
				}

				// close input stream
				inStream.close();

				// close entry, but do not close the output stream
				outStream.closeEntry();
			}
		}

		return true;
	}

	/**
	 * Changes the name of a tar entry. Also calculates the new checksum for the
	 * entry.
	 *
	 * @param entry the entry for which the name has to be changed.
	 * @param newName the new name for the entry.
	 * @return the changed entry.
	 * @since 3.0 TarEntry moved into API
	 */
	protected TarEntry changeTarEntryName(TarEntry entry, String newName) {

		// change entry path
		entry.setName(newName);

		// calculate checksum
		entry.calculateChecksum();

		return entry;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public File[] getFiles(String[] fullNames, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		File[] files = new File[fullNames.length];

		for (int i = 0; i < fullNames.length; i++) {
			String name;
			String fullName = fullNames[i];
			fullName = ArchiveHandlerManager.cleanUpVirtualPath(fullName);
			int j = fullName.lastIndexOf("/"); //$NON-NLS-1$

			if (j == -1) {
				name = fullName;
			}
			else {
				name = fullName.substring(j+1);
			}

			try {
				files[i] = File.createTempFile(name, "virtual"); //$NON-NLS-1$
				files[i].deleteOnExit();
				extractVirtualFile(fullNames[i], files[i], archiveOperationMonitor);
			}
			catch (IOException e) {
				throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
			}
		}

		return files;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void createFolder(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		fullVirtualName = fullVirtualName + "/"; //$NON-NLS-1$
		try {
			createVirtualObject(fullVirtualName, archiveOperationMonitor);
		} finally {
			setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void createFile(String fullVirtualName, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);
		try {
			createVirtualObject(fullVirtualName, archiveOperationMonitor);
		} finally {
			setArchiveOperationMonitorStatusDone(archiveOperationMonitor);
		}
	}

	/**
	 * Creates a virtual object that does not already exist in the virtual file
	 * system. Creates an empty file in the tar file.
	 *
	 * @param name the name of the virtual object.
	 * @param archiveOperationMonitor the operation progress monitor
	 * @return <code>true</code> if the object was created successfully,
	 * 	<code>false</code> if the object already exists such that this is a
	 * 	successful no-op, or if the operation is cancelled by the user.
	 * @throws SystemMessageException in case of an error.
	 * @since org.eclipse.rse.services 3.0
	 */
	protected boolean createVirtualObject(String name, ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {

		File outputTempFile = null;
		TarOutputStream outStream = null;
		int mutexLockStatus = SystemReentrantMutex.LOCK_STATUS_NOLOCK;
		try
		{
			mutexLockStatus = _mutex.waitForLock(archiveOperationMonitor, Long.MAX_VALUE);
			if (SystemReentrantMutex.LOCK_STATUS_NOLOCK != mutexLockStatus)
			{
				updateCache();

				// if the object already exists, return false
				if (exists(name, archiveOperationMonitor)) {
					return false;
				}

				// open a new temp file which will be our destination for the new tar file
				outputTempFile = new File(file.getAbsolutePath() + "temp"); //$NON-NLS-1$
				outStream = getTarOutputStream(outputTempFile);

				// get all the entries
				VirtualChild[] children = getVirtualChildrenList(archiveOperationMonitor);

				// if it is an empty temp file, no need to recreate it
				if (children.length != 0) {
					boolean ok = createTar(children, outStream, (HashSet)null, archiveOperationMonitor);
					if (!ok)
					{
						outStream.close();
						if (outputTempFile != null)
						{
							outputTempFile.delete();
						}
						throw new SystemOperationCancelledException();
					}
				}

				// append an empty file to the tar file
				TarEntry newEntry = appendEmptyFile(outStream, name);

				// add to cache
				VirtualChild temp = getVirtualChild(newEntry);
				vfs.addEntry(temp);

				// close the output stream
				outStream.close();

				// replace the current tar file with the new one, but do not update the cache
				// since we have already updated to the cache
				replaceFile(outputTempFile, false);
			} else {
				throw new SystemLockTimeoutException(IClientServerConstants.PLUGIN_ID);
			}
		}
		catch (IOException e) {
			// close output stream
			if (outStream != null)
			{
				try
				{
					outStream.close();
				}
				catch (Exception exp)
				{
					exp.printStackTrace();
				}
			}
			if (outputTempFile != null)
			{
				outputTempFile.delete();
			}
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
		finally
		{
			releaseMutex(mutexLockStatus);
		}
		return true;
	}

	/**
	 * Creates a new tar entry and appends it to the tar output stream with the
	 * given name.
	 * 
	 * @param outStream the tar output stream.
	 * @param name the name of the new tar entry.
	 * @return the newly created tar entry.
	 * @throws IOException if an I/O error occurs.
	 * @since 3.0 TarEntry moved into API
	 */
	protected TarEntry appendEmptyFile(TarOutputStream outStream, String name) throws IOException {

		// create a new entry with size 0 and the last modified time as the current time
		TarEntry newEntry = new TarEntry(name);
		newEntry.setSize(0);
		newEntry.setModificationTime(System.currentTimeMillis());

		// set the user name
		String userName = System.getProperty("user.name"); //$NON-NLS-1$

		if (userName != null) {
			newEntry.setUserName(userName);
		}

		// set user permissions
		boolean canRead = file.canRead();
		boolean canWrite = file.canWrite();

		// getting execute permission is a bit tricky
		// need to go through security manager
		boolean canExecute = false;

		// first get the system security manager
		SecurityManager sm = System.getSecurityManager();

		// if there is no security manager then create a new one
		if (sm == null) {
			sm = new SecurityManager();
		}

		try {

			// if security manager successfully created, check permission

			// create a file permission to check execute
			FilePermission permission = new FilePermission(file.getAbsolutePath(), "execute"); //$NON-NLS-1$

			// this call will throw a SecurityException if permission does not exist
			sm.checkPermission(permission);
			canExecute = true;

		}
		catch(SecurityException e) {
			canExecute = false;
		}

		newEntry.setUserMode(canRead, canWrite, canExecute);

		// calculate checksum
		newEntry.calculateChecksum();

		// put the entry
		outStream.putNextEntry(newEntry);

		// close the entry
		outStream.closeEntry();

		return newEntry;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getStandardName(org.eclipse.rse.services.clientserver.archiveutils.VirtualChild)
	 */
	public String getStandardName(VirtualChild vc) {

		if (vc.isDirectory) {
			return vc.fullName + "/"; //$NON-NLS-1$
		}

		return vc.fullName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void create() throws SystemMessageException {

		try {

			// create output stream
			TarOutputStream outStream = getTarOutputStream(file);

			// close output stream, so we have an empty tar file
			outStream.close();

			// recreate cache
			createCache();

			// set cache time
			modTimeDuringCache = file.lastModified();
		}
		catch (IOException e) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public SystemSearchLineMatch[] search(String fullVirtualName, SystemSearchStringMatcher matcher, ISystemOperationMonitor archiveOperationMonitor)
			throws SystemMessageException {
		// if the search string is empty or if it is "*", then return no matches
		// since it is a file search
		if (matcher.isSearchStringEmpty() || matcher.isSearchStringAsterisk()) {
			return new SystemSearchLineMatch[0];
		}

		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		VirtualChild vc = getVirtualFile(fullVirtualName, archiveOperationMonitor);

		if (!vc.exists() || vc.isDirectory) {
			return new SystemSearchLineMatch[0];
		}

		TarFile tarFile = getTarFile();
		TarEntry entry = tarFile.getEntry(fullVirtualName);
		InputStream is = null;

		try {
			is = tarFile.getInputStream(entry);

			if (is == null) {
				return new SystemSearchLineMatch[0];
			}

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader bufReader = new BufferedReader(isr);

			SystemSearchStringMatchLocator locator = new SystemSearchStringMatchLocator(bufReader, matcher);
			SystemSearchLineMatch[] matches = locator.locateMatches();

			if (matches == null) {
				return new SystemSearchLineMatch[0];
			}
			else {
				return matches;
			}
		}
		catch (IOException e) {
			throw new SystemOperationFailedException(IClientServerConstants.PLUGIN_ID, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#exists()
	 */
	public boolean exists()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getCommentFor(java.lang.String)
	 */
	public String getCommentFor(String fullVirtualName)
	{
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getCompressedSizeFor(java.lang.String)
	 */
	public long getCompressedSizeFor(String fullVirtualName)
	{
		return getSizeFor(fullVirtualName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getCompressionMethodFor(java.lang.String)
	 */
	public String getCompressionMethodFor(String fullVirtualName)
	{
		return ""; //$NON-NLS-1$
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getArchiveComment()
	 */
	public String getArchiveComment()
	{
		return ""; //$NON-NLS-1$
	}

	/**
	 * Compresses the file <code>file</code> and adds it to the archive, saving
	 * it in the encoding specified by <code>encoding</code> if saving in text
	 * mode. Places the file in the virtual directory <code>virtualPath</code>.
	 * Pass the name as the parameter <code>name</code>. If the virtual path
	 * does not exist in the archive, create it. If <code>file</code> is a
	 * directory, copy it and its contents into the archive, maintaining the
	 * tree structure.
	 *
	 * @param file the file to be added to the archive
	 * @param virtualPath the destination of the file
	 * @param name the name of the result virtual file
	 * @param encoding the file encoding to use
	 * @param registry the file type to use (text or binary)
	 * @param archiveOperationMonitor the operation progress monitor
	 * @throws SystemMessageException in case of an error
	 * @since org.eclipse.rse.services 3.0
	 */
	public void add(File file, String virtualPath, String name, String encoding, ISystemFileTypes registry, ISystemOperationMonitor archiveOperationMonitor)
			throws SystemMessageException {
		add(file, virtualPath, name, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(File file, String virtualPath, String name,
			String sourceEncoding, String targetEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		add(file, virtualPath, name, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(File[] files, String virtualPath, String[] names,
			String[] sourceEncodings, String[] targetEncodings, boolean[] isTexts,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		add(files, virtualPath, names, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualDirectory(String dir, File destinationParent,
			File destination, String sourceEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		extractVirtualDirectory(dir, destinationParent, destination, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualDirectory(String dir, File destinationParent, String sourceEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		extractVirtualDirectory(dir, destinationParent, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void extractVirtualFile(String fullVirtualName, File destination, String sourceEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		extractVirtualFile(fullVirtualName, destination, archiveOperationMonitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler#getClassification(java.lang.String)
	 */
	public String getClassification(String fullVirtualName) throws SystemMessageException {
		fullVirtualName = ArchiveHandlerManager.cleanUpVirtualPath(fullVirtualName);

		// default type
		String type = "file"; //$NON-NLS-1$

		// if it's not a class file, we do not classify it
		if (!fullVirtualName.endsWith(".class")) { //$NON-NLS-1$
			return type;
		}

		// get the entry with that name
		TarEntry entry = getTarFile().getEntry(fullVirtualName);

		// get the input stream for the entry
		InputStream stream = null;

		// class file parser
		BasicClassFileParser parser = null;

		boolean isExecutable = false;

		try {
			stream = getTarFile().getInputStream(entry);

			// use class file parser to parse the class file
			parser = new BasicClassFileParser(stream);
			parser.parse();

			// query if it is executable, i.e. whether it has main method
			isExecutable = parser.isExecutable();
		}
		catch (IOException e) {
			// TODO: log it

			// we assume not executable
			isExecutable = false;
		}

		// if it is executable, then also get qualified class name
		if (isExecutable && parser != null) {
			type = "executable(java"; //$NON-NLS-1$

			String qualifiedClassName = parser.getQualifiedClassName();

			if (qualifiedClassName != null) {
				type = type + ":" + qualifiedClassName; //$NON-NLS-1$
			}

			type = type + ")"; //$NON-NLS-1$
		}

		return type;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(InputStream stream, String virtualPath, String name, String sourceEncoding, String targetEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		throw new SystemUnsupportedOperationException(IClientServerConstants.PLUGIN_ID, "add"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void add(File file, String virtualPath, String name, String sourceEncoding, String targetEncoding, ISystemFileTypes typeRegistery,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		add(file, virtualPath, name, archiveOperationMonitor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.0
	 */
	public void replace(String fullVirtualName, InputStream stream, String name, String sourceEncoding, String targetEncoding, boolean isText,
			ISystemOperationMonitor archiveOperationMonitor) throws SystemMessageException {
		throw new SystemUnsupportedOperationException(IClientServerConstants.PLUGIN_ID, "replace"); //$NON-NLS-1$
	}

	/**
	 * Construct the full virtual name of a virtual file from its virtual path and name.
	 * @param virtualPath the virtual path of this virtual file
	 * @param name the name of this virtual file
	 * @return the full virtual name of this virtual file
	 */
	private static String getFullVirtualName(String virtualPath, String name)
	{
		String fullVirtualName = null;
		if (virtualPath == null || virtualPath.length() == 0)
		{
			fullVirtualName = name;
		}
		else
		{
			fullVirtualName = virtualPath + "/" + name;  //$NON-NLS-1$
		}
		return fullVirtualName;
	}

	private void releaseMutex(int mutexLockStatus)
	{
		//We only release the mutex if we aquired it, not borrowed it.
		if (SystemReentrantMutex.LOCK_STATUS_AQUIRED == mutexLockStatus)
		{
			_mutex.release();
		}
	}

	private void setArchiveOperationMonitorStatusDone(ISystemOperationMonitor archiveOperationMonitor)
	{
		//We only set the status of the archive operation montor to done if it is not been cancelled.
		if (null != archiveOperationMonitor && !archiveOperationMonitor.isCancelled())
		{
			archiveOperationMonitor.setDone(true);
		}
	}

	/**
	 * Get the tar output stream for a given file. This method can be overridden
	 * by subclass to return compressed output steam if needed.
	 *
	 * @param outputFile the output file to create stream
	 * @return OutputStream the output stream to write
	 * @throws FileNotFoundException when the output file doesn't exist
	 * @since org.eclipse.rse.services 3.0
	 */
	protected TarOutputStream getTarOutputStream(File outputFile) throws FileNotFoundException {
		TarOutputStream outStream = new TarOutputStream(new FileOutputStream(outputFile));
		return outStream;
	}
}
