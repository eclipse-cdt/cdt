/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

/**
 * This class provides environment variables supplied as {@link Properties} class.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EnvironmentReader {
	private static Properties envVars = null;
	private static Properties envVarsNormalized = null;
	private static ArrayList<String> rawVars = null;

	private static synchronized void init() {
		if (envVars==null) {
			envVars = new Properties();
			// on Windows environment variable names are case-insensitive
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				envVarsNormalized = new Properties();
			} else {
				envVarsNormalized = envVars;
			}
			rawVars = new ArrayList<String>();
			Map<String, String> envMap = System.getenv();
			for (String var : envMap.keySet()) {
				String value = envMap.get(var);
				envVars.setProperty(var, value);
				if (envVarsNormalized!=envVars) {
					envVarsNormalized.setProperty(var.toUpperCase(), value);
				}
				rawVars.add(var + "=" + value); //$NON-NLS-1$
			}
			rawVars.trimToSize();
		}
	}

	/**
	 * @return list of environment variables.
	 */
	public static Properties getEnvVars() {
		init();
		return (Properties) envVars.clone();
	}

	/**
	 * @param key - name of environment variable (without $ sign).
	 * @return value of environment variable.
	 */
	public static String getEnvVar(String key) {
		init();
		return envVarsNormalized.getProperty(key);
	}

	/**
	 * @deprecated since CDT 6.1. {@link #getEnvVars()} provides all the data.
	 */
	@Deprecated
	public static String[] getRawEnvVars() {
		init();
		return rawVars.toArray(new String[rawVars.size()]);
	}
}
