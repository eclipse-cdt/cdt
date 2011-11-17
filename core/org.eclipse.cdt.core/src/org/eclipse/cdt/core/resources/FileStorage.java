/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/**
 *
 * @see IStorage
 */
public class FileStorage extends PlatformObject implements IStorage {
	IPath path;
	InputStream in = null;

	@Override
	public InputStream getContents() throws CoreException {
		if (in == null) {
			try {
				return new FileInputStream(path.toFile());
			} catch (FileNotFoundException e) {
				throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
						e.toString(), e));
			}
		}
		return in;
	}

	/**
	 * @see IStorage#getFullPath
	 */
	@Override
	public IPath getFullPath() {
		return this.path;
	}

	/**
	 * @see IStorage#getName
	 */
	@Override
	public String getName() {
		return this.path.lastSegment();
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

	public FileStorage(IPath path){
		this.path = path;
	}

	public FileStorage(InputStream in, IPath path){
		this.path = path;
		this.in = in;
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	@Override
	public String toString() {
		return path.toOSString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof IStorage) {
			IPath p= getFullPath();
			IPath objPath= ((IStorage)obj).getFullPath();
			if (p != null && objPath != null)
				return p.equals(objPath);
		}
		return super.equals(obj);
	}
}
