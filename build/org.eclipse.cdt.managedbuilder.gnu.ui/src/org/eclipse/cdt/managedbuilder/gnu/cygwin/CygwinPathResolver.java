/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.gnu.ui.GnuUIPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;


public class CygwinPathResolver implements IBuildPathResolver {
	static final String TOOL = "/cygpath -w -p "; //$NON-NLS-1$ 
	static final char BS = '\\';                      //$NON-NLS-1$
	static final char SLASH = '/';                    //$NON-NLS-1$
	static final String PROPERTY_OS_NAME = "os.name"; //$NON-NLS-1$
	static final String PROPERTY_OS_VALUE = "windows";//$NON-NLS-1$
	static final String ARG0 = "regedit"; //$NON-NLS-1$
	static final String ARG1 = "/ea";     //$NON-NLS-1$
	static final String OUTFILE = "result.txt"; //$NON-NLS-1$
	static final String SP = " ";         //$NON-NLS-1$
	static final String QUOT = "\"";      //$NON-NLS-1$
	static final String REGISTRY_KEY = "\\SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2"; //$NON-NLS-1$ 
	static final String[] REGISTRY_ROOTS = {"\"HKEY_CURRENT_USER", "\"HKEY_LOCAL_MACHINE"}; //$NON-NLS-1$ //$NON-NLS-2$
	static final String REGISTRY_BINS = "/usr/bin";              //$NON-NLS-1$
	static final String CYGPREF_NAME = "cygdrive prefix";        //$NON-NLS-1$
	static final String PATH_NAME = "native";   //$NON-NLS-1$
	static final String REG_SZ = "REG_SZ";      //$NON-NLS-1$
	static final String BINPATTERN = "/usr/bin"; //$NON-NLS-1$
	static final String ETCPATTERN = "/etc";     //$NON-NLS-1$
	static final String ROOTPATTERN = "/";       //$NON-NLS-1$
	static final String DELIMITER_UNIX = ":";    //$NON-NLS-1$
	static final String DELIMITER_WIN  = ";";    //$NON-NLS-1$
	static final String GCC_VERSION_CMD  = "gcc --version";    //$NON-NLS-1$
	static final String MINGW_SPECIAL = "mingw ";    //$NON-NLS-1$
	static final String CYGWIN_SPECIAL = "cygwin ";    //$NON-NLS-1$
	
	static boolean checked = false;
	static String binCygwin  = null;
	static String rootCygwin = null;
	static String etcCygwin = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildPathResolver#resolveBuildPaths(int, java.lang.String, java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration)
	 */
	public String[] resolveBuildPaths(int pathType, String variableName,
			String variableValue, IConfiguration configuration) {
		
		if(isMinGW(configuration)){
			return variableValue.split(DELIMITER_WIN);
		}

		String[] result = variableValue.split(DELIMITER_UNIX);
		if (!isWindows()) return result; 
		String exePath = getBinPath();
		if (exePath == null) { return result; } // no changes
		File file = new File(exePath); 
		if (!file.exists() || !file.isDirectory()) { return result; } // no changes
		
		ArrayList ls = new ArrayList();
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
	/**
	 * @return
	 */
	public static boolean isWindows() {
		return (System.getProperty(PROPERTY_OS_NAME).toLowerCase().startsWith(PROPERTY_OS_VALUE));
	}
		
	/**
	 * reads once data from registry (for Win32 only)
	 * and sets corresponding properties; 
	 */
	
	private static synchronized void checkRegistry() {
		checked = true;
		etcCygwin  = null; 
		binCygwin  = null;
		rootCygwin = null;  
		if (!isWindows()) return;
		for(int i = 0; i < REGISTRY_ROOTS.length; i++){
			IPath toSave = GnuUIPlugin.getDefault().getStateLocation();
			toSave = toSave.addTrailingSeparator().append(OUTFILE);
			String[] args = {ARG0, ARG1, toSave.toOSString(), REGISTRY_ROOTS[i]+REGISTRY_KEY+QUOT };
			try {
				File f = new File(toSave.toOSString());
				f.delete();
				if (ProcessFactory.getFactory().exec(args).waitFor() == 0 && f.exists() && f.canRead()) {
					BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					ArrayList ls = new ArrayList(1);
					String s;
					while ((s = r.readLine() ) != null ) ls.add(s);
					r.close();
					f.delete();
					String[] aus = (String[])ls.toArray(new String[0]); 	
					if (etcCygwin  == null) { etcCygwin  = getDir(aus, ETCPATTERN); }
					if (binCygwin  == null) { binCygwin  = getDir(aus, BINPATTERN); }
					if (rootCygwin == null) { rootCygwin = getDir(aus, ROOTPATTERN);}
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {					
			} catch (InterruptedException e) {
			} catch (SecurityException e) {
			}
		}
	}
	
	/**
	 * @param ls      - "regedit"'s output
	 * @param pattern - path to search
	 * @return
	 */
	private static String getDir(String[] ls, String pattern) {
		String tail = "";  //$NON-NLS-1$ 
		while (pattern.length() > 0) {
			boolean search = false;
			for (int i=0; i<ls.length; i++) {
				int pos=0;
				if (ls[i].lastIndexOf(REGISTRY_KEY) > 0) {
					search = ls[i].endsWith(pattern+"]"); //$NON-NLS-1$ 
				}
				else if (search && ((pos = ls[i].lastIndexOf(PATH_NAME)) > 0)) {
					String s = ls[i].substring(pos + PATH_NAME.length() + 3).trim();
					s = s.substring(0, s.length() - 1) + tail;
					return s.replaceAll("\\\\+", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (pattern.equals(ROOTPATTERN)) break; // no other paths to search 
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
	
	private static String[] exec(String cmd, IConfiguration cfg) {
		try {
//			Process proc = Runtime.getRuntime().exec(cmd);
			IBuildEnvironmentVariable vars[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(cfg,true,true);
			String env[] = new String[vars.length];
			for(int i = 0; i < env.length; i++){
				env[i] = vars[i].getName() + "=";
				String value = vars[i].getValue();
				if(value != null)
					env[i] += value; 
			}
			Process proc = ProcessFactory.getFactory().exec(cmd.split(SP), env);
			if (proc != null) {
				
				InputStream ein = proc.getInputStream();
				BufferedReader d1 = new BufferedReader(new InputStreamReader(ein));
				ArrayList ls = new ArrayList(10);
				String s;
				while ((s = d1.readLine() ) != null ) {
					ls.add(s); 
				}
				ein.close();
				return (String[])ls.toArray(new String[0]);
			}
		} catch (IOException e) {
			//TODO: log
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
