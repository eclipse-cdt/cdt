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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.*;

/**
 * In memory file system implementation used for testing.
 */
public class MemoryFileStore extends FileStore {
	private static final MemoryTree TREE = MemoryTree.TREE;

	private final IPath path;

	public MemoryFileStore(IPath path) {
		super();
		this.path = path.setDevice(null);
	}

	@Override
	public String[] childNames(int options, IProgressMonitor monitor) {
		final String[] names = TREE.childNames(path);
		return names == null ? EMPTY_STRING_ARRAY : names;
	}

	@Override
	public void delete(int options, IProgressMonitor monitor) {
		TREE.delete(path);
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) {
		return TREE.fetchInfo(path);
	}

	@Override
	public IFileStore getChild(String name) {
		return new MemoryFileStore(path.append(name));
	}

	@Override
	public String getName() {
		final String name = path.lastSegment();
		return name == null ? "" : name;
	}

	@Override
	public IFileStore getParent() {
		if (path.segmentCount() == 0)
			return null;
		return new MemoryFileStore(path.removeLastSegments(1));
	}

	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		TREE.mkdir(path, (options & EFS.SHALLOW) == 0);
		return this;
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return TREE.openInputStream(path);
	}

	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		return TREE.openOutputStream(path, options);
	}

	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		TREE.putInfo(path, info, options);
	}

	@Override
	public URI toURI() {
		return MemoryFileSystem.toURI(path);
	}
}