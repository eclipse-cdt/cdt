/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

/**
 * This class provides OS owned environment variables supplied as {@link Properties} class.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EnvironmentReader {
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
	public static Properties getEnvVars() {
		return (Properties) envVars.clone();
	}

	/**
	 * @param key - name of environment variable (without $ sign).
	 * @return value of environment variable.
	 */
	public static String getEnvVar(String key) {
		return envVars.getProperty(key);
	}
}
