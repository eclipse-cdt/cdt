/********************************************************************************
 * Copyright (c) 2022 徐持恒 Xu Chiheng
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.utils;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

// A collection of Cygwin-related utilities.
public class Cygwin {
	@SuppressWarnings("unused")
	private static String cygwinDir;
	static {
		initializeCygwinDir();
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath
	 *            - Cygwin path.
	 * @return Windows style converted path.
	 *
	 */
	public static String cygwinToWindowsPath(String cygwinPath) {
		if (cygwinPath == null || cygwinPath.trim().length() == 0)
			return cygwinPath;

		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			return cygwinPath;
		}
		String windowsPath;
		IPath path = Path.fromOSString(cygwinPath);
		if (path.getDevice() != null) {
			// already a windows path
			windowsPath = path.toPortableString();
			return windowsPath;
		}
		String[] segments = path.segments();
		String[] newSegments;

		if (cygwinPath.startsWith("/")) { //$NON-NLS-1$
			// absolute path
			if (segments.length < 0) {
				// error
				return cygwinPath;
			} else if (segments.length >= 2) {
				if (segments[0].equals("cygdrive")) { //$NON-NLS-1$
					String device = segments[1].toUpperCase();

					newSegments = new String[segments.length - 2];
					System.arraycopy(segments, 2, newSegments, 0, segments.length - 2);

					StringBuilder builder = new StringBuilder();
					builder.append(device);
					builder.append(':');
					for (String s : newSegments) {
						builder.append('/');
						builder.append(s);
					}
					windowsPath = builder.toString();
					return windowsPath;
				}
				if (segments[0].equals("usr") && (segments[1].equals("bin") || segments[1].equals("lib"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					/*
					 * /usr/lib --> /lib ; /usr/bin --> /bin ; /usr/include
					 * unchanged
					 */
					newSegments = new String[segments.length - 1];
					System.arraycopy(segments, 1, newSegments, 0, segments.length - 1);
					segments = newSegments;
				}
			}
			// cygwinPath.startsWith("/") && segments.length >= 0
			StringBuilder builder = new StringBuilder();
			builder.append(cygwinDir);
			for (String s : segments) {
				builder.append('/');
				builder.append(s);
			}
			windowsPath = builder.toString();

		} else {
			// relative path
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < segments.length; i++) {
				String s = segments[i];
				if (i != 0) {
					builder.append('/');
				}
				builder.append(s);
			}
			windowsPath = builder.toString();
		}

		return windowsPath;
	}

	/**
	 * Finds location of the program inspecting each path in the path list.
	 *
	 * @param prog
	 *            - program to find. For Windows, extensions "com" and "exe" can
	 *            be omitted.
	 * @param pathsStr
	 *            - the list of paths to inspect separated by path separator
	 *            defined in the platform (i.e. ":" in Unix and ";" in Windows).
	 *            In case pathStr is {@code null} environment variable ${PATH}
	 *            is inspected.
	 * @return - absolute location of the file on the file system or
	 *         {@code null} if not found.
	 * @since 5.3
	 */
	private static IPath findProgramLocation(String prog, String pathsStr) {
		if (prog == null || prog.trim().isEmpty())
			return null;

		if (pathsStr == null)
			pathsStr = System.getenv("PATH"); //$NON-NLS-1$

		if (pathsStr.trim().isEmpty())
			return null;

		@SuppressWarnings("unused")
		String locationStr = null;
		String[] dirs = pathsStr.split(File.pathSeparator);

		// Try to find "prog.exe" or "prog.com" on Windows
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			for (String dir : dirs) {
				IPath dirLocation = new Path(dir);
				File file = null;

				file = dirLocation.append(prog + ".exe").toFile(); //$NON-NLS-1$
				if (file.isFile() && file.canRead()) {
					locationStr = file.getAbsolutePath();
					break;
				}
				file = dirLocation.append(prog + ".com").toFile(); //$NON-NLS-1$
				if (file.isFile() && file.canRead()) {
					locationStr = file.getAbsolutePath();
					break;
				}
			}
		}

		// Check "prog" on Unix and Windows too (if was not found) - could be
		// cygwin or something
		// do it in separate loop due to performance and correctness of Windows
		// regular case
		if (locationStr == null) {
			for (String dir : dirs) {
				IPath dirLocation = new Path(dir);
				File file = null;

				file = dirLocation.append(prog).toFile();
				if (file.isFile() && file.canRead()) {
					locationStr = file.getAbsolutePath();
					break;
				}
			}
		}

		if (locationStr != null)
			return new Path(locationStr);

		return null;
	}

	/**
	 * initialize static data field cygwinDir.
	 */
	private static void initializeCygwinDir() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			IPath cygwin1DllPath = findProgramLocation("cygwin1.dll", null); //$NON-NLS-1$
			if (cygwin1DllPath != null && cygwin1DllPath.segmentCount() >= 2
					&& cygwin1DllPath.segments()[cygwin1DllPath.segmentCount() - 2].equals("bin")) { //$NON-NLS-1$
				IPath cygwinDirPath = cygwin1DllPath.removeLastSegments(2);
				cygwinDir = cygwinDirPath.toPortableString();
				return;
			}
			// Cygwin not found, set cygwinDir to default
			if (Platform.getOSArch().equals(Platform.ARCH_X86_64)) {
				cygwinDir = "C:/cygwin64"; //$NON-NLS-1$
			} else {
				cygwinDir = "C:/cygwin"; //$NON-NLS-1$
			}
		}
	}

}
