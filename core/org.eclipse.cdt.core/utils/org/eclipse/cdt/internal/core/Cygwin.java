/*******************************************************************************
 * Copyright (c) 2012, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * A collection of cygwin-related utilities.
 */
public class Cygwin {
	public static final String ENV_CYGWIN_HOME = "CYGWIN_HOME"; //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static final String CYGPATH = "cygpath"; //$NON-NLS-1$
	private static final String DEFAULT_ROOT = "C:\\cygwin"; //$NON-NLS-1$
	private static final String CYGWIN_DLL = "cygwin1.dll"; //$NON-NLS-1$
	private static final String REGISTRY_KEY_SETUP = "SOFTWARE\\Cygwin\\setup"; //$NON-NLS-1$
	private static final String REGISTRY_KEY_SETUP_WIN64 = "SOFTWARE\\Wow6432Node\\Cygwin\\setup"; //$NON-NLS-1$
	// note that in Cygwin 1.7 the mount point storage has been moved out of the registry
	private static final String REGISTRY_KEY_MOUNTS = "SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\"; //$NON-NLS-1$
	private static final String PATH_NAME = "native"; //$NON-NLS-1$
	private static final String ROOTPATTERN = "/"; //$NON-NLS-1$
	private static final char SLASH = '/';
	private static final char BACKSLASH = '\\';

	private static final boolean isWindowsPlatform = Platform.getOS().equals(Platform.OS_WIN32);

	private static String envPathValueCached = null;
	private static String envCygwinHomeValueCached = null;
	private static String cygwinLocation = null;
	private static boolean isCygwinLocationCached = false;

	private final static Map<String/*envPath*/, String /*cygpathLocation*/> cygpathLocationCache = Collections
			.synchronizedMap(new LRUCache<String, String>(1, 20));
	private final static Map<String/*command*/, String /*translatedPath*/> translatedPathsCache = Collections
			.synchronizedMap(new LRUCache<String, String>(10, 500));

	/**
	 * Find location of "cygpath" utility on the file system.
	 */
	private static String findCygpathLocation(String envPath) {
		if (envPath == null) {
			// $PATH from user preferences
			IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH,
					(ICConfigurationDescription) null, true);
			if (varPath != null) {
				envPath = varPath.getValue();
			}
		}

