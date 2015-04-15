/*******************************************************************************
 * Copyright (c) 2013, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Environment handling utility methods.
 */
public class Env {

	// Reference to the monitor to lock if determining the native environment
	private final static Object ENV_GET_MONITOR = new Object();

	// Reference to the native environment with the case of the variable names preserved
	private static Map<String, String> nativeEnvironmentCasePreserved = null;

	/**
	 * Returns the merged environment of the native environment and the passed
	 * in environment. Passed in variables will overwrite the native environment
	 * if the same variables are set there.
	 * <p>
	 * For use with terminals, the parameter <code>terminal</code> should be set to
	 * <code>true</code>. In this case, the method will assure that the <code>TERM</code>
	 * environment variable is always set to <code>ANSI</code> and is not overwritten
	 * by the passed in environment.
	 *
	 * @param envp The environment to set on top of the native environment or <code>null</code>.
	 * @param terminal <code>True</code> if used with an terminal, <code>false</code> otherwise.
	 *
	 * @return The merged environment.
	 */
	public static String[] getEnvironment(String[] envp, boolean terminal) {
		// Get the cached native environment
		Map<String, String> nativeEnv = getNativeEnvironmentCasePreserved();
		// Make a copy of the native environment so it can be manipulated without changing
		// the cached environment
		Map<String, String> env = new LinkedHashMap<String, String>(nativeEnv);
		// Set the TERM environment variable if in terminal mode
		if (terminal) env.put("TERM", "xterm"); //$NON-NLS-1$ //$NON-NLS-2$

		// On Windows, the environment variable names are not case-sensitive. However,
		// we desire to preserve the original case. Build up a translation map between
		// an all lowercase name and the original environment name
		Map<String, String> k2n = null;
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			k2n = new HashMap<String, String>();
			for (String name : env.keySet()) {
				k2n.put(name.toLowerCase(), name);
			}
		}

		// If a "local" environment is provided, merge it with the native
		// environment.
		if (envp != null) {
			for (int i = 0; i < envp.length; i++) {
				// The full provided variable in form "name=value"
				String envpPart = envp[i];
				// Split the variable
				String[] parts = envpPart.split("=");//$NON-NLS-1$
				String name = parts[0].trim();
				// Map the variable name to the real environment name (Windows only)
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					if (k2n.containsKey(name.toLowerCase())) {
						String candidate = k2n.get(name.toLowerCase());
						Assert.isNotNull(candidate);
						name = candidate;
					}
					// Filter out environment variables with bad names
					if ("".equals(name.trim()) || name.contains("=") || name.contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						continue;
					}
				}
				// Get the variable value
				String value = parts.length > 1 ? parts[1].trim() : ""; //$NON-NLS-1$
				// Don't overwrite the TERM variable if in terminal mode
				if (terminal && "TERM".equals(name)) continue; //$NON-NLS-1$
				// If a variable with the name does not exist, just append it
				if (!env.containsKey(name) && !"<unset>".equals(value)) { //$NON-NLS-1$
					env.put(name, value);
				} else if (env.containsKey(name)) {
					// If the value contains the special placeholder "<unset>", remove the variable from the environment
					if ("<unset>".equals(value)) {//$NON-NLS-1$
						env.remove(name);
					} else {
						// A variable with the name already exist, check if the value is different
						String oldValue = env.get(name);
						if (oldValue != null && !oldValue.equals(value) || oldValue == null && value != null) {
							env.put(name, value);
						}
					}
				}
			}
		}

		// Convert into an array of strings
		List<String> keys = new ArrayList<String>(env.keySet());
		// On Windows hosts, sort the environment keys
		if (Platform.OS_WIN32.equals(Platform.getOS())) Collections.sort(keys);
		Iterator<String> iter = keys.iterator();
		List<String> strings = new ArrayList<String>(env.size());
		StringBuilder buffer = null;
		while (iter.hasNext()) {
			String key = iter.next();
			buffer = new StringBuilder(key);
			buffer.append('=').append(env.get(key));
			strings.add(buffer.toString());
		}

		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Determine the native environment.
	 *
	 * @return The native environment, or an empty map.
	 */
	private static Map<String, String> getNativeEnvironmentCasePreserved() {
		synchronized (ENV_GET_MONITOR) {
			if (nativeEnvironmentCasePreserved == null) {
				nativeEnvironmentCasePreserved = new LinkedHashMap<String, String>();
				cacheNativeEnvironment(nativeEnvironmentCasePreserved);
			}
			return new LinkedHashMap<String, String>(nativeEnvironmentCasePreserved);
		}
	}

	/**
	 * Query the native environment and store it to the specified cache.
	 *
	 * @param cache The environment cache. Must not be <code>null</code>.
	 */
	private static void cacheNativeEnvironment(Map<String, String> cache) {
		Assert.isNotNull(cache);

		try {
			String nativeCommand = null;
			if (Platform.getOS().equals(Constants.OS_WIN32)) {
				nativeCommand = "cmd.exe /C set"; //$NON-NLS-1$
			} else if (!Platform.getOS().equals(Constants.OS_UNKNOWN)) {
				nativeCommand = "env"; //$NON-NLS-1$
			}
			if (nativeCommand == null) { return; }
			Process process = Runtime.getRuntime().exec(nativeCommand);

			// read process directly on other platforms
			// we need to parse out matching '{' and '}' for function declarations in .bash environments
			// pattern is [function name]=() { and we must find the '}' on its own line with no trailing ';'
			InputStream stream = process.getInputStream();
			InputStreamReader isreader = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(isreader);
			try {
				String line = reader.readLine();
				String key = null;
				String value = null;
				while (line != null) {
					int func = line.indexOf("=()"); //$NON-NLS-1$
					if (func > 0) {
						key = line.substring(0, func);
						// scan until we find the closing '}' with no following chars
						value = line.substring(func + 1);
						while (line != null && !line.equals("}")) { //$NON-NLS-1$
							line = reader.readLine();
							if (line != null) {
								value += line;
							}
						}
						line = reader.readLine();
					} else {
						int separator = line.indexOf('=');
						if (separator > 0) {
							key = line.substring(0, separator);
							value = line.substring(separator + 1);
							StringBuilder bufValue = new StringBuilder(value);
							line = reader.readLine();
							if (line != null) {
								// this line has a '=' read ahead to check next line for '=', might be broken on more
								// than one line
								separator = line.indexOf('=');
								while (separator < 0) {
									bufValue.append(line.trim());
									line = reader.readLine();
									if (line == null) {
										// if next line read is the end of the file quit the loop
										break;
									}
									separator = line.indexOf('=');
								}
							}
							value = bufValue.toString();
						}
					}
					if (key != null) {
						cache.put(key, value);
						key = null;
						value = null;
					} else {
						line = reader.readLine();
					}
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			// Native environment-fetching code failed.
			// This can easily happen and is not useful to log.
		}
	}

}
