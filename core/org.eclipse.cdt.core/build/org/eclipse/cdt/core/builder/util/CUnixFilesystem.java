/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.util;

import java.io.File;

/**
 * Performs file path translation on a Unix system.
 * <p>
 * This is essentially uninteresting, as the whole purpose of the filesystem
 * abstraction is to provide for some minimal support for Unix-y file paths
 * under Windows + Cygwin.
 */
public class CUnixFilesystem implements IFilesystem {
	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getRoot()
	 */
	public String getRoot() {
		return IFilesystem.FILESYSTEM_ROOT;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(String)
	 */
	public String getNativePath(String path) {
		return new String(path);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(File)
	 */
	public String getNativePath(File path) {
		return path.toString();
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(String)
	 */
	public String getUnixPath(String path) {
		return new String(path);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(File)
	 */
	public String getUnixPath(File path) {
		return path.toString();
	}
}
