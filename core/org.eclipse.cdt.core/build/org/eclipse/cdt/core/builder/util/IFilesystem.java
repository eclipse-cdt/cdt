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
 * Abstracts information about a filesystem in order to allow
 * translation between native/unix pathnames.
 */
public interface IFilesystem {

	public static String	FILESYSTEM_ROOT = "/"; //$NON-NLS-1$
	public static char	PATHSEP_WINDOWS = '\\'; //$NON-NLS-1$
	public static char	PATHSEP_CYGWIN = '\\'; //$NON-NLS-1$

	/**
	 * Get the root directory for the filesystem.
	 * 
	 * The root directory is returned in native filesystem format
	 * (ex, "C:/cygwin/" on Windows, "/" on Unix.)  The returned
	 * string is guaranteed to have a trailing path seperator.
	 */
	public String getRoot();

	/**
	 * Convert the provided path into a native path.
	 * 
	 * @param path path to convert.
	 * @return native representation of path.
	 */
	public String getNativePath(String path);

	/**
	 * Convert the provided path into a native path.
	 * 
	 * @param path path to convert.
	 * @return native representation of path.
	 */
	public String getNativePath(File path);

	/**
	 * Convert the provided path into a unix path.
	 * 
	 * @param path path to convert.
	 * @return unix representation of path.
	 */
	public String getUnixPath(String path);
	
	/**
	 * Convert the provided path into a unix path.
	 * 
	 * @param path path to convert.
	 * @return unix representation of path.
	 */
	public String getUnixPath(File path);
}
