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
