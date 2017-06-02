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

package org.eclipse.cdt.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
	private static ArrayList<String> allMinGWLocations = null;
	private static boolean isMinGWLocationCached = false;

	private static String envMinGWHomeValueCached_msys = null;
	private static String mSysLocation = null;
	private static ArrayList<String> allmSysLocations = null;
	private static boolean isMSysLocationCached = false;

	private final static Map<String/* envPath */, String/* mingwLocation */> mingwLocationCache = Collections
			.synchronizedMap(new WeakHashMap<String, String>(1));

	/**
	 * @return The absolute path to MinGW root folder or {@code null} if not
	 *         found
	 */
	private static ArrayList<String> findMinGWRoot(String envPathValue, String envMinGWHomeValue) {
		ArrayList<String> rootValue = new ArrayList<String>();

		// Check $MINGW_HOME
		if (envMinGWHomeValue != null && !envMinGWHomeValue.isEmpty()) {
			IPath mingwBinDir = new Path(envMinGWHomeValue + "\\bin"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue.add(mingwBinDir.removeLastSegments(1).toOSString());
			}
		}

		// Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		{
			IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
			IPath mingwBinDir = installPath.append("mingw\\bin"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue.add(mingwBinDir.removeLastSegments(1).toOSString());
			}
		}

		// Look in PATH values. Look for mingw32-gcc.exe or
		// x86_64-w64-mingw32-gcc.exe
		{
			rootValue.add(findMingwInPath(envPathValue));
		}

		// Look in MSYS2
		{
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			boolean on64bit = Platform.getOSArch().equals(Platform.ARCH_X86_64);
			String key32bit = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if (on64bit) {
					if ("MSYS2 64bit".equals(displayName)) { //$NON-NLS-1$
						String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						String mingwLocation = installLocation + "\\mingw64"; //$NON-NLS-1$
						File gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
						if (gccFile.canExecute()) {
							rootValue.add(mingwLocation);
							break;
						} else {
							mingwLocation = installLocation + "\\mingw32"; //$NON-NLS-1$
							gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
							if (gccFile.canExecute()) {
								rootValue.add(mingwLocation);
								break;
							}
						}
					} else if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						key32bit = compKey;
					}
				} else {
					if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						String mingwLocation = installLocation + "\\mingw32"; //$NON-NLS-1$
						File gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
						if (gccFile.canExecute()) {
							rootValue.add(mingwLocation);
							break;
						}
					}
				}
			}

			if (on64bit && key32bit != null) {
				String installLocation = registry.getCurrentUserValue(key32bit, "InstallLocation"); //$NON-NLS-1$
				String mingwLocation = installLocation + "\\mingw64"; //$NON-NLS-1$
				File gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
				if (gccFile.canExecute()) {
					rootValue.add(mingwLocation);
				} else {
					mingwLocation = installLocation + "\\mingw32"; //$NON-NLS-1$
					gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
					if (gccFile.canExecute()) {
						rootValue.add(mingwLocation);
					}
				}
			}
		}

		// Try the default MinGW install dir
		{
			IPath mingwBinDir = new Path("C:\\MinGW"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue.add(mingwBinDir.toOSString());
			}
		}

		return rootValue;
	}

	private static String findMingwInPath(String envPath) {
		if (envPath == null) {
			// $PATH from user preferences
			IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(ENV_PATH, (ICConfigurationDescription) null, true);
			if (varPath != null) {
				envPath = varPath.getValue();
			}
		}

		String mingwLocation = mingwLocationCache.get(envPath);
		// check if WeakHashMap contains the key as null may be the cached value
		if (mingwLocation == null && !mingwLocationCache.containsKey(envPath)) {
			// Check for MinGW-w64 on Windows 64 bit, see
			// http://mingw-w64.sourceforge.net/
			if (Platform.ARCH_X86_64.equals(Platform.getOSArch())) {
				IPath gcc64Loc = PathUtil.findProgramLocation("x86_64-w64-mingw32-gcc.exe", envPath); //$NON-NLS-1$
				if (gcc64Loc != null) {
					mingwLocation = gcc64Loc.removeLastSegments(2).toOSString();
				}
			}

			// Look for mingw32-gcc.exe
			if (mingwLocation == null) {
				IPath gccLoc = PathUtil.findProgramLocation("mingw32-gcc.exe", envPath); //$NON-NLS-1$
				if (gccLoc != null) {
					mingwLocation = gccLoc.removeLastSegments(2).toOSString();
				}
			}
			mingwLocationCache.put(envPath, mingwLocation);
		}

		return mingwLocation;
	}

	private static ArrayList<String> findMSysRoot(ArrayList<String> envMinGWHomeValue) {
		ArrayList<String> msysHome = new ArrayList<String>();

		// Look in the install location parent dir
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath installMsysBin = installPath.append("msys\\bin"); //$NON-NLS-1$
		if (installMsysBin.toFile().isDirectory()) {
			msysHome.add(installMsysBin.removeLastSegments(1).toOSString());
		}

		// Look under $MINGW_HOME
		{
			if (allMinGWLocations.size() > 0) {
				for (String string : envMinGWHomeValue) {
					IPath minGwMsysBin = new Path(envMinGWHomeValue + "\\msys\\1.0\\bin"); //$NON-NLS-1$
					if (minGwMsysBin.toFile().isDirectory()) {
						msysHome.add(minGwMsysBin.removeLastSegments(1).toOSString());
					}
				}
			}
		}

		// Try under MSYS2
		{
			// Give preference to msys64 on 64-bit platforms and ignore 64 on
			// 32-bit platforms
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			boolean on64bit = Platform.getOSArch().equals(Platform.ARCH_X86_64);
			String key32bit = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if (on64bit) {
					if ("MSYS2 64bit".equals(displayName)) { //$NON-NLS-1$
						String home = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						if (new File(home).isDirectory()) {
							msysHome.add(home);
							break;
						}
					} else if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						key32bit = compKey;
					}
				} else {
					if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						String home = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						if (new File(home).isDirectory()) {
							msysHome.add(home);
							break;
						}
					}
				}
			}

			if (on64bit && key32bit != null) {
				String home = registry.getCurrentUserValue(key32bit, "InstallLocation"); //$NON-NLS-1$
				if (new File(home).isDirectory()) {
					msysHome.add(home);
				}
			}
		}

		// Try under default MinGW dir
		{
			IPath minGwMsysBin = new Path("C:\\MinGW\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (minGwMsysBin.toFile().isDirectory()) {
				msysHome.add(minGwMsysBin.removeLastSegments(1).toOSString());
			}
		}

		// Try in default MSYS root folder
		{
			IPath defaultMsysBin = new Path("C:\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (defaultMsysBin.toFile().isDirectory()) {
				msysHome.add(defaultMsysBin.removeLastSegments(1).toOSString());
			}
		}
		return msysHome;
	}

	/**
	 * Find location where MinGW is installed. A number of locations is being
	 * checked, such as environment variable $MINGW_HOME, $PATH, Windows
	 * registry et al. <br>
	 * <br>
	 * If you use this do not cache results to ensure user preferences are
	 * accounted for. Please rely on internal caching.
	 * 
	 * @return MinGW root ("/") path in Windows format.
	 */
	public static String getMinGWHome() {
		if(minGWLocation != null && !minGWLocation.equals("")) //$NON-NLS-1$
			return minGWLocation;
		else
			return getAllMinGWHome().get(0); 
	}
	
	public static void changeMinGWDefaultLocation(String location)
	{
		minGWLocation = location;
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID)
		.put(CCorePreferenceConstants.MINGW_LOCATION, location);
	}
	
	public static ArrayList<String> getAllMinGWHome() {
		if (!isWindowsPlatform) {
			return null;
		}
		
		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_PATH, (ICConfigurationDescription) null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMinGWHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_MINGW_HOME, (ICConfigurationDescription) null, true);
		String envMinGWHomeValue = varMinGWHome != null ? varMinGWHome.getValue() : null;

		if (isMinGWLocationCached && CDataUtil.objectsEqual(envPathValue, envPathValueCached)
				&& CDataUtil.objectsEqual(envMinGWHomeValue, envMinGWHomeValueCached)) {
			return allMinGWLocations;
		}

		allMinGWLocations = findMinGWRoot(envPathValue, envMinGWHomeValue);
		minGWLocation = DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).get(
				CCorePreferenceConstants.MINGW_LOCATION,
				CCorePreferenceConstants.MINGW_LOCATION);
		envPathValueCached = envPathValue;
		envMinGWHomeValueCached = envMinGWHomeValue;
		isMinGWLocationCached = true;

		return allMinGWLocations;
	}

	/**
	 * Find location where MSys is installed. Environment variable $MSYS_HOME
	 * and some predetermined locations are being checked. <br>
	 * <br>
	 * If you use this do not cache results to ensure user preferences are
	 * accounted for. Please rely on internal caching.
	 * 
	 * @return MSys root ("/") path in Windows format.
	 */
	public static String getMSysHome() {
		return mSysLocation;
	}
	
	public static ArrayList<String> getAllMsysHomes() {
		if (!isWindowsPlatform) {
			return null;
		}

		// Use $MSYS_HOME if defined
		IEnvironmentVariable varMsysHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_MSYS_HOME, (ICConfigurationDescription) null, true);
		ArrayList<String> msysHomeValue = new ArrayList<String>();
		msysHomeValue.add(varMsysHome != null ? varMsysHome.getValue() : null);
		if (msysHomeValue != null) {
			return msysHomeValue;
		}

		String envMinGWHomeValue = getMinGWHome();

		// isMSysLocationCached is used to figure whether it was cached when all
		// cached objects are null
		if (isMSysLocationCached && CDataUtil.objectsEqual(envMinGWHomeValue, envMinGWHomeValueCached_msys)) {
			return allmSysLocations;
		}

		allmSysLocations = findMSysRoot(allMinGWLocations);
		if(allmSysLocations.size()>0)
			mSysLocation = allmSysLocations.get(0);
		else minGWLocation = null;
		envMinGWHomeValueCached_msys = envMinGWHomeValue;
		isMSysLocationCached = true;

		return allmSysLocations;
	}

	/**
	 * Check if MinGW is available in the path.
	 *
	 * @param envPath
	 *            - list of directories to search for MinGW separated by path
	 *            separator (format of environment variable $PATH) or
	 *            {@code null} to use current $PATH.
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
}
