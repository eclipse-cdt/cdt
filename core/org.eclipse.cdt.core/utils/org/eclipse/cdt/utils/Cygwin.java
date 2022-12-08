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
	@SuppressWarnings("unused")
	private static boolean isPresent;
	@SuppressWarnings("unused")
	private static String cygwinDir;
	static {
		initialize();
	}

	// initialize static data fields
	private static void initialize() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			Map<String, String> environment = System.getenv();
			cygwinDir = environment.get("CYGWIN_DIR"); //$NON-NLS-1$
			if (cygwinDir != null) {
				if (dirHasCygwin1Dll(cygwinDir)) {
					isPresent = true;
					return;
				}
			} else {
				for (char drive = 'C'; drive < 'H'; drive++) {
					StringBuilder dirStringBuilder = new StringBuilder();
					dirStringBuilder.append(drive);
					dirStringBuilder.append(":/cygwin64"); //$NON-NLS-1$
					String dirString = dirStringBuilder.toString();
					if (dirHasCygwin1Dll(dirString)) {
						isPresent = true;
						cygwinDir = dirString;
						return;
					}
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
			// already a windows path
			windowsPath = path.toPortableString();
			return windowsPath;
		}
		String[] segments = path.segments();
		String[] newSegments;

		if (unixPath.startsWith("/")) { //$NON-NLS-1$
			// absolute path
			if (segments.length < 0) {
				// error
				return unixPath;
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
					// /usr/lib --> /lib
					// /usr/bin --> /bin
					// /usr/include unchanged
					newSegments = new String[segments.length - 1];
					System.arraycopy(segments, 1, newSegments, 0, segments.length - 1);
					segments = newSegments;
				}
			}
			// unixPath.startsWith("/") && segments.length >= 0
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

	// Convert Windows path to Unix path
	public static String pathToUnix(String windowsPath) {
		if (!isPresent) {
			return windowsPath;
		}
		if (windowsPath == null || windowsPath.trim().length() == 0) {
			return windowsPath;
		}

		IPath cygwinDirPath = Path.fromOSString(cygwinDir);
		IPath path = Path.fromOSString(windowsPath);
		String unixPath;
		if (cygwinDirPath.isPrefixOf(path)) {
			int matchingFirstSegments = cygwinDirPath.matchingFirstSegments(path);
			String[] segments = path.segments();
			String[] newSegments = new String[segments.length - matchingFirstSegments];
			System.arraycopy(segments, matchingFirstSegments, newSegments, 0, segments.length - matchingFirstSegments);

			StringBuilder builder = new StringBuilder();
			for (String s : newSegments) {
				builder.append('/');
				builder.append(s);
			}
			unixPath = builder.toString();
		} else {
			String device = path.getDevice().replace(':', ' ').trim();
			String[] segments = path.segments();
			String[] newSegments = new String[segments.length + 2];
			newSegments[0] = "cygdrive"; //$NON-NLS-1$
			newSegments[1] = device.toLowerCase();
			System.arraycopy(segments, 0, newSegments, 2, segments.length);

			StringBuilder builder = new StringBuilder();
			for (String s : newSegments) {
				builder.append('/');
				builder.append(s);
			}
			unixPath = builder.toString();
		}

		return unixPath;
	}
}
