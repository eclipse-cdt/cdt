/*******************************************************************************
 * Copyright (c) 2010, 2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *      Petri Tuononen - Initial implementation
 *      Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui;

import java.io.File;
import java.util.HashMap;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.gnu.cygwin.GnuCygwinConfigurationEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.gnu.mingw.MingwEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.llvm.ui.preferences.LlvmPreferenceStore;
import org.eclipse.cdt.managedbuilder.llvm.util.Separators;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Contains LLVM environment variables.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LlvmEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	// toggle for preference changes
	private static boolean preferencesChanged = true;
	// LLVM environment variable data structure
	private static HashMap<String, LlvmBuildEnvironmentVariable> llvmEnvironmentVariables = 
		new HashMap<String, LlvmBuildEnvironmentVariable>(6);
	// Environment variables for HashMap usage
	private static final String ENV_VAR_NAME_LLVM_BIN 		= "LLVM_BIN_PATH"; //$NON-NLS-1$
	private static final String ENV_VAR_NAME_LLVMINTERP 	= "LLVMINTERP"; //$NON-NLS-1$
	private static final String ENV_VAR_NAME_PATH 			= "PATH"; //$NON-NLS-1$
	private static final String ENV_VAR_NAME_INCLUDE_PATH 	= "INCLUDE_PATH"; //$NON-NLS-1$
	private static final String ENV_VAR_NAME_LIBRARY_PATH 	= "LLVM_LIB_SEARCH_PATH"; //$NON-NLS-1$
	private static final String ENV_VAR_NAME_LIBRARIES 		= "LIBRARIES"; //$NON-NLS-1$
	
	/**
	 * Initializes llvm environment variable paths from the system environment variables.
	 */
	public static void initializePaths() { //TODO: Is this actually called anywhere?
		// get bin path
		String binPath = getBinPath();
		// set LLVM bin path environment variable
		setLlvmEnvironmentVariableReplace(ENV_VAR_NAME_LLVM_BIN, binPath);
		// if bin path exists
		if (binPath != null && binPath.length()!=0) {
			String pathStr = binPath;
			// if OS is Windows (Windows specific settings)
			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
				try {
					// try to find mingw or cygwin path from PATH environment variable
					IBuildEnvironmentVariable envPath =
							llvmEnvironmentVariables.get(ENV_VAR_NAME_PATH);
					IBuildEnvironmentVariable mingwPath=null, cygwinPath=null;
					// if path is empty
					if (envPath == null) {
						// try to find mingw path from MingwEnvironmentVariableSupplier
						IConfigurationEnvironmentVariableSupplier mingwEnvironmentVariables = 
							new MingwEnvironmentVariableSupplier();
						mingwPath = mingwEnvironmentVariables.getVariable(
								ENV_VAR_NAME_PATH, null, null);
						// try to find cygwin path from GnuCygwinConfigurationEnvironmentSupplier
						IConfigurationEnvironmentVariableSupplier cygwinEnvironmentVariables =
							new GnuCygwinConfigurationEnvironmentSupplier();
						cygwinPath = cygwinEnvironmentVariables.getVariable(
								ENV_VAR_NAME_PATH, null, null);

					}
					// if mingw found
					if (mingwPath != null) {
						//form full path
						pathStr = pathStr + System.getProperty("path.separator") + mingwPath.getValue(); //$NON-NLS-1$
					}
					// if cygwin found
					if (cygwinPath != null) {
						//form full path
						pathStr = pathStr + System.getProperty("path.separator") + cygwinPath.getValue(); //$NON-NLS-1$
					}
				} catch (Exception e) {
					//TODO: Emit proper error message and enter it to Eclipse error log.
					e.printStackTrace();
				}
			}
			//initialize environment variable cache values
			setLlvmEnvironmentVariable(ENV_VAR_NAME_PATH, pathStr);
			setLlvmEnvironmentVariable(ENV_VAR_NAME_LLVMINTERP, binPath + Separators.getFileSeparator() + "lli"); //$NON-NLS-1$
			setLlvmEnvironmentVariable(ENV_VAR_NAME_INCLUDE_PATH, getSysEnvPath(ENV_VAR_NAME_INCLUDE_PATH));
			setLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARY_PATH, getSysEnvPath(ENV_VAR_NAME_LIBRARY_PATH));
			setLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARIES, getSysEnvPath(ENV_VAR_NAME_LIBRARIES));
			preferencesChanged = false;
		}
	}
	
	/**
	 * Returns LLVM bin path
	 * 
	 * @return LLVM bin path
	 */
	public static String getBinPath() {
		return findBinDir(ENV_VAR_NAME_LLVM_BIN, "bin"); //$NON-NLS-1$
	}

	/**
	 * Returns LLVM include paths
	 * 
	 * @return LLVM include paths
	 */
	public static String getIncludePath() {
		return getLlvmEnvironmentVariable(ENV_VAR_NAME_INCLUDE_PATH).getValue();
	}

	/**
	 * Returns LLVM library paths
	 * 
	 * @return LLVM library paths
	 */
	public static String getLibraryPath() {
		return getLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARY_PATH).getValue();
	}

	/**
	 * Returns LLVM libraries
	 * 
	 * @return LLVM libraries
	 */
	public static String getLibraries() {
		return getLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARIES).getValue();
	}
	
	/**
	 * Sets path to LLVM bin.
	 * 
	 * @param path Path to LLVM bin location.
	 */
	public static void setBinPath(String path) {
		setLlvmEnvironmentVariableReplace(ENV_VAR_NAME_LLVM_BIN, path);
	}
	
	/**
	 * Appends a new include path.
	 * 
	 * @param path Include path
	 */
	public static void addIncludePath(String path) {
		String existingIncPaths = getIncludePath();
		//add the include path only if it doesn't already exists
		if (!existingIncPaths.contains(path)) {
			appendLlvmEnvironmentVariable(ENV_VAR_NAME_INCLUDE_PATH, existingIncPaths, path);			
		}
	}
	
	/**
	 * Appends a new library path.
	 * 
	 * @param path Library path
	 */
	public static void addLibraryPath(String path) {
		String existingLibPaths = getLibraryPath();
		//add the library path only if it doesn't already exists
		if (!existingLibPaths.contains(path)) {
			appendLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARY_PATH, existingLibPaths, path);			
		}
	}
	
	/**
	 * Appends a new library.
	 * 
	 * @param lib Library file
	 */
	public static void addLibrary(String lib) {
		String existingLibs = getLibraries();
		//add the library only if it doesn't already exists
		if (!existingLibs.contains(lib)) {
			appendLlvmEnvironmentVariable(ENV_VAR_NAME_LIBRARIES, existingLibs, lib);			
		}
	}
	
	/**
	 * This is to be called if some of the preference paths have changed.
	 */
	public static void notifyPreferenceChange() { //TODO: Change
		preferencesChanged = true;
	}

	/**
	 * Returns a specific path for given parameters.
	 * 
	 * @param pathKey Path for specific location
	 * @param subDirName Additional sub-path
	 * @return bin path
	 */
	private static String findBinDir(String pathKey, String subDirName) {
		String resultPath = null;
		// If preferences haven't been changed, try to find the bin path from the LLVM environment
		// variable map. 
		if (!preferencesChanged) { //TODO: Change
			//get current path
			LlvmBuildEnvironmentVariable earlierValue = llvmEnvironmentVariables.get(pathKey);
			//if earlier LlvmBuildEnvironmentVariable exists
			if (null != earlierValue) {
				//return current path
				return earlierValue.getValue();
			}
		} else {
			// Try if the path is set in the LLVM plug-in preferences
			String preferenceLocation = LlvmPreferenceStore.getBinPath();
			// if preference exists
			if (null != preferenceLocation) {
				// remove white spaces from preference location
				preferenceLocation = preferenceLocation.trim();
				// if preference location is not empty
				if (preferenceLocation.length()!=0) {
					// get path for LLVM executable
					resultPath = getDirIfLlvmFound(preferenceLocation, null);
					// if LLVM executable path doesn't exist
					if (null == resultPath) {
						// If no luck check next with sub directory name appended
						resultPath = getDirIfLlvmFound(preferenceLocation,
								subDirName);
					}
				}
			}
			if (null == resultPath) {
				// If still no luck try all folders listed in PATH
				String pathVariable = System.getenv(ENV_VAR_NAME_PATH);
				// split paths to String array
				String[] paths = pathVariable.split(Separators.getPathSeparator());
				// check every path if LLVM executable is found
				for (String pathStr : paths) {
					resultPath = getDirIfLlvmFound(pathStr, null);
					// stop loop if LLVM executable path is found
					if (null != resultPath) {
						break;
					}
				}
			}
			// return found path
			return resultPath;			
		}
		return null;
	}

	/**
	 * Returns LLVM executable path.
	 * 
	 * @param candidatePath Suggestion for LLVM executable path
	 * @param subPath Additional sub-path for LLVM executable path
	 * @return Full path for LLVM executable if valid, otherwise null
	 */
	private static String getDirIfLlvmFound(String candidatePath, String subPath) {
		String llvmPath = null;
		// If there is a trailing / or \, remove it
		if (candidatePath.endsWith(Separators.getFileSeparator()) && candidatePath.length() > 1) {
			llvmPath = candidatePath.substring(0, candidatePath.length() - 1);
		}
		// If subPath exists and is not empty -> append it to candidatePath.
		if (null != subPath && !subPath.isEmpty()) {
			// Form full path.
			llvmPath = candidatePath + Separators.getFileSeparator() + subPath;
		}
		if (llvmPath == null)
			return null;
		// Return a full path for LLVM executable if it's valid, otherwise null.
		return getBinDirIfLlvm_ar(llvmPath);
	}

	/**
	 * Returns the full path for llvm executable if the bin path given
	 * as a parameter is found and executable exists in that path.
	 * 
	 * @param binPathTemp User provided bin directory path
	 * @return bin path where llvm-ar is located if executable exists
	 */
	private static String getBinDirIfLlvm_ar(String binPathTemp) {
		//if given directory is found
		if (new Path(binPathTemp).toFile().isDirectory()) {
			String llvm_executable = "llvm-ar"; //$NON-NLS-1$
			File arFileFullPath = null;
			// If OS is Windows -> add .exe to the executable name.
			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {  //$NON-NLS-1$//$NON-NLS-2$
				llvm_executable = llvm_executable + ".exe"; //$NON-NLS-1$
			}
			// Form full executable path
			arFileFullPath = new File(binPathTemp, llvm_executable);
			// Check if file exists -> proper LLVM installation exists.
			if (arFileFullPath.isFile()) {
				// Return path where llvm-ar exists.
				return binPathTemp;
			}
		}			
		return null;
	}
	
	/**
	 * Returns stdc++ library path located in MinGW installation.
	 * 
	 * @return stdc++ library path for MinGW
	 */
	public static String getMinGWStdLib() {
		// get mingw bin path
		IPath mingwBinPath = MingwEnvironmentVariableSupplier.getBinDir();
		if (mingwBinPath != null) {
			StringBuilder sB = new StringBuilder(mingwBinPath.toOSString());
			// drop bin
			if (sB.length() >= 3) {
				sB.delete(sB.length() - 3, sB.length());
				// append mingw lib subdir
				sB.append("lib\\gcc\\mingw32\\"); //$NON-NLS-1$
				// get all files in the directory
				File f = new File(sB.toString());
				if (f.isDirectory()) {
					String[] list = f.list();
					if (list.length > 0) {
						// append the first dir
						sB.append(list[0]);
						return sB.toString();
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * Returns LLVM environment variable.
	 * 
	 * @param envName Name of the environment variable
	 */
	public static LlvmBuildEnvironmentVariable getLlvmEnvironmentVariable(String envName) {
		return llvmEnvironmentVariables.get(envName);
	}
	
	/**
	 * Sets LLVM environment variable.
	 * 
	 * @param name Name for the environment variable
	 * @param path Path for the environment variable
	 */
	private static void setLlvmEnvironmentVariable(String name, String path) {
		// append a new path in front of the the old path in HashMap that contains
		// the specific LLVM environment variable
		llvmEnvironmentVariables.put(name, new LlvmBuildEnvironmentVariable(
				name, path, IBuildEnvironmentVariable.ENVVAR_APPEND));
	}
	
	/**
	 * Sets LLVM environment variable by replacing the existing paths.
	 * 
	 * @param name Name for the environment variable
	 * @param path Path for the environment variable
	 */
	public static void setLlvmEnvironmentVariableReplace(String name, String path) {
		// replace the old path in HashMap that contains the specific LLVM environment variable
		llvmEnvironmentVariables.put(name, new LlvmBuildEnvironmentVariable(
				name, path, IBuildEnvironmentVariable.ENVVAR_REPLACE));
	}
	
	/**
	 * Appends a new LLVM environment variable to existing list.
	 * 
	 * @param name Name of the preference
	 * @param oldPath Old paths/preference values
	 * @param path New path to be added to the environment variable
	 */
	public static void appendLlvmEnvironmentVariable(String name, String oldPath, String path) {
		String newPath = null;
		boolean ok = false;
		// if oldPath exists
		if (oldPath!=null) {
			//if the oldPath isn't empty
			if (!oldPath.trim().isEmpty()) {
				StringBuffer sB = new StringBuffer();
				// append old path
				sB.append(oldPath);
				// append a path separator
				sB.append(Separators.getPathSeparator());
				// append the new path
				sB.append(path);
				// construct a new full path
				newPath = sB.toString();
				ok = true;
			}
		}
		if (!ok) {
			newPath=path;			
		}
		// Set new path to the HashMap that contains the specific LLVM environment variable
		// if newPath exists.
		if (newPath != null) {
			// if the newPath isn't empty
			if (!newPath.trim().isEmpty()) {
				// add new values to the LLVM environment variable
				llvmEnvironmentVariables.put(name, new LlvmBuildEnvironmentVariable(name, newPath,
						IBuildEnvironmentVariable.ENVVAR_APPEND));				
			}
		}
	}
	
	/**
	 * Returns a system environment variable path
	 * 
	 * @param envName Environment variable name
	 * @return system environment variable path
	 */
	private static String getSysEnvPath(String envName) {
		String path = System.getenv(envName);
		if (path != null) {
			return path;
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return llvmEnvironmentVariables.get(variableName);
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return llvmEnvironmentVariables.values().toArray(new IBuildEnvironmentVariable[0]);
	}
}
