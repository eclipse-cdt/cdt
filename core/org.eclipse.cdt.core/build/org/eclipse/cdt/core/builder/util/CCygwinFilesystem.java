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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.internal.core.ProcessClosure;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

/**
 * Performs file path translation on a Windows + Cygwin system.
 * <p>
 * This allows for translation between "native" Windows path
 * names and Cygwin style path names.
 */
public class CCygwinFilesystem implements IFilesystem {

	private static String CONVERT_CMD = "cygpath"; //$NON-NLS-1$
	private static String CONVERT_TO_UNIX = "-u"; //$NON-NLS-1$
	private static String CONVERT_TO_NATIVE = "-w"; //$NON-NLS-1$

	private String fHome;

	public CCygwinFilesystem() {
		super();
		fHome =
			getNativePath(IFilesystem.FILESYSTEM_ROOT)
				+ IFilesystem.FILESYSTEM_ROOT;
	}

	/**
	 * Helper function for converting native (Windows) paths to Unix paths,
	 * and vice versa.
	 *
	 * @param path the path to covert.
	 * @param cmdFlags	how to convert the path. Supported values for are
	 * CONVERT_TO_UNIX and CONVERT_TO_NATIVE.
	 */
	private String convertPath(String path, String cmdFlags) {

		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		String[] cmds = { CONVERT_CMD, cmdFlags, path };
		String newPath = path;

		// In the event that cygpath is not found, or fails for some reason,
		// this function will return a Cygwinized/Javaized version of the
		// path (ex, "C:\foo\bar" will become "C:/foo/bar").

		try {
			ProcessFactory pf = ProcessFactory.getFactory();
			Process pid = pf.exec(cmds);
			ProcessClosure pc = new ProcessClosure(pid, stdout, stderr);

			pc.runBlocking();

			newPath = stdout.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newPath.replace(PATHSEP_WINDOWS, PATHSEP_CYGWIN);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getRoot()
	 */
	public String getRoot() {
		return fHome;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(String)
	 */
	public String getNativePath(String path) {
		return convertPath(path, CONVERT_TO_NATIVE);
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getNativePath(File)
	 */
	public String getNativePath(File path) {
		return getNativePath(path.toString());
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(String)
	 */
	public String getUnixPath(String path) {

		path = convertPath(path, CONVERT_TO_UNIX);

		// Make sure there are no spaces in the path and if there are, escape them.
		String subString = new String(""); //$NON-NLS-1$
		int len = 0;
		int begin = 0;
		while ((len = path.indexOf(" ")) >= 0) { //$NON-NLS-1$
			subString += path.substring(begin, len);
			subString += "\\ "; //$NON-NLS-1$
			path = path.substring(len + 1);
		}
		subString += path;

		return subString;
	}

	/**
	 * @see org.eclipse.cdt.core.builder.util.IFilesystem#getUnixPath(File)
	 */
	public String getUnixPath(File path) {
		return getUnixPath(path.toString());
	}

}
