/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

public class EnvironmentReader {
	private static Properties envVars = null;
	private static Vector rawVars = null;

	public static Properties getEnvVars() {

		if (null != envVars)
			return (Properties)envVars.clone();

		String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		Process p = null;
		envVars = new Properties();
		rawVars = new Vector(32);
		String command = "env"; //$NON-NLS-1$
		InputStream in = null;
		boolean check_ready = false;
		boolean isWin32 = false;
		if (OS.startsWith("windows 9") || OS.startsWith("windows me")) { // 95, 98, me //$NON-NLS-1$ //$NON-NLS-2$
			command = "command.com /c set"; //$NON-NLS-1$
			//The buffered stream doesn't always like windows 98
			check_ready = true;
			isWin32 = true;
		} else 
		if (OS.startsWith("windows ")) { //$NON-NLS-1$
			command = "cmd.exe /c set"; //$NON-NLS-1$
			isWin32 = true;
		}
		try {
			p = ProcessFactory.getFactory().exec(command);
			in = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				rawVars.add(line);
				int idx = line.indexOf('=');
				if (idx != -1) {
					String key = line.substring(0, idx);
					if (isWin32) //Since windows env ignores case let normalize to Upper here.
						key = key.toUpperCase();
					String value = line.substring(idx + 1);
					envVars.setProperty(key, value);
				} else {
					envVars.setProperty(line, ""); //$NON-NLS-1$
				}
				if (check_ready && br.ready() == false) {
					break;
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
			try {
				if (p != null)
					p.waitFor();
			} catch (InterruptedException e) {
			}
		}
		rawVars.trimToSize();
		return (Properties)envVars.clone();
	}

	public static String getEnvVar(String key) {
		Properties p = getEnvVars();
		return p.getProperty(key);
	}

	public static String[] getRawEnvVars() {
		getEnvVars();
		return (String[]) rawVars.toArray(new String[0]);
	}
}
