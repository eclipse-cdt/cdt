/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.core.runtime.Platform;

public class CygwinEFSExtensionProvider extends EFSExtensionProvider {
	@Override
	public String getMappedPath(URI locationURI) {
		String cygwinPath = getPathFromURI(locationURI);
		String windowsPath = null;
		try {
			windowsPath = cygwinToWindowsPath(cygwinPath);
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		return windowsPath;
	}

	/**
	 * Conversion from Windows path to Cygwin path.
	 *
	 * @param windowsPath - Windows path.
	 * @return Cygwin style converted path.
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 * 
	 * See ResourceHelper.windowsToCygwinPath(...)
	 */
	public static String windowsToCygwinPath(String windowsPath) throws IOException, UnsupportedOperationException {
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// Don't run this on non-windows platforms
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable.");
		}
		@SuppressWarnings("nls")
		String[] args = {"cygpath", "-u", windowsPath};
		Process cygpath;
		try {
			cygpath = Runtime.getRuntime().exec(args);
		} catch (IOException ioe) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not in the system search path.");
		}
		BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));

		String cygwinPath = stdout.readLine();
		if (cygwinPath == null) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not available.");
		}
		return cygwinPath.trim();
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath - Cygwin path.
	 * @return Windows style converted path.
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 * 
	 * 	 * See ResourceHelper.cygwinToWindowsPath(...)
	 */
	public static String cygwinToWindowsPath(String cygwinPath) throws IOException, UnsupportedOperationException {
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// Don't run this on non-windows platforms
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable.");
		}
		@SuppressWarnings("nls")
		String[] args = {"cygpath", "-w", cygwinPath};
		Process cygpath;
		try {
			cygpath = Runtime.getRuntime().exec(args);
		} catch (IOException ioe) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not in the system search path.");
		}
		BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));

		String windowsPath = stdout.readLine();
		if (windowsPath == null) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not available.");
		}
		return windowsPath.trim();
	}


}
