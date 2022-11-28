/********************************************************************************
 * Copyright (c) 2022 徐持恒 Xu Chiheng.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.eclipse.cdt.utils.spawner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 *
 * use ProcessBuilder to launch process
 *
 * @see "https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ProcessBuilder.html"
 *
 */

public class ProcessFactory2 {

	private static TreeMap<String, String> newEmptyEnvironment() {
		TreeMap<String, String> environment;
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			environment = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		} else {
			environment = new TreeMap<>();
		}
		return environment;
	}

	private static TreeMap<String, String> envpToEnvMap(String[] envp) {
		TreeMap<String, String> environment = newEmptyEnvironment();
		if (envp != null) {
			for (String envstring : envp) {
				int eqlsign = envstring.indexOf('=');
				if (eqlsign != -1) {
					environment.put(envstring.substring(0, eqlsign), envstring.substring(eqlsign + 1));
				} else {
					// Silently ignore envstrings lacking the required `='.
				}
			}
		}
		return environment;
	}

	public static Process exec(String cmdarray[], String[] envp, File dir) throws IOException {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			cmdarray[0] = Path.fromOSString(cmdarray[0]).toPortableString();
		}

		List<String> cmdList = Arrays.asList(cmdarray);
		ProcessBuilder pb = new ProcessBuilder(cmdList);
		TreeMap<String, String> environment = newEmptyEnvironment();
		Map<String, String> env = pb.environment();
		environment.putAll(env);
		env.clear();
		if (envp != null) {
			TreeMap<String, String> environment1 = envpToEnvMap(envp);
			environment.putAll(environment1);
		}

		{
			boolean setLangEnvVartoDefault = false;
			String langEnvValue = environment.get("LANG"); //$NON-NLS-1$
			if (langEnvValue == null) {
				setLangEnvVartoDefault = true;
			} else {
				String[] array = langEnvValue.split("\\."); //$NON-NLS-1$
				if (array == null || array.length != 2) {
					setLangEnvVartoDefault = true;
				} else {
					String country = array[0];
					String charset = array[1];
					if (country.equals("C")) { //$NON-NLS-1$
						country = "en_US"; //$NON-NLS-1$
					}
					if (!charset.equals("UTF-8")) { //$NON-NLS-1$
						charset = "UTF-8"; //$NON-NLS-1$
					}
					langEnvValue = country + '.' + charset;
					environment.put("LANG", langEnvValue); //$NON-NLS-1$
					environment.put("LC_ALL", langEnvValue); //$NON-NLS-1$
				}
			}
			if (setLangEnvVartoDefault) {
				environment.put("LANG", "en_US.UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
				environment.put("LC_ALL", "en_US.UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		env.putAll(environment);
		if (dir != null) {
			pb.directory(dir);
		}
		/*
		 * for debug purpose
		 */
		StringBuilder sb = new StringBuilder();
		{
			for (int i = 0; i < cmdarray.length; i++) {
				if (i != 0) {
					sb.append(' ');
				}
				sb.append('\'');
				sb.append(cmdarray[i]);
				sb.append('\'');
			}
			sb.append("\n\n"); //$NON-NLS-1$

			Iterator<Entry<String, String>> iterator = environment.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				sb.append(entry.getKey());
				sb.append('=');
				sb.append(entry.getValue());
				sb.append('\n');
			}

			sb.append("\n"); //$NON-NLS-1$
			if (dir != null) {
				sb.append(dir.toString());
			} else {
				sb.append("no directory specified"); //$NON-NLS-1$
			}
			sb.append("\n"); //$NON-NLS-1$
		}
		/*
		 * set breakpoint on next line to inspect sb when debugging, to see the
		 * ultimate parameters of ProcessBuilder
		 */
		Process p = pb.start();
		return p;
	}

}
