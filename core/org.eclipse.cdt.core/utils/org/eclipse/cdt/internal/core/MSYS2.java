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
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

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
 * A collection of MSYS2-related utility methods.
 */
public class MSYS2 {
	public static final String ENV_MSYS2_HOME = "MSYS2_HOME"; //$NON-NLS-1$
	public static final String ENV_MSYS_HOME = "MSYS_HOME"; //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static final boolean isWindowsPlatform = Platform.getOS().equals(Platform.OS_WIN32);

	private static String envPathValueCached = null;
	private static String envMSYS2HomeValueCached = null;
	private static String MSYS2Location = null;
	private static boolean isMSYS2LocationCached = false;

	private static String envMSYS2HomeValueCached_msys = null;
	private static String mSysLocation = null;
	private static boolean isMSysLocationCached = false;

	private final static Map<String/* envPath */, String/* MSYS2Location */> MSYS2LocationCache = Collections
			.synchronizedMap(new WeakHashMap<String, String>(1));

	/**
	 * @return The absolute path to MSYS2 root folder or {@code null} if not
	 *         found
	 */
	private static String findMSYS2Root(String envPathValue, String envMSYS2HomeValue) {
		String rootValue = null;

		// Check $MSYS2_HOME
		if (envMSYS2HomeValue != null && !envMSYS2HomeValue.isEmpty()) {
			IPath MSYS2BinDir = new Path(envMSYS2HomeValue + "\\bin"); //$NON-NLS-1$
			if (MSYS2BinDir.toFile().isDirectory()) {
				rootValue = MSYS2BinDir.removeLastSegments(1).toOSString();
			}
		}

		// Try the MSYS2 directory in the platform install directory
		// CDT distributions like Wascana may distribute MSYS2 like that
		if (rootValue == null) {
			IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
			IPath MSYS2BinDir = installPath.append("MSYS2\\bin"); //$NON-NLS-1$
			if (MSYS2BinDir.toFile().isDirectory()) {
				rootValue = MSYS2BinDir.removeLastSegments(1).toOSString();
			}
		}

		// Look in PATH values. Look for MSYS232-gcc.exe or
		// x86_64-w64-MSYS232-gcc.exe
		if (rootValue == null) {
			rootValue = findMSYS2InPath(envPathValue);
		}

		// Look in MSYS2
		if (rootValue == null) {
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
						String MSYS2Location = installLocation + "\\MSYS264"; //$NON-NLS-1$
						File gccFile = new File(MSYS2Location + "\\bin\\gcc.exe"); //$NON-NLS-1$
						if (gccFile.canExecute()) {
							rootValue = MSYS2Location;
							break;
						} else {
							MSYS2Location = installLocation + "\\MSYS232"; //$NON-NLS-1$
							gccFile = new File(MSYS2Location + "\\bin\\gcc.exe"); //$NON-NLS-1$
							if (gccFile.canExecute()) {
								rootValue = MSYS2Location;
								break;
							}
						}
					} else if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						key32bit = compKey;
					}
				} else {
					if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						String MSYS2Location = installLocation + "\\MSYS232"; //$NON-NLS-1$
						File gccFile = new File(MSYS2Location + "\\bin\\gcc.exe"); //$NON-NLS-1$
						if (gccFile.canExecute()) {
							rootValue = MSYS2Location;
							break;
						}
					}
				}
			}

			if (on64bit && key32bit != null) {
				String installLocation = registry.getCurrentUserValue(key32bit, "InstallLocation"); //$NON-NLS-1$
				String MSYS2Location = installLocation + "\\MSYS264"; //$NON-NLS-1$
				File gccFile = new File(MSYS2Location + "\\bin\\gcc.exe"); //$NON-NLS-1$
				if (gccFile.canExecute()) {
					rootValue = MSYS2Location;
				} else {
					MSYS2Location = installLocation + "\\MSYS232"; //$NON-NLS-1$
					gccFile = new File(MSYS2Location + "\\bin\\gcc.exe"); //$NON-NLS-1$
					if (gccFile.canExecute()) {
						rootValue = MSYS2Location;
					}
				}
			}
		}

		// Try the default MSYS2 install dir
		if (rootValue == null) {
			IPath MSYS2BinDir = new Path("C:\\MSYS2"); //$NON-NLS-1$
			if (MSYS2BinDir.toFile().isDirectory()) {
				rootValue = MSYS2BinDir.toOSString();
			}
		}

		return rootValue;
	}

	private static String findMSYS2InPath(String envPath) {
		if (envPath == null) {
			// $PATH from user preferences
			IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(ENV_PATH, (ICConfigurationDescription) null, true);
			if (varPath != null) {
				envPath = varPath.getValue();
			}
		}

		String MSYS2Location = MSYS2LocationCache.get(envPath);
		// check if WeakHashMap contains the key as null may be the cached value
		if (MSYS2Location == null && !MSYS2LocationCache.containsKey(envPath)) {
			// Check for MSYS2-w64 on Windows 64 bit, see
			// http://MSYS2-w64.sourceforge.net/
			if (Platform.ARCH_X86_64.equals(Platform.getOSArch())) {
				IPath gcc64Loc = PathUtil.findProgramLocation("x86_64-w64-MSYS232-gcc.exe", envPath); //$NON-NLS-1$
				if (gcc64Loc != null) {
					MSYS2Location = gcc64Loc.removeLastSegments(2).toOSString();
				}
			}

			// Look for MSYS232-gcc.exe
			if (MSYS2Location == null) {
				IPath gccLoc = PathUtil.findProgramLocation("MSYS232-gcc.exe", envPath); //$NON-NLS-1$
				if (gccLoc != null) {
					MSYS2Location = gccLoc.removeLastSegments(2).toOSString();
				}
			}
			MSYS2LocationCache.put(envPath, MSYS2Location);
		}

		return MSYS2Location;
	}

	private static String findMSysRoot(String envMSYS2HomeValue) {
		String msysHome = null;

		// Look in the install location parent dir
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath installMsysBin = installPath.append("msys\\bin"); //$NON-NLS-1$
		if (installMsysBin.toFile().isDirectory()) {
			msysHome = installMsysBin.removeLastSegments(1).toOSString();
		}

		// Look under $MSYS2_HOME
		if (msysHome == null) {
			if (envMSYS2HomeValue != null && !envMSYS2HomeValue.isEmpty()) {
				IPath MSYS2MsysBin = new Path(envMSYS2HomeValue + "\\msys\\1.0\\bin"); //$NON-NLS-1$
				if (MSYS2MsysBin.toFile().isDirectory()) {
					msysHome = MSYS2MsysBin.removeLastSegments(1).toOSString();
				}
			}
		}

		// Try under MSYS2
		if (msysHome == null) {
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
							msysHome = home;
							break;
						}
					} else if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						key32bit = compKey;
					}
				} else {
					if ("MSYS2 32bit".equals(displayName)) { //$NON-NLS-1$
						String home = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						if (new File(home).isDirectory()) {
							msysHome = home;
							break;
						}
					}
				}
			}

			if (on64bit && key32bit != null) {
				String home = registry.getCurrentUserValue(key32bit, "InstallLocation"); //$NON-NLS-1$
				if (new File(home).isDirectory()) {
					msysHome = home;
				}
			}
		}

		// Try under default MSYS2 dir
		if (msysHome == null) {
			IPath MSYS2MsysBin = new Path("C:\\MSYS2\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (MSYS2MsysBin.toFile().isDirectory()) {
				msysHome = MSYS2MsysBin.removeLastSegments(1).toOSString();
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
	 * Find location where MSYS2 is installed. A number of locations is being
	 * checked, such as environment variable $MSYS2_HOME, $PATH, Windows
	 * registry et al. <br>
	 * <br>
	 * If you use this do not cache results to ensure user preferences are
	 * accounted for. Please rely on internal caching.
	 * 
	 * @return MSYS2 root ("/") path in Windows format.
	 */
	public static String getMSYS2Home() {
		if (!isWindowsPlatform) {
			return null;
		}

		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_PATH, (ICConfigurationDescription) null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMSYS2Home = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_MSYS2_HOME, (ICConfigurationDescription) null, true);
		String envMSYS2HomeValue = varMSYS2Home != null ? varMSYS2Home.getValue() : null;

		// isMSYS2LocationCached is used to figure fact of caching when all
		// cached objects are null
		if (isMSYS2LocationCached && CDataUtil.objectsEqual(envPathValue, envPathValueCached)
				&& CDataUtil.objectsEqual(envMSYS2HomeValue, envMSYS2HomeValueCached)) {
			return MSYS2Location;
		}

		MSYS2Location = findMSYS2Root(envPathValue, envMSYS2HomeValue);
		envPathValueCached = envPathValue;
		envMSYS2HomeValueCached = envMSYS2HomeValue;
		isMSYS2LocationCached = true;

		return MSYS2Location;
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
		if (!isWindowsPlatform) {
			return null;
		}

		// Use $MSYS_HOME if defined
		IEnvironmentVariable varMsysHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_MSYS_HOME, (ICConfigurationDescription) null, true);
		String msysHomeValue = varMsysHome != null ? varMsysHome.getValue() : null;
		if (msysHomeValue != null) {
			return msysHomeValue;
		}

		String envMSYS2HomeValue = getMSYS2Home();

		// isMSysLocationCached is used to figure whether it was cached when all
		// cached objects are null
		if (isMSysLocationCached && CDataUtil.objectsEqual(envMSYS2HomeValue, envMSYS2HomeValueCached_msys)) {
			return mSysLocation;
		}

		mSysLocation = findMSysRoot(envMSYS2HomeValue);
		envMSYS2HomeValueCached_msys = envMSYS2HomeValue;
		isMSysLocationCached = true;

		return mSysLocation;
	}

	/**
	 * Check if MSYS2 is available in the path.
	 *
	 * @param envPath
	 *            - list of directories to search for MSYS2 separated by path
	 *            separator (format of environment variable $PATH) or
	 *            {@code null} to use current $PATH.
	 * @return {@code true} if MSYS2 is available, {@code false} otherwise.
	 */
	public static boolean isAvailable(String envPath) {
		return isWindowsPlatform && findMSYS2InPath(envPath) != null;
	}

	/**
	 * Check if MSYS2 is available in $PATH.
	 *
	 * @return {@code true} if MSYS2 is available, {@code false} otherwise.
	 */
	public static boolean isAvailable() {
		return isWindowsPlatform && findMSYS2InPath(null) != null;
	}
}
