/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.core.ffs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileTree;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 * This is the virtual file system. It maps URIs to files in underlying file systems.
 * In doing so, it allows the hierarchical structure of URIs to be different.
 * In particular, you can add files from one location to another and excludes files
 * and directories from the tree.
 */
public class FFSFileSystem extends FileSystem {

	public FFSFileSystem() {
	}
	
	public IFileStore getStore(URI uri) {
		try {
			URI realURI = new URI(getScheme(), uri.getSchemeSpecificPart(), uri.getFragment());
			return EFS.getStore(realURI);
		} catch (URISyntaxException e) {
			CCorePlugin.log(e);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		return EFS.getNullFileSystem().getStore(uri);
	}

	public int attributes() {
		// TODO what attributes should we support?
		return 0;
	}

	public boolean canDelete() {
		return true;
	}

	public boolean canWrite() {
		return true;
	}

	public IFileTree fetchFileTree(IFileStore root, IProgressMonitor monitor) {
		try {
			// TODO obviously
			return EFS.getNullFileSystem().fetchFileTree(root, monitor);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IFileStore fromLocalFile(File file) {
		return EFS.getLocalFileSystem().fromLocalFile(file);
	}

	public IFileStore getStore(IPath path) {
		return EFS.getLocalFileSystem().getStore(path);
	}

	public boolean isCaseSensitive() {
		return EFS.getLocalFileSystem().isCaseSensitive();
	}

}
