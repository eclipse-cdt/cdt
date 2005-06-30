/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.spawner;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.Properties;
import java.util.Vector;

public class EnvironmentReader {
	private static Properties envVars = null;
	private static Vector rawVars = null;

	public static Properties getEnvVars() {

		if (null != envVars) {
			return (Properties)envVars.clone();
		}

		String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		Process p = null;
		envVars = new Properties();
		rawVars = new Vector(32);
		String command = "env"; //$NON-NLS-1$
		InputStream in = null;
		boolean check_ready = false;
		boolean isWin32 = false;
		String charSet = null;
		if (OS.startsWith("windows 9") || OS.startsWith("windows me")) { // 95, 98, me //$NON-NLS-1$ //$NON-NLS-2$
			command = "command.com /c set"; //$NON-NLS-1$
			//The buffered stream doesn't always like windows 98
			check_ready = true;
			isWin32 = true;
		} else if (OS.startsWith("windows ")) { //$NON-NLS-1$
			command = "cmd.exe /u /c  set"; //$NON-NLS-1$
			isWin32 = true;
			charSet = "UTF-16" + (ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()) ? "BE" : "LE");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		try {
			p = ProcessFactory.getFactory().exec(command);
			in = p.getInputStream();
			BufferedReader br;
			if (null == charSet) { 
				br = new BufferedReader(new InputStreamReader(in));
			} else {
				br = new BufferedReader(new InputStreamReader(in, charSet));
			}
			String line;
			String prev_key = null; // previous key read and saved in envVars
			String prev_value = null; // value of previous key
			while ((line = br.readLine()) != null) {
				rawVars.add(line);
				int idx = line.indexOf('=');
				if (idx != -1) {
					String key = line.substring(0, idx);
					if (isWin32) { //Since windows env ignores case let normalize to Upper here.
						key = key.toUpperCase();
					}
					String value = line.substring(idx + 1);
					envVars.setProperty(key, value);
					// Save key and value in case we're dealing with a variable with
					// a multi-line value; i.e., it contains embedded new lines (\n).
					prev_key = key;
					prev_value = value;
				} else {
					// Any variable setting that contains '\n' will be read as 
					// separate lines into the line variable.  Subsequent lines
					// after the first line (which contains '=') takes us to
					// this else case.  We try to restore the original setting
					// by concatenating the value of line with previous value,
					// with a \n replaced in between, and saving the new value with
					// the previous key.
					if (prev_key != null) {
						if (prev_value != null) {
							prev_value = prev_value + '\n' + line;
						} else {
							prev_value = line;
						}
						envVars.setProperty(prev_key, prev_value);
					} else {
						envVars.setProperty(line, ""); //$NON-NLS-1$
					}
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
				if (p != null) {
					p.waitFor();
				}
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
