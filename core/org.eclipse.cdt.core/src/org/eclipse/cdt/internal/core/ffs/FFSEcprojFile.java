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

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 * Contents of the ecproj file.
 */
public class FFSEcprojFile {

	private final URI uri;
	private final IFileStore root;
	private final FFSFileSystem fileSystem;
	
	public FFSEcprojFile(FFSFileSystem fileSystem, URI uri) throws CoreException {
		this.uri = uri;
		this.fileSystem = fileSystem;
		this.root = EFS.getStore(uri);
	}
	
	public FFSFileSystem getFileSystem() {
		return fileSystem;
	}
	
	public IFileStore getRoot() {
		return new FFSFileStore(this, null, root);
	}
	
}
