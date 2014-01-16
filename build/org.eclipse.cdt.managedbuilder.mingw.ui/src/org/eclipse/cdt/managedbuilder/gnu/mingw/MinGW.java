/*******************************************************************************
 * Copyright (c) 2012, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.mingw;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.mingw.ui.Activator;
import org.eclipse.cdt.managedbuilder.mingw.ui.preferences.PreferenceConstants;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * A collection of MinGW-related utility methods.
 */
public class MinGW {
	public static final String ENV_MINGW_HOME = "MINGW_HOME"; //$NON-NLS-1$
	public static final String ENV_MSYS_HOME = "MSYS_HOME"; //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static final boolean isWindowsPlatform = Platform.getOS().equals(Platform.OS_WIN32);

	private static String envPathValueCached = null;
	private static String envMinGWHomeValueCached = null;
	private static String minGWLocation = null;
	private static boolean isMinGWLocationCached = false;

	private static String envMinGWHomeValueCached_msys = null;
	private static String mSysLocation = null;
	private static boolean isMSysLocationCached = false;
	
	private final static Map<String/*envPath*/, String/*mingwLocation*/> mingwLocationCache = Collections.synchronizedMap(new WeakHashMap<String, String>(1));

	/**
	 * @return The absolute path to MinGW root folder or {@code null} if not found
	 */
	private static String findMinGWRoot(String envPathValue, String envMinGWHomeValue) {
		// Check $MINGW_HOME
		if (envMinGWHomeValue != null && !envMinGWHomeValue.isEmpty()) {
			IPath mingwBinDir = new Path(envMinGWHomeValue + "\\bin"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				return mingwBinDir.removeLastSegments(1).toOSString();
			}
		}

		// Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath mingwBinDir = installPath.append("mingw\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			return mingwBinDir.removeLastSegments(1).toOSString();
		}

		// Look in PATH values. Look for mingw32-gcc.exe or x86_64-w64-mingw32-gcc.exe
		String rootValue = findMingwInPath(envPathValue);
		if (rootValue != null) {
			return rootValue;
		}

