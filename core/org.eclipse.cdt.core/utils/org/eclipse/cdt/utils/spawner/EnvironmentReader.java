package org.eclipse.cdt.utils.spawner;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
			return envVars;

		String OS = System.getProperty("os.name").toLowerCase();
		Process p = null;
		envVars = new Properties();
		rawVars = new Vector(32);
		String command = "env";
		InputStream	in = null;
		try {
			if (OS.indexOf("windows 9") > -1) {
				command = "command.com /c set";
			} else if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows 2000") > -1)) {
				command = "cmd.exe /c set";
			}
			p = ProcessFactory.getFactory().exec(command);
			in = p .getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				rawVars.add(line);
				int idx = line.indexOf('=');
				if (idx != -1) {
					String key = line.substring(0, idx);
					String value = line.substring(idx + 1);
					envVars.setProperty(key, value);
				} else {
					envVars.setProperty(line, "");
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
		return envVars;
	}

	public static String getEnvVar(String key) {
		Properties p = getEnvVars();
		return p.getProperty(key);
	}
	
	public static String[] getRawEnvVars() {
		getEnvVars();
		return (String[])rawVars.toArray( new String[0] );
	}
}
