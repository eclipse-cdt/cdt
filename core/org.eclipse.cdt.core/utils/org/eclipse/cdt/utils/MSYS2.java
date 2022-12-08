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
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

// A collection of MSYS2-related utilities.
public class MSYS2 {
	public static boolean isPresent;
	private static String msys2Dir;
	static {
		initialize();
	}

	// initialize static data fields
	private static void initialize() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			Map<String, String> environment = System.getenv();
			msys2Dir = environment.get("MSYS2_DIR"); //$NON-NLS-1$
			if (msys2Dir != null) {
				if (dirHasMsys2Dll(msys2Dir)) {
					isPresent = true;
					msys2Dir = new Path(msys2Dir).toPortableString();
					return;
				}
			} else {
				for (char drive = 'C'; drive < 'H'; drive++) {
					StringBuilder dirStringBuilder = new StringBuilder();
					dirStringBuilder.append(drive);
					dirStringBuilder.append(":/msys64"); //$NON-NLS-1$
					String dirString = dirStringBuilder.toString();
					if (dirHasMsys2Dll(dirString)) {
						isPresent = true;
						msys2Dir = new Path(dirString).toPortableString();
						return;
					}
				}
			}
		}
	}

	private static boolean dirHasMsys2Dll(String dirString) {
		IPath dirLocation = new Path(dirString);
		File dir = dirLocation.toFile();
		if (dir.isAbsolute() && dir.exists() && dir.isDirectory()) {
			File file = dirLocation.append("/usr/bin/msys-2.0.dll").toFile(); //$NON-NLS-1$
			if (file.exists() && file.isFile() && file.canRead()) {
				return true;
			}
		}
		return false;
	}

	// Convert Unix path to Windows path
	public static String pathToWindows(String unixPath) {
		if (!isPresent) {
			return unixPath;
		}
		if (unixPath == null || unixPath.trim().length() == 0) {
			return unixPath;
		}

		String windowsPath;
		IPath path = Path.fromOSString(unixPath);
		if (path.getDevice() != null) {
			// already a windows absolute path
			windowsPath = path.toPortableString();
			return windowsPath;
		}
		String[] segments = path.segments();
		if (unixPath.startsWith("/")) { //$NON-NLS-1$
			// absolute path
			if (segments.length >= 1) {
				if (segments[0].length() == 1) {
					char drive = segments[0].charAt(0);
					if ((drive >= 'a' && drive <= 'z') || (drive >= 'A' && drive <= 'Z')) {
						StringBuilder builder = new StringBuilder();
						builder.append(drive);
						builder.append(':');
						for (int i = 1; i < segments.length; i++) {
							builder.append('/');
							builder.append(segments[i]);
						}
						windowsPath = builder.toString();
						return windowsPath;
					}
				}
			}
			// unixPath.startsWith("/") && segments.length >= 0
			StringBuilder builder = new StringBuilder();
			builder.append(msys2Dir);
			for (String s : segments) {
				builder.append('/');
				builder.append(s);
			}
			windowsPath = builder.toString();
		} else {
			// relative path
			windowsPath = path.toPortableString();
		}

		return windowsPath;
	}

	// Convert Windows path to Unix path
	public static String pathToUnix(String windowsPath) {
		if (!isPresent) {
			return windowsPath;
		}
		if (windowsPath == null || windowsPath.trim().length() == 0) {
			return windowsPath;
		}

		IPath msys2DirPath = Path.fromOSString(msys2Dir);
		IPath path = Path.fromOSString(windowsPath);
		String unixPath;
		if (!path.isAbsolute()) {
			// relative path
			unixPath = path.toPortableString();
		} else if (msys2DirPath.isPrefixOf(path)) {
			int matchingFirstSegments = msys2DirPath.matchingFirstSegments(path);
			String[] segments = path.segments();
			StringBuilder builder = new StringBuilder();
			for (int i = matchingFirstSegments; i < segments.length; i++) {
				builder.append('/');
				builder.append(segments[i]);
			}
			unixPath = builder.toString();
		} else {
			String device = path.getDevice().replace(':', ' ').trim();
			String[] segments = path.segments();
			StringBuilder builder = new StringBuilder();
			builder.append('/');
			builder.append(device.toLowerCase());
			for (String s : segments) {
				builder.append('/');
				builder.append(s);
			}
			unixPath = builder.toString();
		}

		return unixPath;
	}
}
