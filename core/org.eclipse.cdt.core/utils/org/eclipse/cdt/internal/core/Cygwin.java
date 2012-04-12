/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * A collection of cygwin-related utilities.
 */
public class Cygwin {

	private static IPath findCygpathLocation(String envPath) {
		return PathUtil.findProgramLocation("cygpath", envPath); //$NON-NLS-1$
	}

	/**
	 * Check if cygwin path conversion utilities are available in the path.
	 *
	 * @param envPath - list of directories to search for cygwin utilities separated
	 *    by path separator (format of environment variable $PATH)
	 *    or {@code null} to use current $PATH.
	 * @return {@code true} if cygwin is available, {@code false} otherwise.
	 */
	public static boolean isAvailable(String envPath) {
		return Platform.getOS().equals(Platform.OS_WIN32) && findCygpathLocation(envPath) != null;
	}

	/**
	 * Check if cygwin path conversion utilities are available in $PATH.
	 *
	 * @return {@code true} if cygwin is available, {@code false} otherwise.
	 */
	public static boolean isAvailable() {
		return Platform.getOS().equals(Platform.OS_WIN32) && findCygpathLocation(null) != null;
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath - Cygwin path.
	 * @param envPath - list of directories to search for cygwin utilities separated
	 *    by path separator (format of environment variable $PATH).
	 * @return Windows style converted path. Note that that also converts cygwin links to their targets.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String cygwinToWindowsPath(String cygwinPath, String envPath) throws IOException, UnsupportedOperationException {
		if (cygwinPath == null || cygwinPath.trim().length() == 0)
			return cygwinPath;

		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// Don't run this on non-windows platforms
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable."); //$NON-NLS-1$
		}

		IPath cygpathLocation = findCygpathLocation(envPath);
		if (cygpathLocation == null) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not in the system search path."); //$NON-NLS-1$
		}

		String[] args = {cygpathLocation.toOSString(), "-w", cygwinPath}; //$NON-NLS-1$
		Process cygpathProcess = Runtime.getRuntime().exec(args);
		BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpathProcess.getInputStream()));

		String windowsPath = stdout.readLine();
		if (windowsPath == null) {
			throw new IOException("Unexpected output from Cygwin utility cygpath."); //$NON-NLS-1$
		}
		return windowsPath.trim();
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath - Cygwin path.
	 * @return Windows style converted path. Note that that also converts cygwin links to their targets.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String cygwinToWindowsPath(String cygwinPath) throws IOException, UnsupportedOperationException {
		return cygwinToWindowsPath(cygwinPath, null);
	}

	/**
	 * Conversion from Windows path to Cygwin path.
	 *
	 * @param windowsPath - Windows path.
	 * @param envPath - list of directories to search for cygwin utilities (value of environment variable $PATH).
	 * @return Cygwin style converted path.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String windowsToCygwinPath(String windowsPath, String envPath) throws IOException, UnsupportedOperationException {
		if (windowsPath == null || windowsPath.trim().length() == 0)
			return windowsPath;

		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// Don't run this on non-windows platforms
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable."); //$NON-NLS-1$
		}
		IPath cygpathLocation = findCygpathLocation(envPath);
		if (cygpathLocation == null) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not in the system search path."); //$NON-NLS-1$
		}

		String[] args = {cygpathLocation.toOSString(), "-u", windowsPath}; //$NON-NLS-1$
		Process cygpath = Runtime.getRuntime().exec(args);
		BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));

		String cygwinPath = stdout.readLine();
		if (cygwinPath == null) {
			throw new IOException("Unexpected output from Cygwin utility cygpath."); //$NON-NLS-1$
		}
		return cygwinPath.trim();
	}

	/**
	 * Conversion from Windows path to Cygwin path.
	 *
	 * @param windowsPath - Windows path.
	 * @return Cygwin style converted path.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String windowsToCygwinPath(String windowsPath) throws IOException, UnsupportedOperationException {
		return windowsToCygwinPath(windowsPath, null);
	}
}