		String cygpathLocation = cygpathLocationCache.get(envPath);
		if (cygpathLocation == null) {
			IPath loc = PathUtil.findProgramLocation(CYGPATH, envPath);
			cygpathLocation = loc != null ? loc.toOSString() : null;
			cygpathLocationCache.put(envPath, cygpathLocation);
		}
		return cygpathLocation;
	}

	/**
	 * Check if cygwin path conversion utilities are available in the path.
	 * Tells whether cygwin is installed in the path.
	 *
	 * @param envPath - list of directories to search for cygwin utilities separated
	 *    by path separator (format of environment variable $PATH)
	 *    or {@code null} to use current $PATH.
	 * @return {@code true} if cygwin is available, {@code false} otherwise.
	 */
	public static boolean isAvailable(String envPath) {
		return isWindowsPlatform && findCygpathLocation(envPath) != null;
	}

	/**
	 * Check if cygwin path conversion utilities are available in $PATH.
	 * Tells whether cygwin is installed in the path.
	 *
	 * @return {@code true} if cygwin is available, {@code false} otherwise.
	 */
	public static boolean isAvailable() {
		return isWindowsPlatform && findCygpathLocation(null) != null;
	}

	/**
	 * Run program (assuming cygpath) and return the translated path which is the first line of output.
	 */
	private static String runCygpath(String[] args) throws IOException {
		String command = getCommand(args);
		String translatedPath = translatedPathsCache.get(command);
		if (translatedPath == null) {
			Process cygpathProcess = Runtime.getRuntime().exec(args);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpathProcess.getInputStream()));
			String firstLine = null;
			try {
				firstLine = stdout.readLine();
			} finally {
				stdout.close();
			}
			if (firstLine == null) {
				throw new IOException("Unable read output from command=[" + command + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			translatedPath = firstLine.trim();
			translatedPathsCache.put(command, translatedPath);
		}

		return translatedPath;
	}

	/**
	 * Construct a command from arguments array.
	 */
	private static String getCommand(String[] args) {
		String command = ""; //$NON-NLS-1$
		for (String arg : args) {
			command = command + arg + ' ';
		}
		return command.trim();
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 * Note that there is no need to cache results, they are already cached internally.
	 *
	 * @param cygwinPath - cygwin path.
	 * @param envPath - list of directories to search for cygwin utilities separated
	 *    by path separator (format of environment variable $PATH).
	 * @return Windows style converted path. Note that that also converts cygwin links to their targets.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String cygwinToWindowsPath(String cygwinPath, String envPath)
			throws IOException, UnsupportedOperationException {
		if (cygwinPath == null || cygwinPath.trim().length() == 0)
			return cygwinPath;

		if (!isWindowsPlatform) {
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable."); //$NON-NLS-1$
		}

		String cygpathLocation = findCygpathLocation(envPath);
		if (cygpathLocation == null) {
			throw new UnsupportedOperationException(CYGPATH + " is not in the system search path."); //$NON-NLS-1$
		}

		String windowsPath = runCygpath(new String[] { cygpathLocation, "-w", cygwinPath }); //$NON-NLS-1$
		return windowsPath;
	}

	/**
	 * Conversion from Cygwin path to Windows path.
	 * Note that there is no need to cache results, they are already cached internally.
	 *
	 * @param cygwinPath - cygwin path.
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
	 * Note that there is no need to cache results, they are already cached internally.
	 *
	 * @param windowsPath - Windows path.
	 * @param envPath - list of directories to search for cygwin utilities (value of environment variable $PATH).
	 * @return Cygwin style converted path.
	 *
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	public static String windowsToCygwinPath(String windowsPath, String envPath)
			throws IOException, UnsupportedOperationException {
		if (windowsPath == null || windowsPath.trim().length() == 0)
			return windowsPath;

		if (!isWindowsPlatform) {
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable."); //$NON-NLS-1$
		}

		String cygpathLocation = findCygpathLocation(envPath);
		if (cygpathLocation == null) {
			throw new UnsupportedOperationException(CYGPATH + " is not in the system search path."); //$NON-NLS-1$
		}

		String cygwinPath = runCygpath(new String[] { cygpathLocation, "-u", windowsPath }); //$NON-NLS-1$
		return cygwinPath;
	}

	/**
	 * Conversion from Windows path to Cygwin path.
	 * Note that there is no need to cache results, they are already cached internally.
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

	/**
	 * Find location where Cygwin is installed. A number of locations is being checked,
	 * such as environment variable $CYGWIN_HOME, $PATH, Windows registry et al.
	 * <br><br>
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 *
	 * @return Location of Cygwin root folder "/" on file system in Windows format.
	 */
	public static String getCygwinHome() {
		if (!isWindowsPlatform) {
			return null;
		}

		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH,
				(ICConfigurationDescription) null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varCygwinHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_CYGWIN_HOME, (ICConfigurationDescription) null, true);
		String envCygwinHomeValue = varCygwinHome != null ? varCygwinHome.getValue() : null;

		// isCygwinLocationCached is used to figure fact of caching when all cached objects are null
		if (isCygwinLocationCached && CDataUtil.objectsEqual(envPathValue, envPathValueCached)
				&& CDataUtil.objectsEqual(envCygwinHomeValue, envCygwinHomeValueCached)) {
			return cygwinLocation;
		}

		cygwinLocation = findCygwinRoot(envPathValue, envCygwinHomeValue);

		envPathValueCached = envPathValue;
		envCygwinHomeValueCached = envCygwinHomeValue;
		isCygwinLocationCached = true;

		return cygwinLocation;
	}

	/**
	 * Reads required value from registry. Looks in both
	 * HKEY_CURRENT_USER and HKEY_LOCAL_MACHINE
	 *
	 * @param key Registry key
	 * @param name Registry value to read
	 * @return corresponding string value or null if nothing found
	 */
	private static String readValueFromRegistry(String key, String name) {
		WindowsRegistry registry = WindowsRegistry.getRegistry();
		if (registry != null) {
			String s = registry.getCurrentUserValue(key, name);
			if (s == null) {
				s = registry.getLocalMachineValue(key, name);
			}

			if (s != null) {
				return (s.replace(BACKSLASH, SLASH));
			}
		}
		return null;
	}

	/**
	 * @return The absolute path to cygwin's root or null if not found
	 */
	private static String findCygwinRoot(String envPathValue, String envCygwinHomeValue) {
		String rootValue = null;

		// Check $CYGWIN_HOME
		if (envCygwinHomeValue != null && !envCygwinHomeValue.isEmpty()) {
			IPath location = new Path(envCygwinHomeValue + "/bin/" + CYGWIN_DLL); //$NON-NLS-1$
			if (location.toFile().exists()) {
				// get rootValue from "rootValue\bin\cygwin1.dll"
				rootValue = location.removeLastSegments(2).toOSString();
			}
		}

		// Look in PATH values. Look for cygwin1.dll
		if (rootValue == null) {
			IPath location = PathUtil.findProgramLocation(CYGWIN_DLL, envPathValue);
			if (location != null) {
				// get rootValue from "rootValue\bin\cygwin1.dll"
				rootValue = location.removeLastSegments(2).toOSString();
			}
		}

		// Try to find the root dir in SOFTWARE\Cygwin\setup
		if (rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP, "rootdir"); //$NON-NLS-1$
		}

		// Try to find the root dir in SOFTWARE\Wow6432Node\Cygwin\setup
		if (rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP_WIN64, "rootdir"); //$NON-NLS-1$
		}

		// Try to find the root dir in SOFTWARE\Cygnus Solutions
		if (rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ROOTPATTERN, PATH_NAME);
		}

		// Try the default Cygwin install dir
		if (rootValue == null) {
			File file = new File(DEFAULT_ROOT);
			if (file.exists() && file.isDirectory())
				rootValue = DEFAULT_ROOT;
		}

		if (rootValue != null) {
			rootValue = rootValue.replace(BACKSLASH, SLASH);
		}

		return rootValue;
	}

}
