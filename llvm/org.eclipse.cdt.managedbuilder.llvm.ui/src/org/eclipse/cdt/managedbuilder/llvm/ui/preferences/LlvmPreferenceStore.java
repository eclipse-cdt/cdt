/*******************************************************************************
 * Copyright (c) 2010, 2016 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui.preferences;

import org.eclipse.cdt.managedbuilder.llvm.ui.LlvmEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.llvm.ui.LlvmUIPlugin;
import org.eclipse.cdt.managedbuilder.llvm.util.FindStdLibPath;
import org.eclipse.cdt.managedbuilder.llvm.util.LlvmToolOptionPathUtil;
import org.eclipse.cdt.managedbuilder.llvm.util.Separators;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Class used to access the LLVM Preference store values.
 *
 * This class is not intended to be subclassed by clients.
 */
public class LlvmPreferenceStore {

	/**
	 * Get the LLVM Preference store.
	 *
	 * @return LLVM Preference store.
	 */
	public static IEclipsePreferences getPreferenceStore() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LlvmUIPlugin.PLUGIN_ID);
		return prefs;
	}

	/**
	 * Get a value from the LLVM Preference store.
	 *
	 * @param name the name of the preference
	 * @return the string-valued preference
	 */
	public static String getPreferenceStoreValue(String name) {
		return getPreferenceStore().get(name, ""); //$NON-NLS-1$
	}

	/**
	 * Get the LLVM bin path value from the LLVM Preference store.
	 *
	 * @return the LLVM bin path value.
	 */
	public static String getBinPath() {
		return getPreferenceStoreValue(PreferenceConstants.P_LLVM_PATH);
	}

	/**
	 * Get the LLVM include path value from the LLVM Preference store.
	 *
	 * @return the LLVM include path value.
	 */
	public static String getIncludePath() {
		return getPreferenceStoreValue(PreferenceConstants.P_LLVM_INCLUDE_PATH);
	}

	/**
	 * Get the LLVM library path value from the LLVM Preference Store.
	 *
	 * @return the LLVM library path value.
	 */
	public static String getLibraryPath() {
		return getPreferenceStoreValue(PreferenceConstants.P_LLVM_LIBRARY_PATH);
	}

	/**
	 * Get the LLVM library from the LLVM Preference store.
	 *
	 * @return the LLVM library value.
	 */
	public static String getLibraries() {
		return getPreferenceStoreValue(PreferenceConstants.P_LLVM_LIBRARIES);
	}

	/**
	 * Get values from the LLVM Preference store as a String array.
	 * Used to get Preference store values which consist of multiple paths
	 * separated by a path separator.
	 *
	 * @param name the name of the preference
	 * @return A String array containing all Preference store values
	 */
	public static String[] getPreferenceStoreValueAsArray(String name) {
		return LlvmToolOptionPathUtil.stringToArray(name);
	}

	/**
	 * Set LLVM Preference store value.
	 *
	 * @param name the name of the preference
	 * @param value the string-valued preference
	 */
	public static void setPreferenceStoreValue(String name, String value) {
		getPreferenceStore().put(name, value);
	}

	/**
	 * Set LLVM bin path to the LLVM Preference store.
	 *
	 * @param path the path to the LLVM bin path.
	 */
	public static void setBinPath(String path) {
		setPreferenceStoreValue(PreferenceConstants.P_LLVM_PATH, path);
	}

	/**
	 * Set LLVM include path to the LLVM Preference store.
	 *
	 * @param path LLVM include path.
	 */
	public static void setIncludePath(String path) {
		setPreferenceStoreValue(PreferenceConstants.P_LLVM_INCLUDE_PATH, path);
	}

	/**
	 * Set LLVM library path to the LLVM Preference store.
	 *
	 * @param path LLVM library path.
	 */
	public static void setLibraryPath(String path) {
		setPreferenceStoreValue(PreferenceConstants.P_LLVM_LIBRARY_PATH, path);
	}

	/**
	 * Set LLVM library to the LLVM Preference store.
	 *
	 * @param lib LLVM library.
	 */
	public static void setLibrary(String lib) {
		setPreferenceStoreValue(PreferenceConstants.P_LLVM_LIBRARIES, lib);
	}

	/**
	 * Get existing paths from the Preference store.
	 *
	 * @param name the name of the preference
	 * @return paths
	 */
	private static String getExistingPaths(String name) {
		String paths = ""; //$NON-NLS-1$
		if (name.equals(PreferenceConstants.P_LLVM_INCLUDE_PATH)) {
			paths = getIncludePath();
		} else if (name.equals(PreferenceConstants.P_LLVM_LIBRARY_PATH)) {
			paths = getLibraryPath();
		} else if (name.equals(PreferenceConstants.P_LLVM_LIBRARIES)) {
			paths = getLibraries();
		}
		return paths;
	}

	/**
	 * Append a new value to the Preference store if it doesn't already exists.
	 *
	 * @param name the name of the preference
	 * @param value the string-valued preference
	 */
	public static void appendValue(String name, String value) {
		StringBuilder sB = new StringBuilder();
		String paths = null;
		//get existing paths
		paths = getExistingPaths(name);
		//if values exist
		if (paths.length() != 0) {
			//if the value is reasonable
			if (!value.equalsIgnoreCase("") && value.length() != 0) { //$NON-NLS-1$
				//if the paths doesn't contain the new value
				if (!paths.contains(value)) {
					//append existing paths to the string buffer
					sB.append(paths);
					//add a path separator in the end if it doesn't exists
					if (paths.charAt(paths.length() - 1) != Separators.getPathSeparator().charAt(0)) {
						sB.append(Separators.getPathSeparator());
					}
					//append the new value to end of the list
					sB.append(value);
				}
			}
		} else { //no existing values
			//if the value is reasonable
			if (!value.equalsIgnoreCase("") && value.length() != 0) { //$NON-NLS-1$
				//append a new path to the string buffer
				sB.append(value);
			}
		}
		String newValues = sB.toString();
		if (newValues.length() != 0) {
			//set the new preference store value
			setPreferenceStoreValue(name, newValues);
		}
	}

	/**
	 * Append an include path to the LLVM Preference store.
	 *
	 * @param path the LLVM include path.
	 */
	public static void appendIncludePath(String path) {
		appendValue(PreferenceConstants.P_LLVM_INCLUDE_PATH, path);
	}

	/**
	 * Append a library path to the LLVM Preference store.
	 *
	 * @param path the LLVM library path.
	 */
	public static void appendLibraryPath(String path) {
		appendValue(PreferenceConstants.P_LLVM_LIBRARY_PATH, path);
	}

	/**
	 * Append a library to the LLVM Preference store.
	 *
	 * @param lib the LLVM library.
	 */
	public static void appendLibrary(String lib) {
		appendValue(PreferenceConstants.P_LLVM_LIBRARIES, lib);
	}

	/**
	 * Remove a value from the LLVM preference store.
	 *
	 * @param name Name of the preference
	 * @param value Value to remove from the preference store
	 */
	public static void removeValue(String name, String value) {
		StringBuilder sB = new StringBuilder();
		String existingValues = null;
		String newValue = null;
		//get existing values
		existingValues = getExistingPaths(name);
		//if the String contains the value
		if (existingValues.contains(value)) {
			//if many values i.e. contains path separator
			if (existingValues.contains(Separators.getPathSeparator())) {
				//separate String of values to an array
				String[] exValArray = existingValues.split(Separators.getPathSeparator());
				//if more than one value
				if (exValArray.length > 1) {
					//remove the value from the array
					exValArray = LlvmToolOptionPathUtil.removePathFromExistingPathList(exValArray, value);
					//if the array isn't empty
					if (exValArray.length > 0) {
						//append all values to the StringBuilder excluding the removed one
						for (String val : exValArray) {
							//append a value
							sB.append(val);
							//append a path separator
							sB.append(Separators.getPathSeparator());
						}
						//form a String
						newValue = sB.toString();
					}
				} else { //only one value with a path separator at the end
					newValue = ""; //$NON-NLS-1$
				}

			} else { //only value without a path separator at the end
				newValue = ""; //$NON-NLS-1$
			}
			//set the new preference store value
			setPreferenceStoreValue(name, newValue);
		}
	}

	/**
	 * Remove a include path from the LLVM preference store.
	 *
	 * @param path The include path to be removed from the LLVM preference store.
	 */
	public static void removeIncludePath(String path) {
		removeValue(PreferenceConstants.P_LLVM_INCLUDE_PATH, path);
	}

	/**
	 * Remove a library path from the LLVM preference store.
	 *
	 * @param path The library path to be removed from the LLVM preference store.
	 */
	public static void removeLibraryPath(String path) {
		removeValue(PreferenceConstants.P_LLVM_LIBRARY_PATH, path);
	}

	/**
	 * Remove a library from the LLVM preference store.
	 *
	 * @param lib The library to be removed from the LLVM preference store.
	 */
	public static void removeLibrary(String lib) {
		removeValue(PreferenceConstants.P_LLVM_LIBRARIES, lib);
	}

	public static void addMinGWStdLib() {
		String path = LlvmEnvironmentVariableSupplier.getMinGWStdLib();
		if (path != null) {
			//add to preference store
			appendLibraryPath(path);
			//			ProjectIndex.rebuiltIndex(proj);
			appendLibrary("stdc++"); //$NON-NLS-1$
		}
	}

	public static void addStdLibUnix() {
		String path = FindStdLibPath.find();
		String lib = "stdc++"; //$NON-NLS-1$
		if (path != null) {
			//add to preference store
			appendLibraryPath(path);
			//			ProjectIndex.rebuiltIndex(proj);
			appendLibrary(lib);
		}
	}

}
