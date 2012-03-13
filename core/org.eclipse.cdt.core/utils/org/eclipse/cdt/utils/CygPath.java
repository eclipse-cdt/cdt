/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CygPath {

	boolean useOldCygPath = false;
	private final Process cygpath;
	private final BufferedReader stdout;
	private final BufferedWriter stdin;
	private boolean fSpaceIsSeparator= false;

	public CygPath(String command) throws IOException {
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			// Don't run this on non-windows platforms
			throw new IOException("Not Windows"); //$NON-NLS-1$
		String[] args = {command, "--windows", "--file", "-"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cygpath = Runtime.getRuntime().exec(args);
		stdin = new BufferedWriter(new OutputStreamWriter(cygpath.getOutputStream()));
		stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));
		try {
			String test= getFileName("a b"); //$NON-NLS-1$
			if ("a".equals(test)) { //$NON-NLS-1$
				// Bug 298615: This version seems to treat space as a separator
				fSpaceIsSeparator= true;
				// Read off second part
				stdout.readLine();
			}
		} catch (IOException e) {
			// older cygwin
			dispose();
			useOldCygPath = true;
		}
	}

	public CygPath() throws IOException {
		this("cygpath"); //$NON-NLS-1$
	}

	/**
	 * Use this method for series of translations of paths.
	 * If a single path needs to be translated consider {@link Cygwin#cygwinToWindowsPath(String)}.
	 */
	public String getFileName(String name) throws IOException {
		// bug 214603, empty names don't create a response
		if (name == null || name.length() == 0)
			return name;

		if (useOldCygPath) {
			return internalgetFileName(name);
		}
		if (fSpaceIsSeparator && name.indexOf(' ') != -1) {
			return internalgetFileName(name);
		}

		// Clear everything from stdout
		while(stdout.ready()) {
			stdout.read();
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

	private String internalgetFileName(String path) throws IOException {
		Process cygPath = null;
		BufferedReader reader = null;
		try {
			cygPath = Runtime.getRuntime().exec(new String[]{"cygpath", "-w", path}); //$NON-NLS-1$ //$NON-NLS-2$
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
