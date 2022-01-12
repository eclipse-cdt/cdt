/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Application is responsible for calling core launch api
 */
public class NewMain extends Main {
	private static final String DEFAULT_APPLICATION = "org.eclipse.ui.workbench";

	public NewMain(String application, String location, URL pluginPathLocation, String bootLocation, boolean debug)
			throws IOException {
		this.application = application;
		this.location = location;
		this.pluginPathLocation = pluginPathLocation;
		this.bootLocation = bootLocation;
	}

	public static void main(String[] args) {
		try {
			String location = getLocationFromProperties("platform");
			new NewMain(DEFAULT_APPLICATION, location, null, null, true).run(args);
		} catch (Throwable e) {
			System.out.println("Exception launching the Eclipse Platform UI:");
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * Run this launcher with the arguments specified in the given string.
	 * This is a short cut method for people running the launcher from
	 * a scrapbook (i.e., swip-and-doit facility).
	 */
	public static void main(String argString) throws Exception {
		List<String> list = new ArrayList<>(5);
		for (StringTokenizer tokens = new StringTokenizer(argString, " "); tokens.hasMoreElements();)
			list.add((String) tokens.nextElement());
		main(list.toArray(new String[list.size()]));
	}

	public static String getLocationFromProperties(String key) {
		Properties properties = new Properties();
		try {
			FileInputStream fis = new FileInputStream(getSettingsFile());
			properties.load(fis);
			return properties.getProperty(key);
		} catch (IOException e) {
		}
		return null;
	}

	private static File getSettingsFile() {
		String home = System.getProperty("user.home");
		if (home == null) {
			System.out.println("Home dir not defined");
			return null;
		}
		return new File(home, "eclipse-workspaces.properties");
	}
}
