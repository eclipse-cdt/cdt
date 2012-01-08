/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev (Quoin Inc.) - contributed to CDT from org.eclipse.core.tests.resources v20090320
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests.filesystem.ram;

import java.io.*;
import java.util.ArrayList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * An in-memory file system.
 */
public class MemoryTree {
	static class DirNode extends Node {
		private final ArrayList children = new ArrayList();

		DirNode(Node parent, String name) {
			super(parent, name);
		}

		void add(Node child) {
			children.add(child);
		}

		public String[] childNames() {
			String[] names = new String[children.size()];
			for (int i = 0, imax = children.size(); i < imax; i++) {
				Node child = (Node) children.get(i);
				names[i] = child.getInfo(false).getName();
			}
			return names;
		}

		/**
		 * Returns the child with the given name, or null if not found.
		 * @param name
		 * @return
		 */
		Node getChild(String name) {
			for (int i = 0, imax = children.size(); i < imax; i++) {
				Node child = (Node) children.get(i);
				if (child.getInfo(false).getName().equals(name))
					return child;
			}
			return null;
		}

		@Override
		protected void initializeInfo(FileInfo fileInfo) {
			super.initializeInfo(fileInfo);
			fileInfo.setDirectory(true);
		}

		@Override
		boolean isFile() {
			return false;
		}

		void remove(String name) {
			Node child = getChild(name);
			if (child != null)
				children.remove(child);
		}

		@Override
		public String toString() {
			return super.toString() + ' ' + children;
		}
	}

	static class FileNode extends Node {
		byte[] contents = EMPTY_CONTENTS;

		FileNode(Node parent, String name) {
			super(parent, name);
		}

		@Override
		boolean isFile() {
			return true;
		}

		public InputStream openInputStream() {
			return new ByteArrayInputStream(contents);
		}

		public OutputStream openOutputStream(final int options) {
			return new ByteArrayOutputStream() {
				@Override
				public void close() throws IOException {
					super.close();
					setContents(toByteArray(), options);
				}
			};
		}

		protected void setContents(byte[] bytes, int options) {
			if ((options & EFS.APPEND) != 0) {
				//create reference in case of concurrent modification
				byte[] oldContents = this.contents;
				byte[] newContents = new byte[oldContents.length + bytes.length];
				System.arraycopy(oldContents, 0, newContents, 0, oldContents.length);
				System.arraycopy(bytes, 0, newContents, oldContents.length, bytes.length);
				this.contents = newContents;
			} else
				this.contents = bytes;
			info.setLastModified(System.currentTimeMillis());
			((FileInfo) info).setLength(bytes.length);
		}
	}

	static abstract class Node {
		protected IFileInfo info;

		Node(Node parent, String name) {
			if (parent != null)
				((DirNode) parent).add(this);
			FileInfo fileInfo = new FileInfo(name);
			initializeInfo(fileInfo);
			this.info = fileInfo;
		}

		IFileInfo getInfo(boolean copy) {
			return (IFileInfo) (copy ? ((FileInfo) info).clone() : info);
		}

		protected void initializeInfo(FileInfo fileInfo) {
			fileInfo.setExists(true);
			fileInfo.setLastModified(System.currentTimeMillis());
		}

		abstract boolean isFile();

		void putInfo(IFileInfo newInfo, int options) {
			if ((options & EFS.SET_ATTRIBUTES) != 0) {
				for (int i = 0; i < ALL_ATTRIBUTES.length; i++)
					info.setAttribute(ALL_ATTRIBUTES[i], newInfo.getAttribute(ALL_ATTRIBUTES[i]));
			}
			if ((options & EFS.SET_LAST_MODIFIED) != 0) {
				info.setLastModified(newInfo.getLastModified());
			}
		}

		/**
		 * For debugging purposes only.
		 */
		@Override
		public String toString() {
			return info.getName();
		}
	}

	static final int[] ALL_ATTRIBUTES = new int[] {EFS.ATTRIBUTE_ARCHIVE, EFS.ATTRIBUTE_EXECUTABLE, EFS.ATTRIBUTE_HIDDEN, EFS.ATTRIBUTE_READ_ONLY,};

	public static final MemoryTree TREE = new MemoryTree();

	static final byte[] EMPTY_CONTENTS = new byte[0];

	private static final String ROOT_NAME = "<root>";

	private Node root = new DirNode(null, ROOT_NAME);

	private MemoryTree() {
		// TREE singleton should be used rather than direct instantiation
	}

	public String[] childNames(IPath path) {
		Node node = findNode(path);
		if (node == null || node.isFile())
			return null;
		return ((DirNode) node).childNames();
	}

	public void delete(IPath path) {
		//cannot delete the root
		if (path.segmentCount() == 0)
			return;
		Node parent = findNode(path.removeLastSegments(1));
		if (parent == null || parent.isFile())
			return;
		((DirNode) parent).remove(path.lastSegment());

	}

	/**
	 * Deletes the entire memory tree. Used during debugging and testing only.
	 */
	public void deleteAll() {
		this.root = new DirNode(null, ROOT_NAME);
	}

	/**
	 * Returns the file info for the given path. Never returns null.
	 * @param path
	 * @return
	 */
	public synchronized IFileInfo fetchInfo(IPath path) {
		Node node = findNode(path);
		if (node == null)
			return new FileInfo(path.lastSegment());
		return node.getInfo(true);
	}

	/**
	 * Returns the node at the given path, or null if not found.
	 * 
	 * @param path
	 * @return
	 */
	private Node findNode(IPath path) {
		Node current = root;
		for (int i = 0, imax = path.segmentCount(); i < imax; i++) {
			if (current == null || current.isFile())
				return null;
			current = ((DirNode) current).getChild(path.segment(i));
		}
		return current;
	}

	public Node mkdir(IPath path, boolean deep) throws CoreException {
		Node dir = findNode(path);
		if (dir != null) {
			if (dir.isFile())
				Policy.error("A file exists with this name: " + path);
			return dir;
		}
		final IPath parentPath = path.removeLastSegments(1);
		Node parent = findNode(parentPath);
		if (parent != null) {
			if (parent.isFile())
				Policy.error("Parent is a file: " + path);
		} else {
			if (!deep)
				Policy.error("Parent does not exist: " + parentPath);
			parent = mkdir(parentPath, deep);
		}
		//create the child directory
		return new DirNode(parent, path.lastSegment());
	}

	public InputStream openInputStream(IPath path) throws CoreException {
		Node node = findNode(path);
		if (node == null)
			Policy.error("File not found: " + path);
		if (!node.isFile())
			Policy.error("Cannot open stream on directory: " + path);
		return ((FileNode) node).openInputStream();
	}

	public OutputStream openOutputStream(IPath path, int options) throws CoreException {
		Node node = findNode(path);
		//if we already have such a file, just open a stream on it
		if (node instanceof DirNode)
			Policy.error("Could not create file: " + path);
		if (node instanceof FileNode)
			return ((FileNode) node).openOutputStream(options);
		//if the parent exists we can create the file
		Node parent = findNode(path.removeLastSegments(1));
		if (!(parent instanceof DirNode))
			Policy.error("Could not create file: " + path);
		node = new FileNode(parent, path.lastSegment());
		return ((FileNode) node).openOutputStream(options);
	}

	public void putInfo(IPath path, IFileInfo info, int options) throws CoreException {
		Node node = findNode(path);
		if (node == null)
			Policy.error("File not found: " + path);
		node.putInfo(info, options);
	}
}