		// Check preference
		String prefValue = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.MINGW_LOCATION);
		if (new File(prefValue).isDirectory()) {
			return prefValue;
		}

		// Try the default MinGW install dir
		mingwBinDir = new Path("C:\\MinGW"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			return mingwBinDir.toOSString();
		}

		return null;
	}

	private static String findMingwInPath(String envPath) {
		if (envPath == null) {
			// $PATH from user preferences
			IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH, null, true);
			if (varPath != null) {
				envPath = varPath.getValue();
			}
		}

		String mingwLocation = mingwLocationCache.get(envPath);
		// check if WeakHashMap contains the key as null may be the cached value
		if (mingwLocation == null && !mingwLocationCache.containsKey(envPath)) {
			// Check for MinGW-w64 on Windows 64 bit, see http://mingw-w64.sourceforge.net/
			if (Platform.ARCH_X86_64.equals(Platform.getOSArch())) {
				IPath gcc64Loc = PathUtil.findProgramLocation("x86_64-w64-mingw32-gcc.exe", envPath); //$NON-NLS-1$
				if (gcc64Loc != null) {
					mingwLocation  = gcc64Loc.removeLastSegments(2).toOSString();
				}
			}

			// Look for mingw32-gcc.exe
			if (mingwLocation == null) {
				IPath gccLoc = PathUtil.findProgramLocation("mingw32-gcc.exe", envPath); //$NON-NLS-1$
				if (gccLoc != null) {
					mingwLocation = gccLoc.removeLastSegments(2).toOSString();
				}
			}
			
			// The 32-bit mingw-w64 compiler (seen in Qt)
			if (mingwLocation == null) {
				IPath gccLoc = PathUtil.findProgramLocation("i686-w64-mingw32-gcc.exe", envPath); //$NON-NLS-1$
				if (gccLoc != null) {
					mingwLocation = gccLoc.removeLastSegments(2).toOSString();
				}
			}
			
			mingwLocationCache.put(envPath, mingwLocation);
		}

		return mingwLocation;
	}

	private static String findMSysRoot(String envMinGWHomeValue) {
		String msysHome = null;
	
		// Look in the install location parent dir
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath installMsysBin = installPath.append("msys\\bin"); //$NON-NLS-1$
		if (installMsysBin.toFile().isDirectory()) {
			msysHome = installMsysBin.removeLastSegments(1).toOSString();
		}
	
		// Look under $MINGW_HOME
		if (msysHome == null) {
			if (envMinGWHomeValue != null && !envMinGWHomeValue.isEmpty()) {
				IPath minGwMsysBin = new Path(envMinGWHomeValue + "\\msys\\1.0\\bin"); //$NON-NLS-1$
				if (minGwMsysBin.toFile().isDirectory()) {
					msysHome = minGwMsysBin.removeLastSegments(1).toOSString();
				}
			}
		}
	
		// Check preference
		String prefValue = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.MSYS_LOCATION);
		if (new File(prefValue).isDirectory()) {
			return prefValue;
		}

		// Try under default MinGW dir
		if (msysHome == null) {
			IPath minGwMsysBin = new Path("C:\\MinGW\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (minGwMsysBin.toFile().isDirectory()) {
				msysHome = minGwMsysBin.removeLastSegments(1).toOSString();
			}
		}
	
		// Try in default MSYS root folder
		if (msysHome == null) {
			IPath defaultMsysBin = new Path("C:\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (defaultMsysBin.toFile().isDirectory()) {
				msysHome = defaultMsysBin.removeLastSegments(1).toOSString();
			}
		}
		return msysHome;
	}

	/**
	 * Find location where MinGW is installed. A number of locations is being checked,
	 * such as environment variable $MINGW_HOME, $PATH, Windows registry et al.
	 * <br><br>
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 * 
	 * @return MinGW root ("/") path in Windows format.
	 */
	public static String getMinGWHome() {
		if (!isWindowsPlatform) {
			return null;
		}

		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH, null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMinGWHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_MINGW_HOME, null, true);
		String envMinGWHomeValue = varMinGWHome != null ? varMinGWHome.getValue() : null;

		 // isMinGWLocationCached is used to figure fact of caching when all cached objects are null
		if (isMinGWLocationCached && CDataUtil.objectsEqual(envPathValue, envPathValueCached) && CDataUtil.objectsEqual(envMinGWHomeValue, envMinGWHomeValueCached)) {
			return minGWLocation;
		}

		minGWLocation = findMinGWRoot(envPathValue, envMinGWHomeValue);
		envPathValueCached = envPathValue;
		envMinGWHomeValueCached = envMinGWHomeValue;
		isMinGWLocationCached = true;

		return minGWLocation;
	}

	/**
	 * Find location where MSys is installed. Environment variable $MSYS_HOME and
	 * some predetermined locations are being checked.
	 * <br><br>
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 * 
	 * @return MSys root ("/") path in Windows format.
	 */
	public static String getMSysHome() {
		if (!isWindowsPlatform) {
			return null;
		}

		// Use $MSYS_HOME if defined
		IEnvironmentVariable varMsysHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_MSYS_HOME, null, true);
		String msysHomeValue = varMsysHome != null ? varMsysHome.getValue() : null;
		if (msysHomeValue != null) {
			return msysHomeValue;
		}

		String envMinGWHomeValue = getMinGWHome();

		// isMSysLocationCached is used to figure whether it was cached when all cached objects are null
		if (isMSysLocationCached && CDataUtil.objectsEqual(envMinGWHomeValue, envMinGWHomeValueCached_msys)) {
			return mSysLocation;
		}

		mSysLocation = findMSysRoot(envMinGWHomeValue);
		envMinGWHomeValueCached_msys = envMinGWHomeValue;
		isMSysLocationCached = true;

		return mSysLocation;
	}

	/**
	 * Check if MinGW is available in the path.
	 *
	 * @param envPath - list of directories to search for MinGW separated
	 *    by path separator (format of environment variable $PATH)
	 *    or {@code null} to use current $PATH.
	 * @return {@code true} if MinGW is available, {@code false} otherwise.
	 */
	public static boolean isAvailable(String envPath) {
		return isWindowsPlatform && findMingwInPath(envPath) != null;
	}

	/**
	 * Check if MinGW is available in $PATH.
	 *
	 * @return {@code true} if MinGW is available, {@code false} otherwise.
	 */
	public static boolean isAvailable() {
		return isWindowsPlatform && findMingwInPath(null) != null;
	}
	
	public static void invalidateCache() {
		isMinGWLocationCached = false;
		isMSysLocationCached = false;
		mingwLocationCache.clear();
	}
}
