/**********************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Wind River Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.core.ffs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public class FFSFileStore implements org.eclipse.core.filesystem.IFileStore {

	private final IFileStore target;
	private final IFileStore parent;
	private final FFSEcprojFile ecprojFile;

	public FFSFileStore(FFSEcprojFile ecprojFile, IFileStore parent, IFileStore target) {
		this.ecprojFile = ecprojFile;
		this.parent = parent;
		this.target = target;
	}
	
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		return target.childInfos(options, monitor);
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		// TODO child handling
		return target.childNames(options, monitor);
	}

	public IFileStore[] childStores(int options, IProgressMonitor monitor) throws CoreException {
		IFileStore[] targetChildren = target.childStores(options, monitor);
		IFileStore[] children = new IFileStore[targetChildren.length];
		for (int i = 0; i < children.length; ++i)
			children[i] = new FFSFileStore(ecprojFile, this, targetChildren[i]);
		return children;
	}

	public void copy(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		target.copy(destination, options, monitor);
	}

	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		target.delete(options, monitor);
	}

	public IFileInfo fetchInfo() {
		return target.fetchInfo();
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return target.fetchInfo(options, monitor);
	}

	public IFileStore getChild(IPath path) {
		IFileStore store = getChild(path.segment(0));
		if (store == null)
			return null;
		return store.getChild(path.removeFirstSegments(1));
	}

	public IFileStore getChild(String name) {
		// TODO child handling
		return target.getChild(name);
	}

	public IFileSystem getFileSystem() {
		return ecprojFile.getFileSystem();
	}

	public String getName() {
		return target.getName();
	}

	public IFileStore getParent() {
		return parent;
	}

	public boolean isParentOf(IFileStore other) {
		if (other == null || !(other instanceof FFSFileStore))
			return false;
		
		if (other.equals(this))
			return true;
		
		return isParentOf(other.getParent());
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return target.mkdir(options, monitor);
	}

	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
		// TODO what happens if destination is a different target file system?
		target.move(destination, options, monitor);
	}

	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return target.openInputStream(options, monitor);
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor)	throws CoreException {
		return target.openOutputStream(options, monitor);
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		target.putInfo(info, options, monitor);
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return target.toLocalFile(options, monitor);
	}

	public URI toURI() {
		// TODO
		// need base URI from file system
		// then add in the child path as the query
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IFileStore.class || adapter == FFSFileStore.class)
			return this;
		
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FFSFileStore)
			return target.equals(((FFSFileStore)obj).target);
		else
			return target.equals(obj);
	}
	
}
