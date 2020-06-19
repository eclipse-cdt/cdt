/*******************************************************************************
 * Copyright (c) 2013, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

/**
 * Environment handling utility methods.
 */
public class Env {

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
		// Get a copy of the native environment variables
		Properties environmentVariables = getEnvVars();

		// If a "local" environment is provided, merge it with the native
		// environment.
		if (envp != null) {
			for (String keyValue : envp) {
				// keyValue is the full provided variable in form "name=value"
				String[] parts = keyValue.split("=", 2); //$NON-NLS-1$
				if (parts.length == 2) {
					String name = parts[0];
					String value = parts[1];

					if ("<unset>".equals(value)) { //$NON-NLS-1$
						environmentVariables.remove(name);
					} else {
						environmentVariables.put(name, value);
					}
				}
			}
		}
		// Set the TERM environment variable if in terminal mode
		if (terminal)
			environmentVariables.put("TERM", "xterm");//$NON-NLS-1$ //$NON-NLS-2$

		// Convert into an array of strings
		List<String> keys = new ArrayList<>(environmentVariables.stringPropertyNames());
		// On Windows hosts, sort the environment keys
		if (Platform.OS_WIN32.equals(Platform.getOS()))
			Collections.sort(keys);
		List<String> strings = new ArrayList<>(keys.size());
		for (String key : keys) {
			String value = environmentVariables.getProperty(key);
			strings.add(key + "=" + value); //$NON-NLS-1$
		}

		return strings.toArray(new String[strings.size()]);
	}

	//WARNING
	//Below is a copy of org.eclipse.cdt.utils.spawner.EnvironmentReader to make the terminal independent from CDT
	//This is supposed to be a straight copy (no modifications)
	//except for making getEnvVars private to avoid errors/warnings
	private static Properties envVars;
	@SuppressWarnings("nls")
	private static List<String> toUppercaseEnvironmentVars = Arrays.asList("PATH", "CYGWIN_HOME", "LANG");

	static {
		boolean isWindows = Platform.OS_WIN32.equals(Platform.getOS());
		envVars = new Properties();
		Map<String, String> envMap = System.getenv();
		for (Map.Entry<String, String> curEnvVar : envMap.entrySet()) {
			String key = curEnvVar.getKey();
			String value = curEnvVar.getValue();
			if (isWindows && toUppercaseEnvironmentVars.contains(key.toUpperCase())) {
				key = key.toUpperCase();
			}
			envVars.setProperty(key, value);
		}
	}

	/**
	 * @return a clone of the list of environment variables.
	 */
	private static Properties getEnvVars() {
		return (Properties) envVars.clone();
	}
}
