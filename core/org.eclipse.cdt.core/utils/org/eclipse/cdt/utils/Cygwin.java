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

// A collection of Cygwin-related utilities.
public class Cygwin {
	private static boolean present;
	private static String rootDir;
	static {
		initialize();
	}

	// initialize static data fields
	private static void initialize() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			Map<String, String> environment = System.getenv();
			rootDir = environment.get("CYGWIN_DIR"); //$NON-NLS-1$
			if (rootDir != null) {
				if (dirHasCygwin1Dll(rootDir)) {
					present = true;
					rootDir = new Path(rootDir).toPortableString();
					return;
				}
			}
			for (char drive = 'C'; drive < 'H'; drive++) {
				StringBuilder dirStringBuilder = new StringBuilder();
				dirStringBuilder.append(drive);
				dirStringBuilder.append(":/cygwin64"); //$NON-NLS-1$
				String dirString = dirStringBuilder.toString();
				if (dirHasCygwin1Dll(dirString)) {
					present = true;
					rootDir = new Path(dirString).toPortableString();
					return;
				}
			}
		}
	}

	private static boolean dirHasCygwin1Dll(String dirString) {
		IPath dirLocation = new Path(dirString);
		File dir = dirLocation.toFile();
		if (dir.isAbsolute() && dir.exists() && dir.isDirectory()) {
			File file = dirLocation.append("/bin/cygwin1.dll").toFile(); //$NON-NLS-1$
			if (file.exists() && file.isFile() && file.canRead()) {
				return true;
			}
		}
		return false;
	}

	public boolean isPresent() {
		return present;
	}

	public String getRootDir() {
		return rootDir;
	}

	// Convert Unix path to Windows path
	public static String pathToWindows(String unixPath) {
		if (!present) {
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

		if (unixPath.startsWith("/")) { //$NON-NLS-1$
			// absolute path
			String[] segments = path.segments();
			if (segments.length >= 2) {
				if (segments[0].equals("cygdrive")) { //$NON-NLS-1$
					if (segments[1].length() == 1) {
						char drive = segments[1].charAt(0);
						if ((drive >= 'a' && drive <= 'z') || (drive >= 'A' && drive <= 'Z')) {
							String device = segments[1].toUpperCase();
							StringBuilder builder = new StringBuilder();
							builder.append(device);
							builder.append(':');
							for (int i = 2; i < segments.length; i++) {
								builder.append('/');
								builder.append(segments[i]);
							}
							windowsPath = builder.toString();
							return windowsPath;
						}
					}
				}
				if (segments[0].equals("usr") && (segments[1].equals("bin") || segments[1].equals("lib"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// /usr/lib --> /lib
					// /usr/bin --> /bin
					// /usr/include unchanged
					String[] newSegments = new String[segments.length - 1];
					System.arraycopy(segments, 1, newSegments, 0, segments.length - 1);
					segments = newSegments;
				}
			}
			// unixPath.startsWith("/") && segments.length >= 0
			StringBuilder builder = new StringBuilder();
			builder.append(rootDir);
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
		if (!present) {
			return windowsPath;
		}
		if (windowsPath == null || windowsPath.trim().length() == 0) {
			return windowsPath;
		}

		IPath rootDirPath = Path.fromOSString(rootDir);
		IPath path = Path.fromOSString(windowsPath);
		String unixPath;
		if (!path.isAbsolute()) {
			// relative path
			unixPath = path.toPortableString();
		} else if (rootDirPath.isPrefixOf(path)) {
			int matchingFirstSegments = rootDirPath.matchingFirstSegments(path);
			String[] segments = path.segments();
			StringBuilder builder = new StringBuilder();
			for (int i = matchingFirstSegments; i < segments.length; i++) {
				builder.append('/');
				builder.append(segments[i]);
			}
			unixPath = builder.toString();
		} else {
			String device = path.getDevice().replace(':', ' ').trim().toLowerCase();
			String[] segments = path.segments();
			StringBuilder builder = new StringBuilder();
			builder.append("/cygdrive/"); //$NON-NLS-1$
			builder.append(device);
			for (String s : segments) {
				builder.append('/');
				builder.append(s);
			}
			unixPath = builder.toString();
		}

		return unixPath;
	}
}
