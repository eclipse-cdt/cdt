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

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.gnu.ui.GnuUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;


/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CygwinPathResolver implements IBuildPathResolver {
	private static final String DEFAULT_ROOT = "C:\\cygwin"; //$NON-NLS-1$
	private static final String TOOL = "/cygpath -w -p "; //$NON-NLS-1$
	private static final char BS = '\\';
	private static final char SLASH = '/';
	private static final String PROPERTY_OS_NAME = "os.name"; //$NON-NLS-1$
	private static final String PROPERTY_OS_VALUE = "windows";//$NON-NLS-1$
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

	private static boolean checked = false;
	private static String binCygwin  = null;
	private static String rootCygwin = null;
	private static String etcCygwin = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildPathResolver#resolveBuildPaths(int, java.lang.String, java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration)
	 */
	@Override
	public String[] resolveBuildPaths(int pathType, String variableName,
			String variableValue, IConfiguration configuration) {

		if(!isWindows()){
			return variableValue.split(DELIMITER_UNIX);
		} else if(isMinGW(configuration)){
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
	/*
	 * returns "/etc" path in Windows format
	 */
	public static String getEtcPath() {
		if (!checked) findPaths();
		return etcCygwin;
	}

	/*
	 * returns "/usr/bin" path in Windows format
	 */
	public static String getBinPath() {
		if (!checked) findPaths();
		return binCygwin;
	}
	/*
	 * returns Cygwin root ("/") path in Windows format
	 */

	public static String getRootPath() {
		if (!checked) findPaths();
		return rootCygwin;
	}

	public static boolean isWindows() {
		return (System.getProperty(PROPERTY_OS_NAME).toLowerCase().startsWith(PROPERTY_OS_VALUE));
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
	 *  Returns the absolute path of the pattern by
	 *  simply appending the pattern to the root
	 *
	 * @param pattern The pattern to find
	 * @return The absolute path to the pattern or null if pattern is not found
	 */
	private static String getValueFromRoot(String pattern) {
		if (rootCygwin != null) {
			String path = rootCygwin + pattern;
			File file = new File(path);
			if (file.exists() && file.isDirectory())
				return (path.replaceAll(BSLASH, SSLASH));
			else
				return null;
		}

		return null;
	}

	/**
	 * Returns the absolute path to cygwin's root
	 *
	 * @return The absolute path to cygwin's root or null if not found
	 */
	private static String findRoot() {
		String rootValue = null;

		// 1. Look in PATH values. Look for bin\cygwin1.dll
		IPath location = PathUtil.findProgramLocation("cygwin1.dll"); //$NON-NLS-1$
		if (location!=null) {
			rootValue = location.removeLastSegments(2).toOSString();
		}

		// 2. Try to find the root dir in SOFTWARE\Cygwin\setup
		if(rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP, "rootdir"); //$NON-NLS-1$
		}

		// 3. Try to find the root dir in SOFTWARE\Wow6432Node\Cygwin\setup
		if(rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_SETUP_WIN64, "rootdir"); //$NON-NLS-1$
		}

		// 4. Try to find the root dir in SOFTWARE\Cygnus Solutions
		if (rootValue == null) {
			rootValue = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ROOTPATTERN, PATH_NAME);
		}

		// 5. Try the default Cygwin install dir
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
	 * Finds Cygwin's paths and sets corresponding properties
	 */
	private static synchronized void findPaths() {
		if (checked) return;
		etcCygwin  = null;
		binCygwin  = null;
		rootCygwin = null;
		if (!isWindows()) return;

		rootCygwin = findRoot();

		// 1. Try to find the paths by appending the patterns to the root dir
		etcCygwin = getValueFromRoot(ETCPATTERN);
		binCygwin = getValueFromRoot(BINPATTERN);
		if(binCygwin == null)
			binCygwin = getValueFromRoot(BINPATTERN_ALTERNATE);

		// 2. Try to find the paths in SOFTWARE\\Cygnus Solutions
		if(etcCygwin == null)
			etcCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + ETCPATTERN, PATH_NAME);
		if(binCygwin == null)
			binCygwin = readValueFromRegistry(REGISTRY_KEY_MOUNTS + BINPATTERN, PATH_NAME);

		checked = true;
	}

	private static String[] exec(String cmd, IConfiguration cfg) {
		try {
			IEnvironmentVariable vars[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(cfg,true);
			String env[] = new String[vars.length];
			for(int i = 0; i < env.length; i++){
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

	public static boolean isMinGW(IConfiguration cfg){
		String versionInfo[] = exec(GCC_VERSION_CMD, cfg);
		if(versionInfo != null) {
			for(int i = 0; i < versionInfo.length; i++){
				if(versionInfo[i].indexOf(MINGW_SPECIAL) != -1)
					return true;
				else if(versionInfo[i].indexOf(CYGWIN_SPECIAL) != -1)
					return false;
			}
		}
		return false;
	}
}
