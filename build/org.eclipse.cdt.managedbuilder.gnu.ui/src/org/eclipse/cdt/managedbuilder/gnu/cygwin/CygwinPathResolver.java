/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Enrico Ehrich - http://bugs.eclipse.org/233866
 *     Marc-Andre Laperle - fix for Cygwin GCC is Not detected (bug 303900)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.gnu.ui.GnuUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CygwinPathResolver implements IBuildPathResolver {
	private static final String DEFAULT_ROOT = "C:\\cygwin"; //$NON-NLS-1$
	private static final String CYGWIN_DLL = "cygwin1.dll"; //$NON-NLS-1$
	private static final String TOOL = "/cygpath -w -p "; //$NON-NLS-1$
	private static final char BS = '\\';
	private static final char SLASH = '/';
	private static final String PROPERTY_OS_NAME = "os.name"; //$NON-NLS-1$
	private static final String OS_WINDOWS = "windows";//$NON-NLS-1$
	private static final String SP = " ";         //$NON-NLS-1$
	private static final String REGISTRY_KEY_SETUP = "SOFTWARE\\Cygwin\\setup"; //$NON-NLS-1$
	private static final String REGISTRY_KEY_SETUP_WIN64 = "SOFTWARE\\Wow6432Node\\Cygwin\\setup"; //$NON-NLS-1$
	// note that in Cygwin 1.7 the mount point storage has been moved out of the registry
	private static final String REGISTRY_KEY_MOUNTS = "SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\"; //$NON-NLS-1$
	private static final String PATH_NAME = "native";   //$NON-NLS-1$
	private static final String SSLASH = "/";           //$NON-NLS-1$
	private static final String BSLASH = "\\\\";        //$NON-NLS-1$
	private static final String BINPATTERN = "/usr/bin"; //$NON-NLS-1$
	private static final String BINPATTERN_ALTERNATE = "/bin"; //$NON-NLS-1$
	private static final String ETCPATTERN = "/etc";     //$NON-NLS-1$
	private static final String ROOTPATTERN = SSLASH;
	private static final String DELIMITER_UNIX = ":";    //$NON-NLS-1$
	private static final String DELIMITER_WIN  = ";";    //$NON-NLS-1$
	private static final String GCC_VERSION_CMD  = "gcc --version";    //$NON-NLS-1$
	private static final String MINGW_SPECIAL = "mingw ";    //$NON-NLS-1$
	private static final String CYGWIN_SPECIAL = "cygwin ";    //$NON-NLS-1$

	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static String envPathValueCached = null;
	private static String envCygwinHomeValueCached = null;
	private static String binCygwin  = null;
	private static String rootCygwin = null;
	private static String etcCygwin = null;

	@Override
	public String[] resolveBuildPaths(int pathType, String variableName, String variableValue, IConfiguration configuration) {
		if(!isWindows()) {
			return variableValue.split(DELIMITER_UNIX);
		} else if(isMinGW(configuration)) {
			return variableValue.split(DELIMITER_WIN);
		}

		String[] result = variableValue.split(DELIMITER_UNIX);
		String exePath = getBinPath();
		if (exePath == null) {
			return result; // no changes
		}
		File file = new File(exePath);
		if (!file.exists() || !file.isDirectory()) {
			return result; // no changes
		}
		String s = exePath + TOOL + variableValue;
		String[] lines = exec(s, configuration);
		if (lines != null && lines.length > 0) {
			result = lines[0].replace(BS,SLASH).split(DELIMITER_WIN);
		}
		return result;
	}

	/**
	 * @return "/etc" path in Windows format.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static String getEtcPath() {
		locateCygwin();
		return etcCygwin;
	}

	/**
	 * @return "/usr/bin" path in Windows format.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static String getBinPath() {
		locateCygwin();
		return binCygwin;
	}

	/**
	 * @return Cygwin root ("/") path in Windows format.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static String getRootPath() {
		locateCygwin();
		return rootCygwin;
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
		if (null != registry) {
			String s = registry.getCurrentUserValue(key, name);
			if(s == null)
				s = registry.getLocalMachineValue(key, name);

			if (s != null)
				return (s.replaceAll(BSLASH, SSLASH));
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
		if (rootCygwin != null) {
			String path = rootCygwin + relativePath;
			File file = new File(path);
			if (file.exists() && file.isDirectory()) {
				return (path.replaceAll(BSLASH, SSLASH));
			}
		}

		return null;
	}

	/**
	 * @return The absolute path to cygwin's root or null if not found
	 */
	private static String findRoot(String paths) {
		String rootValue = null;

		// Check $CYGWIN_HOME
		if (envCygwinHomeValueCached != null && !envCygwinHomeValueCached.isEmpty()) {
			IPath location = new Path(envCygwinHomeValueCached + "/bin/" + CYGWIN_DLL);
			if (location.toFile().exists()) {
				// deduct rootValue from "rootValue\bin\cygwin1.dll"
				rootValue = location.removeLastSegments(2).toOSString();
			}
		}

		// Look in PATH values. Look for cygwin1.dll
		if(rootValue == null) {
			IPath location = PathUtil.findProgramLocation(CYGWIN_DLL, paths);
			if (location != null) {
				// deduct rootValue from "rootValue\bin\cygwin1.dll"
				rootValue = location.removeLastSegments(2).toOSString();
			}
		}

		// Try to find the root dir in SOFTWARE\Cygwin\setup
		if(rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP, "rootdir"); //$NON-NLS-1$
		}

		// Try to find the root dir in SOFTWARE\Wow6432Node\Cygwin\setup
		if(rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP_WIN64, "rootdir"); //$NON-NLS-1$
		}

		// Try to find the root dir in SOFTWARE\Cygnus Solutions
		if (rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ROOTPATTERN, PATH_NAME);
		}

		// Try the default Cygwin install dir
		if(rootValue == null) {
			File file = new File(DEFAULT_ROOT);
			if (file.exists() && file.isDirectory())
				rootValue = DEFAULT_ROOT;
		}

		if(rootValue != null)
			rootValue = rootValue.replaceAll(BSLASH, SSLASH);

		return rootValue;
	}

	/**
	 * Finds Cygwin's paths and sets corresponding properties.
	 */
	private static synchronized void locateCygwin() {
		if (!isWindows()) {
			return;
		}

		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH, null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varCygwinHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable("CYGWIN_HOME", null, true); //$NON-NLS-1$
		String envCygwinHomeValue = varCygwinHome != null ? varCygwinHome.getValue() : null;

		if (CDataUtil.objectsEqual(envPathValue, envPathValueCached) && CDataUtil.objectsEqual(envCygwinHomeValue, envCygwinHomeValueCached)) {
			return;
		}

		envPathValueCached = envPathValue;
		envCygwinHomeValueCached = envCygwinHomeValue;

		etcCygwin  = null;
		binCygwin  = null;
		rootCygwin = null;

		rootCygwin = findRoot(envPathValue);

		// Try to find the paths by appending the patterns to the root dir
		etcCygwin = getPathFromRoot(ETCPATTERN);
		binCygwin = getPathFromRoot(BINPATTERN);
		if(binCygwin == null)
			binCygwin = getPathFromRoot(BINPATTERN_ALTERNATE);

		// Try to find the paths in SOFTWARE\\Cygnus Solutions
		if(etcCygwin == null)
			etcCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ETCPATTERN, PATH_NAME);
		if(binCygwin == null)
			binCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + BINPATTERN, PATH_NAME);

		envPathValueCached = envPathValue;
	}

	private static String[] exec(String cmd, IConfiguration cfg) {
		try {
			IEnvironmentVariable vars[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(cfg,true);
			String env[] = new String[vars.length];
			for(int i = 0; i < env.length; i++) {
				env[i] = vars[i].getName() + "="; //$NON-NLS-1$
				String value = vars[i].getValue();
				if(value != null)
					env[i] += value;
			}
			Process proc = ProcessFactory.getFactory().exec(cmd.split(SP), env);
			if (proc != null) {

				InputStream ein = proc.getInputStream();
				BufferedReader d1 = new BufferedReader(new InputStreamReader(ein));
				ArrayList<String> ls = new ArrayList<String>(10);
				String s;
				while ((s = d1.readLine() ) != null ) {
					ls.add(s);
				}
				ein.close();
				return ls.toArray(new String[0]);
			}
		} catch (IOException e) {
			GnuUIPlugin.getDefault().log(e);
		}
		return null;
	}

	public static boolean isMinGW(IConfiguration cfg) {
		String versionInfo[] = exec(GCC_VERSION_CMD, cfg);
		if(versionInfo != null) {
			for(int i = 0; i < versionInfo.length; i++) {
				if(versionInfo[i].indexOf(MINGW_SPECIAL) != -1)
					return true;
				else if(versionInfo[i].indexOf(CYGWIN_SPECIAL) != -1)
					return false;
			}
		}
		return false;
	}
}
