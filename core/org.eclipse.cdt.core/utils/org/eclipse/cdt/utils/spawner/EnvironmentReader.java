/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EnvironmentReader {
	private static Properties envVars = null;
	private static ArrayList<String> rawVars = null;

	public static synchronized Properties getEnvVars() {
		if (null != envVars) {
			return (Properties)envVars.clone();
		}

		envVars = new Properties();
		rawVars = new ArrayList<String>();
		Map<String,String> envMap = System.getenv();
		for (String var : envMap.keySet()) {
	        String value = envMap.get(var);
			envVars.setProperty(var, value);
			rawVars.add(var + "=" + value); //$NON-NLS-1$
        }
		rawVars.trimToSize();
		return (Properties)envVars.clone();
	}

	public static String getEnvVar(String key) {
		Properties p = getEnvVars();
		return p.getProperty(key);
	}

	public static synchronized String[] getRawEnvVars() {
		if (rawVars==null) 
			getEnvVars();
		return rawVars.toArray(new String[rawVars.size()]);
	}
}
