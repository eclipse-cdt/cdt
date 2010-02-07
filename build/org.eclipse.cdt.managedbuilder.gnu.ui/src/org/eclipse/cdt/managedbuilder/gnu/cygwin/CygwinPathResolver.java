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
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.cdt.utils.spawner.ProcessFactory;


/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CygwinPathResolver implements IBuildPathResolver {
	private static final String TOOL = "/cygpath -w -p "; //$NON-NLS-1$ 
	private static final char BS = '\\';
	private static final char SLASH = '/';
	private static final String PROPERTY_OS_NAME = "os.name"; //$NON-NLS-1$
	private static final String PROPERTY_OS_VALUE = "windows";//$NON-NLS-1$
	private static final String SP = " ";         //$NON-NLS-1$
	private static final String REGISTRY_KEY = "SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\"; //$NON-NLS-1$ 
	private static final String PATH_NAME = "native";   //$NON-NLS-1$
	private static final String EMPTY = "";				//$NON-NLS-1$
	private static final String SSLASH = "/";           //$NON-NLS-1$
	private static final String BSLASH = "\\\\";        //$NON-NLS-1$
	private static final String BINPATTERN = "/usr/bin"; //$NON-NLS-1$
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
		if (!checked) checkRegistry();
		return etcCygwin;
	}

	/*
	 * returns "/usr/bin" path in Windows format 
	 */
	public static String getBinPath() {
		if (!checked) checkRegistry();
		return binCygwin;
	}
	/*
	 * returns Cygwin root ("/") path in Windows format 
	 */
	
	public static String getRootPath() {
		if (!checked) checkRegistry();
		return rootCygwin;
	}
	
	public static boolean isWindows() {
		return (System.getProperty(PROPERTY_OS_NAME).toLowerCase().startsWith(PROPERTY_OS_VALUE));
	}
		
	/**
	 * Reads required value from registry.
	 * 
	 * If there's no such key, tries to read "parent" key
	 * and forms resulting path from parent path and 
	 * key suffix.
	 * 
	 * For example, if there's no key for "/etc",
	 * reads key for "/" (has value "c:/cygwin")
	 * and forms result "c:/cygwin/etc".
	 * 
	 * @param user 
	 * 			if true,  reads from HKEY_CURRENT_USER
	 *          if false, reads from HKEY_LOCAL_MACHINE  
	 * @param pattern
	 *          specific registry key value (last segment) 
	 * @return
	 *          corresponding string value 
	 *          or null if nothing found
	 */
	private static String read(boolean user, String pattern) {
		String tail = EMPTY; 
		while (pattern.length() > 0) {
			String key = REGISTRY_KEY + pattern;
			WindowsRegistry registry = WindowsRegistry.getRegistry(); 
			if (null != registry) {
				String s = user ?
					registry.getCurrentUserValue(key, PATH_NAME) :
					registry.getLocalMachineValue(key, PATH_NAME);

				if (s != null) 
					return (s.concat(tail).replaceAll(BSLASH, SSLASH));
			}
			if (pattern.equals(ROOTPATTERN)) 
				break; // no other paths to search 
			int pos = pattern.lastIndexOf(SLASH);
			if (pos < 0) break; 
			tail = pattern.substring(pos, pattern.length()) + tail;
			if (pos == 0) 
				pattern = ROOTPATTERN; // leave leading slash
			else 
				pattern = pattern.substring(0, pos); 
		}
		return null;	
	}
	
	/**
	 * reads once data from registry (for Win32 only)
	 * and sets corresponding properties; 
	 */
	private static synchronized void checkRegistry() {
		if (checked) return;
		etcCygwin  = null; 
		binCygwin  = null;
		rootCygwin = null;  
		if (!isWindows()) return;
		for (int i=0; i<2; i++) {
			if (etcCygwin == null)  
				etcCygwin = read((i==0), ETCPATTERN);
			if (binCygwin == null) 
				binCygwin = read((i==0), BINPATTERN);
			if (rootCygwin == null)
				rootCygwin = read((i==0), ROOTPATTERN);
		}
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
