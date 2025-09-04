/*******************************************************************************
 * Copyright (c) 2012, 2025 Andrew Gvozdev and others.
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
 *     John Dallaway - Support multiple MSYS2 64-bit registry names (#237)
 *     John Dallaway - Detect MSYS2 UCRT64 toolchains (#568)
 *     Tue Ton - Support for Windows on Arm64
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
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
	private static final Set<String> MSYS2_64BIT_NAMES = Set.of("MSYS2", "MSYS2 64bit"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final List<String> MSYS2_MINGW_SUBSYSTEMS = List.of("ucrt64", "mingw64", "mingw32"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final List<String> MSYS2_MINGW_SUBSYSTEM_SELECTION_TOOLS = List.of("clangd", "clang", "gcc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private static final boolean isWindowsPlatform = Platform.getOS().equals(Platform.OS_WIN32);

	private static String envPathValueCached = null;
	private static String envMinGWHomeValueCached = null;
	private static String minGWLocation = null;
	private static boolean isMinGWLocationCached = false;

	private static String envMinGWHomeValueCached_msys = null;
	private static String mSysLocation = null;
	private static boolean isMSysLocationCached = false;

	private final static Map<String/* envPath */, String/* mingwLocation */> mingwLocationCache = Collections
			.synchronizedMap(new WeakHashMap<>(1));

	/**
	 * @return The absolute path to MinGW root folder or {@code null} if not
	 *         found
	 */
	private static String findMinGWRoot(String envPathValue, String envMinGWHomeValue) {
		String rootValue = null;

		// Check $MINGW_HOME
		if (envMinGWHomeValue != null && !envMinGWHomeValue.isEmpty()) {
			IPath mingwBinDir = new Path(envMinGWHomeValue + "\\bin"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue = mingwBinDir.removeLastSegments(1).toOSString();
			}
		}

		// Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		if (rootValue == null) {
			IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
			IPath mingwBinDir = installPath.append("mingw\\bin"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue = mingwBinDir.removeLastSegments(1).toOSString();
			}
		}

		// Look in PATH values. Look for mingw32-gcc.exe or
		// x86_64-w64-mingw32-gcc.exe
		// or aarch64-w64-mingw32-gcc.exe
		if (rootValue == null) {
			rootValue = findMingwInPath(envPathValue);
		}

		// Look in MSYS2
		if (rootValue == null) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			boolean on64bit = Platform.getOSArch().equals(Platform.ARCH_X86_64)
					|| Platform.getOSArch().equals(Platform.ARCH_AARCH64);
			String key32bit = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if (displayName == null) {
					continue;
				}
				if (on64bit) {
					if (MSYS2_64BIT_NAMES.contains(displayName)) {
						String installLocation = registry.getCurrentUserValue(compKey, "InstallLocation"); //$NON-NLS-1$
						for (String subsys : MSYS2_MINGW_SUBSYSTEMS) {
							String mingwLocation = installLocation + "\\" + subsys; //$NON-NLS-1$
							for (String toolName : MSYS2_MINGW_SUBSYSTEM_SELECTION_TOOLS) {
								File toolFile = new File(mingwLocation + "\\bin\\" + toolName + ".exe"); //$NON-NLS-1$ //$NON-NLS-2$
								if (toolFile.canExecute()) {
									rootValue = mingwLocation;
									break;
								}
							}
							if (null != rootValue) {
								break;
							}
						}
						if (null != rootValue) {
							break;
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
							rootValue = mingwLocation;
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
					rootValue = mingwLocation;
				} else {
					mingwLocation = installLocation + "\\mingw32"; //$NON-NLS-1$
					gccFile = new File(mingwLocation + "\\bin\\gcc.exe"); //$NON-NLS-1$
					if (gccFile.canExecute()) {
						rootValue = mingwLocation;
					}
				}
			}
		}

		// Try the default MinGW install dir
		if (rootValue == null) {
			IPath mingwBinDir = new Path("C:\\MinGW"); //$NON-NLS-1$
			if (mingwBinDir.toFile().isDirectory()) {
				rootValue = mingwBinDir.toOSString();
			}
		}

		return rootValue;
	}

	private static Optional<String> findMinGwInstallationLocation(String exeName, String envPath) {
		IPath exeLocation = PathUtil.findProgramLocation(exeName, envPath);
		if (exeLocation != null) {
			return Optional.of(exeLocation.removeLastSegments(2).toOSString());
		}
		return Optional.empty();
	}

	private static String findMingwInPath(String envPath) {
		if (envPath == null) {
			// $PATH from user preferences
			IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH,
					(ICConfigurationDescription) null, true);
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
				mingwLocation = findMinGwInstallationLocation("x86_64-w64-mingw32-gcc.exe", envPath).orElse(null); //$NON-NLS-1$
			} else if (Platform.ARCH_AARCH64.equals(Platform.getOSArch())) {
				mingwLocation = findMinGwInstallationLocation("aarch64-w64-mingw32-gcc.exe", envPath).orElse(null); //$NON-NLS-1$
			}

			if (mingwLocation == null) {
				mingwLocation = findMinGwInstallationLocation("mingw32-gcc.exe", envPath).orElse(null); //$NON-NLS-1$
			}

			// Fallback: Look for paths containing "mingw"
			if (mingwLocation == null) {
				mingwLocation = findMinGwInstallationLocation("gcc.exe", envPath) //$NON-NLS-1$
						.filter(path -> path.toLowerCase().contains("mingw")) //$NON-NLS-1$
						.orElse(null);

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

		// Try under MSYS2
		if (msysHome == null) {
			// Give preference to msys64 on 64-bit platforms and ignore 64 on
			// 32-bit platforms
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			String uninstallKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"; //$NON-NLS-1$
			String subkey;
			boolean on64bit = Platform.getOSArch().equals(Platform.ARCH_X86_64)
					|| Platform.getOSArch().equals(Platform.ARCH_AARCH64);
			String key32bit = null;
			for (int i = 0; (subkey = registry.getCurrentUserKeyName(uninstallKey, i)) != null; i++) {
				String compKey = uninstallKey + '\\' + subkey;
				String displayName = registry.getCurrentUserValue(compKey, "DisplayName"); //$NON-NLS-1$
				if (displayName == null) {
					continue;
				}
				if (on64bit) {
					if (MSYS2_64BIT_NAMES.contains(displayName)) {
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
		if (!isWindowsPlatform) {
			return null;
		}

		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH,
				(ICConfigurationDescription) null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMinGWHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_MINGW_HOME, (ICConfigurationDescription) null, true);
		String envMinGWHomeValue = varMinGWHome != null ? varMinGWHome.getValue() : null;

		// isMinGWLocationCached is used to figure fact of caching when all
		// cached objects are null
		if (isMinGWLocationCached && Objects.equals(envPathValue, envPathValueCached)
				&& Objects.equals(envMinGWHomeValue, envMinGWHomeValueCached)) {
			return minGWLocation;
		}

		minGWLocation = findMinGWRoot(envPathValue, envMinGWHomeValue);
		envPathValueCached = envPathValue;
		envMinGWHomeValueCached = envMinGWHomeValue;
		isMinGWLocationCached = true;

		return minGWLocation;
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

		String envMinGWHomeValue = getMinGWHome();

		// isMSysLocationCached is used to figure whether it was cached when all
		// cached objects are null
		if (isMSysLocationCached && Objects.equals(envMinGWHomeValue, envMinGWHomeValueCached_msys)) {
			return mSysLocation;
		}

		mSysLocation = findMSysRoot(envMinGWHomeValue);
		envMinGWHomeValueCached_msys = envMinGWHomeValue;
		isMSysLocationCached = true;

		return mSysLocation;
	}

	/**
	 * Check if MinGW is available in the specified path or via known installation hints.
	 *
	 * @param envPath
	 *            - list of directories to search for MinGW separated by path
	 *            separator (format of environment variable $PATH) or
	 *            {@code null} to use current $PATH.
	 * @return {@code true} if MinGW is available, {@code false} otherwise.
	 */
	public static boolean isAvailable(String envPath) {
		return isWindowsPlatform && findMinGWRoot(envPath, null) != null;
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
