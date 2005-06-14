/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class CygPath {

	boolean useOldCygPath = false;
	private final Process cygpath;
	private final BufferedReader stdout;
	private final BufferedWriter stdin;

	public CygPath(String command) throws IOException {
		String[] args = {command, "--windows", "--file", "-"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cygpath = ProcessFactory.getFactory().exec(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cygpath.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));
		try {
			getFileName("test"); //$NON-NLS-1$ // test for older cygpath 
		} catch (IOException e) {
			dispose();
			useOldCygPath = true;
		}
	}

	public CygPath() throws IOException {
		this("cygpath"); //$NON-NLS-1$
	}

	public String getFileName(String name) throws IOException {
		if (useOldCygPath) {
			return internalgetFileName(name);
		}
		stdin.write(name + "\n"); //$NON-NLS-1$
		stdin.flush();
		String str = stdout.readLine();
		if (str != null) {
			return str.trim();
		}
		throw new IOException();
	}

	public void dispose() {
		if (!useOldCygPath) {
			try {
				stdout.close();
				stdin.close();
				cygpath.getErrorStream().close();
			} catch (IOException e) {
			}
			cygpath.destroy();
		}
	}

	/**
	 * @param path
	 * @return
	 */
	private String internalgetFileName(String path) throws IOException {
		Process cygPath = null;
		BufferedReader reader = null;
		try {
			cygPath = ProcessFactory.getFactory().exec(new String[]{"cygpath", "-w", path}); //$NON-NLS-1$ //$NON-NLS-2$
			reader = new BufferedReader(new InputStreamReader(cygPath.getInputStream()));
			String newPath = reader.readLine();
			IPath ipath;
			if (path != null && !path.equals("")) { //$NON-NLS-1$
				ipath = new Path(newPath);
			} else {
				ipath = new Path(path);
			}
			if (ipath.isAbsolute() && !ipath.toFile().exists() && ipath.segment(0).length() == 1) {
				// look like it could be /c/... path
				StringBuffer drive = new StringBuffer(ipath.segment(0));
				drive.append(':');
				ipath = ipath.removeFirstSegments(1);
				ipath = ipath.makeAbsolute();
				ipath = ipath.setDevice(drive.toString());
			}
			return ipath.toOSString();
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (cygPath != null) {
				cygPath.destroy();
			}
		}
	}

}
