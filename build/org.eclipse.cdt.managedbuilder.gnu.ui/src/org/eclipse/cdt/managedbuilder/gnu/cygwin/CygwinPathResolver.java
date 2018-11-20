/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Enrico Ehrich - http://bugs.eclipse.org/233866
 *     Marc-Andre Laperle - fix for Cygwin GCC is Not detected (bug 303900)
 *     Andrew Gvozdev - changes to recognize $CYGWIN_HOME
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.gnu.ui.GnuUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CygwinPathResolver implements IBuildPathResolver {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$
	private static final String DELIMITER_UNIX = ":"; //$NON-NLS-1$
	private static final String DELIMITER_WIN = ";"; //$NON-NLS-1$

	private static final String PROPERTY_OS_NAME = "os.name"; //$NON-NLS-1$
	private static final String OS_WINDOWS = "windows";//$NON-NLS-1$
	private static final char SLASH = '/';
	private static final char BACKSLASH = '\\';

	private static final String CYGPATH_PATH_LIST_TO_WINDOWS = "cygpath -w -p "; //$NON-NLS-1$

	// note that in Cygwin 1.7 the mount point storage has been moved out of the registry
	private static final String REGISTRY_KEY_MOUNTS = "SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\"; //$NON-NLS-1$
	private static final String PATH_NAME = "native"; //$NON-NLS-1$
	private static final String BINPATTERN = "/usr/bin"; //$NON-NLS-1$
	private static final String BINPATTERN_ALTERNATE = "/bin"; //$NON-NLS-1$
	private static final String ETCPATTERN = "/etc"; //$NON-NLS-1$

	private static final String GCC_VERSION_CMD = "gcc --version"; //$NON-NLS-1$
	private static final String MINGW_SPECIAL = "mingw "; //$NON-NLS-1$
	private static final String CYGWIN_SPECIAL = "cygwin "; //$NON-NLS-1$

	@Override
	public String[] resolveBuildPaths(int pathType, String variableName, String variableValue,
			IConfiguration configuration) {
		if (!isWindows()) {
			return variableValue.split(DELIMITER_UNIX);
		} else if (isMinGW(configuration)) {
			return variableValue.split(DELIMITER_WIN);
		}

		String[] lines = executeInConfigurationContext(CYGPATH_PATH_LIST_TO_WINDOWS + variableValue, configuration);
		if (lines != null && lines.length > 0) {
			String pathList = lines[0].replace(BACKSLASH, SLASH);
			return pathList.split(DELIMITER_WIN);
		}

		return variableValue.split(DELIMITER_UNIX);
	}

	/**
	 * @return "/etc" path in Windows format for workspace.
	 * @deprecated. Deprecated as of CDT 8.2. Note that Cygwin root path in general may depend on configuration.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	@Deprecated
	public static String getEtcPath() {
		String etcCygwin = getPathFromRoot(ETCPATTERN);
		// Try to find the paths in SOFTWARE\\Cygnus Solutions
		if (etcCygwin == null) {
			etcCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ETCPATTERN, PATH_NAME);
		}
		return etcCygwin;
	}

	/**
	 * @return "/usr/bin" path in Windows format for workspace.
	 * @deprecated. Deprecated as of CDT 8.2. Note that Cygwin root path in general may depend on configuration.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	@Deprecated
	public static String getBinPath() {
		String binCygwin = getPathFromRoot(BINPATTERN);
		if (binCygwin == null) {
			binCygwin = getPathFromRoot(BINPATTERN_ALTERNATE);
		}
		// Try to find the paths in SOFTWARE\\Cygnus Solutions
		if (binCygwin == null) {
			binCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + BINPATTERN, PATH_NAME);
		}
		return binCygwin;
	}

	/**
	 * @return Cygwin root ("/") path in Windows format for workspace.
	 * @deprecated. Deprecated as of CDT 8.2. Note that Cygwin root path in general may depend on configuration.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	@Deprecated
	public static String getRootPath() {
		return Cygwin.getCygwinHome();
	}

	public static boolean isWindows() {
		return (System.getProperty(PROPERTY_OS_NAME).toLowerCase().startsWith(OS_WINDOWS));
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
			String value = registry.getCurrentUserValue(key, name);
			if (value == null) {
				value = registry.getLocalMachineValue(key, name);
			}
			if (value != null) {
				return value.replace(BACKSLASH, SLASH);
			}
		}
		return null;
	}

	/**
	 * Returns the absolute path of the pattern by simply appending the relativePath to the root.
	 *
	 * @param relativePath - the pattern to find.
	 * @return The absolute path to the pattern or {@code null} if path does not exist.
	 */
	private static String getPathFromRoot(String relativePath) {
		String rootCygwin = Cygwin.getCygwinHome();
		if (rootCygwin != null) {
			String path = rootCygwin + relativePath;
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				return path.replace(BACKSLASH, SLASH);
			}
		}
		return null;
	}

	/**
	 * Resolve and return full path to program in context of configuration.
	 *
	 * @param program - program to resolve.
	 * @param cfg - configuration context.
	 * @return absolute path to program.
	 */
	private static String resolveProgram(String program, IConfiguration cfg) {
		String envPathValue = null;
		try {
			IEnvironmentVariable envPathVar = ManagedBuildManager.getEnvironmentVariableProvider().getVariable(ENV_PATH,
					cfg, true);
			if (envPathVar != null) {
				envPathValue = envPathVar.getValue();
				IPath progPath = PathUtil.findProgramLocation(program, envPathValue);
				if (progPath != null) {
					program = progPath.toOSString();
				}
				// this resolves cygwin symbolic links
				program = Cygwin.cygwinToWindowsPath(program, envPathValue);
			}
		} catch (Exception e) {
			GnuUIPlugin.getDefault().log(new Status(IStatus.WARNING, GnuUIPlugin.PLUGIN_ID,
					"Problem trying to find program [" + program + "] in $PATH=[" + envPathValue + "]", e));
		}
		return program;
	}

	/**
	 * Return environment in envp format, see {@link Runtime#exec(String, String[])}.
	 *
	 * @param cfg - configuration.
	 * @return environment as array of strings in format name=value.
	 */
	private static String[] getEnvp(IConfiguration cfg) {
		IEnvironmentVariable vars[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(cfg, true);
		String envp[] = new String[vars.length];
		for (int i = 0; i < envp.length; i++) {
			envp[i] = vars[i].getName() + '=';
			String value = vars[i].getValue();
			if (value != null)
				envp[i] += value;
		}
		return envp;
	}

	/**
	 * Execute command taking in account configuration environment.
	 *
	 * @param cmd - command to execute.
	 * @param cfg - configuration context.
	 * @return command output as string array.
	 */
	private static String[] executeInConfigurationContext(String cmd, IConfiguration cfg) {
		String[] args = cmd.split(" "); //$NON-NLS-1$
		args[0] = resolveProgram(args[0], cfg);

		String[] result = null;
		try {
			String[] envp = getEnvp(cfg);
			Process proc = ProcessFactory.getFactory().exec(args, envp);
			if (proc != null) {
				InputStream ein = proc.getInputStream();
				try {
					BufferedReader d1 = new BufferedReader(new InputStreamReader(ein));
					ArrayList<String> ls = new ArrayList<>(10);
					String s;
					while ((s = d1.readLine()) != null) {
						ls.add(s);
					}
					result = ls.toArray(new String[0]);
				} finally {
					ein.close();
				}
			}
		} catch (IOException e) {
			GnuUIPlugin.getDefault()
					.log(new Status(IStatus.ERROR, GnuUIPlugin.PLUGIN_ID, "Error executing program [" + cmd + "]", e));
		}
		return result;
	}

	public static boolean isMinGW(IConfiguration cfg) {
		String versionInfo[] = executeInConfigurationContext(GCC_VERSION_CMD, cfg);
		if (versionInfo != null) {
			for (int i = 0; i < versionInfo.length; i++) {
				if (versionInfo[i].indexOf(MINGW_SPECIAL) != -1)
					return true;
				else if (versionInfo[i].indexOf(CYGWIN_SPECIAL) != -1)
					return false;
			}
		}
		return false;
	}
}
